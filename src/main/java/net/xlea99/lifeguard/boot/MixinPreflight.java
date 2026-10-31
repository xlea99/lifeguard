package net.xlea99.lifeguard.boot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.MixinGate;
import net.xlea99.lifeguard.core.Registry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Locals;

/**
 * Checks, before any mixin applies, that every Lifeguard mixin that could apply (its entry installed, on this side, on
 * or off) still fits what it hooks: its target classes, methods, injection points (with their ordinals and required
 * counts), constants, shadows, accessors, invokers, the other mods' mixins it reaches into, and its {@code @Local}s. A mixin that doesn't fit breaks its whole entry
 * ({@link BrokenEntries}), so none of that entry's mixins apply and the game still starts; a core mixin that doesn't fit
 * crashes, since core is always on.
 *
 * <p>It reads bytecode through Mixin's own provider, which never loads a class (proven 2026-10-30: the targets still
 * took their mixins afterwards). It sees each target as it is before any mixin, so another mod's mixin that removes a
 * call we hook isn't visible here; {@code defaultRequire: 1} stays as the backstop for that.
 *
 * <p>Strict on purpose: an annotation or attribute it doesn't understand counts as not fitting, so a new kind of hook
 * fails the dev checks instead of slipping past them.
 */
final class MixinPreflight {
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifeguard");
    private static final String CONFIG = "lifeguard.mixins.json";

    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String SHADOW = "Lorg/spongepowered/asm/mixin/Shadow;";
    private static final String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
    private static final String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
    private static final String DYNAMIC = "Lorg/spongepowered/asm/mixin/Dynamic;";
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String REDIRECT = "Lorg/spongepowered/asm/mixin/injection/Redirect;";
    private static final String MODIFY_ARG = "Lorg/spongepowered/asm/mixin/injection/ModifyArg;";
    private static final String MODIFY_VARIABLE = "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;";
    private static final String MODIFY_CONSTANT = "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;";
    private static final String WRAP_OPERATION = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;";
    private static final String MODIFY_RETURN_VALUE = "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;";
    private static final String MODIFY_EXPRESSION_VALUE = "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;";
    private static final String WRAP_METHOD = "Lcom/llamalad7/mixinextras/injector/wrapmethod/WrapMethod;";
    private static final String LOCAL = "Lcom/llamalad7/mixinextras/sugar/Local;";
    private static final String TARGET_HANDLER = "Lcom/bawnorton/mixinsquared/TargetHandler;";
    private static final String CALLBACK_INFO = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
    private static final String CALLBACK_INFO_RETURNABLE =
        "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";

    private static final Set<String> INJECTORS = Set.of(INJECT, REDIRECT, MODIFY_ARG, MODIFY_VARIABLE, MODIFY_CONSTANT,
        WRAP_OPERATION, MODIFY_RETURN_VALUE, MODIFY_EXPRESSION_VALUE, WRAP_METHOD);
    // Understood and nothing to check: they change how a member merges, not what it hooks.
    private static final Set<String> NOTHING_TO_CHECK = Set.of(MIXIN, DYNAMIC, TARGET_HANDLER,
        "Lorg/spongepowered/asm/mixin/Unique;", "Lorg/spongepowered/asm/mixin/Final;",
        "Lorg/spongepowered/asm/mixin/Mutable;");
    private static final List<String> HOOK_LIBRARIES = List.of("Lorg/spongepowered/asm/mixin/",
        "Lcom/llamalad7/mixinextras/", "Lcom/bawnorton/mixinsquared/");
    private static final Set<String> AT_KEYS = Set.of("value", "target", "ordinal", "args");

    private final ClassSource source;
    private final Map<String, ClassNode> classes = new HashMap<>();
    private final Map<String, String> unreadable = new HashMap<>();

    /** Where classes come from: Mixin's provider in the game; compiled fixtures in the tests. */
    interface ClassSource {
        ClassNode read(String internalName) throws Exception;
    }

    MixinPreflight(ClassSource source) {
        this.source = source;
    }

    /** Checks every mixin that would apply on this side and breaks the entries whose mixins don't fit. */
    static void run(Registry registry, boolean dedicatedServer) {
        long start = System.nanoTime();
        IClassBytecodeProvider bytecode = MixinService.getService().getBytecodeProvider();
        int checked = new MixinPreflight(name -> bytecode.getClassNode(name, true)).check(registry, dedicatedServer);
        LOGGER.info("Lifeguard preflight: {} mixins checked in {} ms, {} entries broken", checked,
            (System.nanoTime() - start) / 1_000_000, registry.broken().size());
    }

    private int check(Registry registry, boolean dedicatedServer) {
        JsonObject config = readConfig();
        String root = config.get("package").getAsString() + ".";
        int checked = 0;
        for (String side : dedicatedServer ? List.of("mixins", "server") : List.of("mixins", "client", "server")) {
            if (!config.has(side))
                continue;
            for (JsonElement listed : (JsonArray) config.get(side)) {
                String mixin = root + listed.getAsString();
                if (!MixinGate.couldApply(mixin, registry, dedicatedServer))
                    continue;
                checked++;
                List<String> problems;
                try {
                    problems = checkMixin(mixin);
                } catch (RuntimeException e) {
                    problems = List.of("the preflight couldn't check it (" + e + ")");
                }
                if (problems.isEmpty())
                    continue;
                String what = simple(mixin) + ": " + String.join("; ", problems);
                if (MixinGate.isCore(mixin))
                    throw new IllegalStateException("Lifeguard's core mixin " + what + ". Core mixins are always on, so"
                        + " this stops the game: this Lifeguard doesn't fit this Minecraft or NeoForge.");
                for (Entry owner : MixinGate.owners(mixin, registry))
                    if (owner.installed())
                        BrokenEntries.mark(owner, what);
            }
        }
        // Other mods' mixins an entry switches off, by name: one that's gone means the clash it guards against may be
        // back under a new name, and the entry would never know.
        for (Entry entry : registry.entries())
            if (entry.installed())
                for (String cancelled : entry.cancelledMixins())
                    if (read(internal(cancelled)) == null)
                        BrokenEntries.mark(entry, "it switches off " + cancelled + ", which isn't there any more ("
                            + unreadable.get(internal(cancelled)) + "): the clash it guards against may be back");
        return checked;
    }

    private static JsonObject readConfig() {
        try (InputStream in = MixinPreflight.class.getClassLoader().getResourceAsStream(CONFIG)) {
            if (in == null)
                throw new IllegalStateException(CONFIG + " isn't in Lifeguard's jar");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Lifeguard couldn't read " + CONFIG, e);
        }
    }

    // --- one mixin -------------------------------------------------------------------------------------------------

    /** What doesn't fit in one mixin (by its dotted name), as sentences; empty when it all fits. */
    List<String> checkMixin(String mixinName) {
        List<String> problems = new ArrayList<>();
        ClassNode mixin = read(internal(mixinName));
        if (mixin == null)
            return List.of("its own class can't be read (" + unreadable.get(internal(mixinName)) + ")");
        AnnotationNode mixinAnnotation = annotation(mixin.invisibleAnnotations, mixin.visibleAnnotations, MIXIN);
        if (mixinAnnotation == null)
            return List.of("it has no @Mixin");
        List<ClassNode> targets = new ArrayList<>();
        for (String target : mixinTargets(mixinAnnotation)) {
            ClassNode node = read(target);
            if (node == null)
                problems.add("its target " + dotted(target) + " can't be read (" + unreadable.get(target) + ")");
            else
                targets.add(node);
        }
        if (targets.isEmpty()) {
            if (problems.isEmpty())
                problems.add("it names no target");
            return problems;
        }

        for (FieldNode field : mixin.fields)
            if (annotation(field.invisibleAnnotations, field.visibleAnnotations, SHADOW) != null
                && findField(targets, field.name, field.desc, true) == null)
                problems.add("@Shadow field " + field.name + " (" + field.desc + ") is gone from " + names(targets));

        for (MethodNode method : mixin.methods) {
            List<AnnotationNode> annotations = all(method.invisibleAnnotations, method.visibleAnnotations);
            for (AnnotationNode annotation : annotations) {
                String desc = annotation.desc;
                if (desc.equals(SHADOW))
                    checkShadowMethod(method, annotation, targets, problems);
                else if (desc.equals(ACCESSOR))
                    checkAccessor(method, annotation, targets, problems);
                else if (desc.equals(INVOKER))
                    checkInvoker(method, annotation, targets, problems);
                else if (INJECTORS.contains(desc))
                    checkInjector(method, annotation, annotations, targets, problems);
                else if (!NOTHING_TO_CHECK.contains(desc) && HOOK_LIBRARIES.stream().anyMatch(desc::startsWith))
                    problems.add(method.name + " uses @" + simple(Type.getType(desc).getClassName())
                        + ", which the preflight can't check yet");
            }
        }
        return problems;
    }

    private static List<String> mixinTargets(AnnotationNode mixin) {
        List<String> out = new ArrayList<>();
        for (Object type : list(mixin, "value"))
            out.add(((Type) type).getInternalName());
        for (Object name : list(mixin, "targets"))
            out.add(internal((String) name));
        return out;
    }

    // --- members -------------------------------------------------------------------------------------------------

    private void checkShadowMethod(MethodNode method, AnnotationNode shadow, List<ClassNode> targets, List<String> problems) {
        String prefix = string(shadow, "prefix", "shadow$");
        String name = method.name.startsWith(prefix) ? method.name.substring(prefix.length()) : method.name;
        if (findMethod(targets, name, method.desc, true) == null)
            problems.add("@Shadow method " + name + method.desc + " is gone from " + names(targets));
    }

    private void checkAccessor(MethodNode method, AnnotationNode accessor, List<ClassNode> targets, List<String> problems) {
        Type type = Type.getMethodType(method.desc);
        boolean getter = type.getArgumentTypes().length == 0;
        String fieldDesc = getter ? type.getReturnType().getDescriptor() : type.getArgumentTypes()[0].getDescriptor();
        List<String> names = candidates(string(accessor, "value", ""), method.name, List.of("get", "is", "set"));
        for (String name : names)
            if (findField(targets, name, fieldDesc, false) != null)
                return;
        problems.add("@Accessor " + method.name + ": no field " + String.join(" or ", names) + " (" + fieldDesc + ") in "
            + names(targets));
    }

    private void checkInvoker(MethodNode method, AnnotationNode invoker, List<ClassNode> targets, List<String> problems) {
        String explicit = string(invoker, "value", "");
        boolean constructor = explicit.equals("<init>")
            || explicit.isEmpty() && (method.name.startsWith("new") || method.name.startsWith("create"));
        if (constructor) {
            String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(method.desc));
            if (findMethod(targets, "<init>", desc, false) == null)
                problems.add("@Invoker " + method.name + ": no constructor " + desc + " in " + names(targets));
            return;
        }
        List<String> names = candidates(explicit, method.name, List.of("call", "invoke"));
        for (String name : names)
            if (findMethod(targets, name, method.desc, false) != null)
                return;
        problems.add("@Invoker " + method.name + ": no method " + String.join(" or ", names) + method.desc + " in "
            + names(targets));
    }

    /** The member an accessor or invoker means: the explicit name, else its own name less the prefix, both cases. */
    private static List<String> candidates(String explicit, String methodName, List<String> prefixes) {
        if (!explicit.isEmpty())
            return List.of(explicit);
        for (String prefix : prefixes)
            if (methodName.startsWith(prefix) && methodName.length() > prefix.length()) {
                String bare = methodName.substring(prefix.length());
                String lower = Character.toLowerCase(bare.charAt(0)) + bare.substring(1);
                return bare.equals(lower) ? List.of(bare) : List.of(lower, bare);
            }
        return List.of(methodName);
    }

    // --- injectors -------------------------------------------------------------------------------------------------

    private record Target(ClassNode owner, MethodNode method) {
        String describe() {
            return simple(dotted(owner.name)) + "." + method.name + method.desc;
        }
    }

    private void checkInjector(MethodNode handler, AnnotationNode injector, List<AnnotationNode> annotations,
                               List<ClassNode> targets, List<String> problems) {
        for (int i = 0; i < vals(injector).size(); i += 2) {
            String key = (String) vals(injector).get(i);
            if (!Set.of("method", "at", "constant", "require", "cancellable", "argsOnly", "index", "ordinal")
                .contains(key))
                problems.add(handler.name + " sets " + key + ", which the preflight can't check yet");
        }
        List<Target> methods = targetMethods(handler, injector, annotations, targets, problems);
        if (methods.isEmpty())
            return;
        int required = integer(injector, "require", 1);
        int found = 0;
        String desc = injector.desc;
        if (desc.equals(WRAP_METHOD)) {
            found = methods.size();
        } else if (desc.equals(MODIFY_CONSTANT)) {
            List<Object> constants = list(injector, "constant");
            if (constants.isEmpty())
                problems.add(handler.name + " names no @Constant, which the preflight can't check yet");
            for (Object constant : constants) {
                int here = 0;
                for (Target target : methods)
                    here += ordinal(constantMatches(target.method(), (AnnotationNode) constant, problems, handler),
                        integer((AnnotationNode) constant, "ordinal", -1)).size();
                if (here == 0)
                    problems.add(handler.name + ": " + describeConstant((AnnotationNode) constant) + " is gone from "
                        + describe(methods));
                found += here;
            }
        } else {
            List<AnnotationNode> ats = new ArrayList<>();
            Object at = value(injector, "at");
            if (at instanceof AnnotationNode single)
                ats.add(single);
            else if (at instanceof List<?> several)
                for (Object one : several)
                    ats.add((AnnotationNode) one);
            if (ats.isEmpty())
                problems.add(handler.name + " has no @At");
            for (AnnotationNode one : ats) {
                int here = 0;
                for (Target target : methods) {
                    List<AbstractInsnNode> points = points(target, one, handler, injector, problems);
                    here += points.size();
                    checkLocals(handler, target, points, problems);
                }
                if (here == 0)
                    problems.add(handler.name + ": " + describeAt(one) + " is gone from " + describe(methods));
                found += here;
            }
        }
        if (desc.equals(MODIFY_VARIABLE))
            for (Target target : methods)
                modifiedSlot(handler, injector, target, problems);
        if (desc.equals(INJECT))
            for (Target target : methods)
                checkCapturedArguments(handler, target, problems);
        if (found < required)
            problems.add(handler.name + " finds " + found + " of the " + required + " places it needs in "
                + describe(methods));
    }

    /** The methods an injector targets: by its selectors, or in the other mod's mixin it reaches into. */
    private List<Target> targetMethods(MethodNode handler, AnnotationNode injector, List<AnnotationNode> annotations,
                                       List<ClassNode> targets, List<String> problems) {
        AnnotationNode targetHandler = find(annotations, TARGET_HANDLER);
        if (targetHandler != null) {
            String other = internal(string(targetHandler, "mixin", ""));
            String name = string(targetHandler, "name", "");
            ClassNode otherMixin = read(other);
            if (otherMixin == null) {
                problems.add(handler.name + ": the mixin it reaches into, " + dotted(other) + ", can't be read ("
                    + unreadable.get(other) + ")");
                return List.of();
            }
            List<Target> found = new ArrayList<>();
            for (MethodNode method : otherMixin.methods)
                if (method.name.equals(name))
                    found.add(new Target(otherMixin, method));
            if (found.isEmpty())
                problems.add(handler.name + ": " + simple(dotted(other)) + " has no handler " + name + " any more");
            return found;
        }

        List<ClassNode> searched = new ArrayList<>(targets);
        AnnotationNode dynamic = find(annotations, DYNAMIC);
        if (dynamic != null) {
            Object adder = value(dynamic, "mixin");
            if (!(adder instanceof Type type)) {
                problems.add(handler.name + " is @Dynamic without naming the mixin that adds its target (mixin = ...),"
                    + " so the preflight can't check it");
                return List.of();
            }
            ClassNode adding = read(type.getInternalName());
            if (adding == null) {
                problems.add(handler.name + ": the mixin that adds its target, " + type.getClassName()
                    + ", can't be read (" + unreadable.get(type.getInternalName()) + ")");
                return List.of();
            }
            searched.add(adding);
        }

        List<Target> found = new ArrayList<>();
        List<Object> selectors = list(injector, "method");
        if (selectors.isEmpty())
            problems.add(handler.name + " names no target method");
        for (Object selector : selectors) {
            String text = (String) selector;
            if (text.contains(" ") || text.contains("{") || text.indexOf('*') >= 0 && !text.endsWith("*")) {
                problems.add(handler.name + ": the selector " + text + " is a pattern, which the preflight can't check yet");
                continue;
            }
            Selector parsed = Selector.parse(text);
            boolean staticHandler = (handler.access & Opcodes.ACC_STATIC) != 0;
            boolean any = false;
            // As Mixin does: a plain selector takes the first match in each class, in declaration order (its default
            // quantifier allows one), so a later synthetic bridge of the same name is never a target. A quantified one
            // ("*", "name+") takes every match, constructors included, but only static methods for a static handler.
            for (ClassNode owner : searched)
                for (MethodNode method : owner.methods)
                    if (parsed.matches(owner, method)) {
                        if (parsed.multi() && !staticHandler && (method.access & Opcodes.ACC_STATIC) != 0)
                            continue;
                        found.add(new Target(owner, method));
                        any = true;
                        if (!parsed.multi())
                            break;
                    }
            if (!any)
                problems.add(handler.name + ": method " + text + " is gone from " + names(searched));
        }
        return found;
    }

    /** A method selector: {@code name}, {@code name(desc)}, {@code Lowner;name(desc)}; a trailing * or + takes all. */
    private record Selector(String owner, String name, String desc, boolean multi) {
        static Selector parse(String text) {
            String owner = null;
            int semi = text.indexOf(';');
            int paren = text.indexOf('(');
            if (text.startsWith("L") && semi > 0 && (paren < 0 || semi < paren)) {
                owner = text.substring(1, semi);
                text = text.substring(semi + 1);
                paren = text.indexOf('(');
            }
            String name = paren >= 0 ? text.substring(0, paren) : text;
            String desc = paren >= 0 ? text.substring(paren) : null;
            boolean multi = name.endsWith("*") || name.endsWith("+");
            if (multi)
                name = name.substring(0, name.length() - 1);
            return new Selector(owner, name.isEmpty() ? null : name, desc, multi);
        }

        boolean matches(ClassNode in, MethodNode method) {
            return (name == null || method.name.equals(name)) && (desc == null || desc.equals(method.desc))
                && (owner == null || owner.equals(in.name));
        }
    }

    /** Where an {@code @At} lands in one target method: every match, cut to its ordinal when it has one. */
    private List<AbstractInsnNode> points(Target target, AnnotationNode at, MethodNode handler, AnnotationNode injector,
                                          List<String> problems) {
        for (int i = 0; i < vals(at).size(); i += 2)
            if (!AT_KEYS.contains((String) vals(at).get(i))) {
                problems.add(handler.name + ": @At sets " + vals(at).get(i) + ", which the preflight can't check yet");
                return List.of();
            }
        String kind = string(at, "value", "");
        String member = string(at, "target", "");
        MethodNode method = target.method();
        List<AbstractInsnNode> matches = new ArrayList<>();
        switch (kind) {
            case "HEAD" -> {
                AbstractInsnNode first = firstReal(method);
                if (first != null)
                    matches.add(first);
            }
            case "RETURN", "TAIL" -> {
                for (AbstractInsnNode insn : method.instructions)
                    if (insn.getOpcode() >= Opcodes.IRETURN && insn.getOpcode() <= Opcodes.RETURN)
                        matches.add(insn);
                if (kind.equals("TAIL") && !matches.isEmpty())
                    matches = List.of(matches.get(matches.size() - 1));
            }
            case "INVOKE" -> {
                Member wanted = Member.parse(member);
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof MethodInsnNode call && wanted.matches(call.owner, call.name, call.desc))
                        matches.add(insn);
            }
            case "FIELD" -> {
                Member wanted = Member.parse(member);
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof FieldInsnNode field && wanted.matches(field.owner, field.name, field.desc))
                        matches.add(insn);
            }
            case "NEW" -> {
                if (member.contains("(")) {
                    problems.add(handler.name + ": @At NEW by constructor descriptor, which the preflight can't check yet");
                    return List.of();
                }
                String type = member.startsWith("L") && member.endsWith(";") ? member.substring(1, member.length() - 1)
                    : internal(member);
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof TypeInsnNode node && node.getOpcode() == Opcodes.NEW && node.desc.equals(type))
                        matches.add(insn);
            }
            case "CONSTANT" -> matches = constantMatches(method, constantFromArgs(list(at, "args")), problems, handler);
            case "LOAD" -> {
                int slot = injector.desc.equals(MODIFY_VARIABLE) ? modifiedSlot(handler, injector, target, null) : -2;
                if (slot == -2) {
                    problems.add(handler.name + ": @At LOAD outside an argsOnly @ModifyVariable, which the preflight"
                        + " can't check yet");
                    return List.of();
                }
                for (AbstractInsnNode insn : method.instructions)
                    if (insn instanceof VarInsnNode load && load.var == slot && load.getOpcode() >= Opcodes.ILOAD
                        && load.getOpcode() <= Opcodes.ALOAD)
                        matches.add(insn);
            }
            default -> {
                problems.add(handler.name + ": @At(\"" + kind + "\") is a kind the preflight can't check yet");
                return List.of();
            }
        }
        return ordinal(matches, integer(at, "ordinal", -1));
    }

    private static List<AbstractInsnNode> ordinal(List<AbstractInsnNode> matches, int ordinal) {
        if (ordinal < 0)
            return matches;
        return matches.size() > ordinal ? List.of(matches.get(ordinal)) : List.of();
    }

    private record Member(String owner, String name, String desc) {
        /** {@code Lowner;name(args)ret} for a call, {@code Lowner;name:desc} for a field; the owner may be left out. */
        static Member parse(String text) {
            String owner = null;
            int semi = text.indexOf(';');
            int split = text.indexOf('(') >= 0 ? text.indexOf('(') : text.indexOf(':');
            if (text.startsWith("L") && semi > 0 && (split < 0 || semi < split)) {
                owner = text.substring(1, semi);
                text = text.substring(semi + 1);
            }
            int paren = text.indexOf('(');
            int colon = text.indexOf(':');
            if (paren >= 0)
                return new Member(owner, text.substring(0, paren), text.substring(paren));
            if (colon >= 0)
                return new Member(owner, text.substring(0, colon), text.substring(colon + 1));
            return new Member(owner, text, null);
        }

        boolean matches(String inOwner, String inName, String inDesc) {
            return name.equals(inName) && (desc == null || desc.equals(inDesc)) && (owner == null || owner.equals(inOwner));
        }
    }

    // --- constants ---------------------------------------------------------------------------------------------------

    /** {@code args = "classValue=..."} as the @Constant it stands for: one key, one value. */
    private static AnnotationNode constantFromArgs(List<Object> args) {
        AnnotationNode constant = new AnnotationNode("Lorg/spongepowered/asm/mixin/injection/Constant;");
        constant.values = new ArrayList<>();
        for (Object arg : args) {
            String text = (String) arg;
            int equals = text.indexOf('=');
            if (equals < 0)
                continue;
            String key = text.substring(0, equals);
            String raw = text.substring(equals + 1);
            Object value = switch (key) {
                case "intValue" -> Integer.valueOf(raw);
                case "floatValue" -> Float.valueOf(raw);
                case "longValue" -> Long.valueOf(raw);
                case "doubleValue" -> Double.valueOf(raw);
                case "classValue" -> Type.getObjectType(internal(raw));
                default -> raw;
            };
            constant.values.add(key);
            constant.values.add(value);
        }
        return constant;
    }

    private static List<AbstractInsnNode> constantMatches(MethodNode method, AnnotationNode constant, List<String> problems,
                                                          MethodNode handler) {
        List<AbstractInsnNode> matches = new ArrayList<>();
        Object want = null;
        String key = null;
        for (int i = 0; i < vals(constant).size(); i += 2) {
            String k = (String) vals(constant).get(i);
            if (k.equals("ordinal"))
                continue;
            if (key != null || !Set.of("intValue", "floatValue", "longValue", "doubleValue", "stringValue", "classValue")
                .contains(k)) {
                problems.add(handler.name + ": " + describeConstant(constant) + " is a constant the preflight can't check yet");
                return matches;
            }
            key = k;
            want = vals(constant).get(i + 1);
        }
        if (key == null) {
            problems.add(handler.name + ": an empty @Constant, which the preflight can't check yet");
            return matches;
        }
        for (AbstractInsnNode insn : method.instructions) {
            Object found = constantAt(insn, key);
            if (found != null && found.equals(want))
                matches.add(insn);
        }
        return matches;
    }

    /** The constant an instruction pushes, as the @Constant key would hold it, or null. */
    private static Object constantAt(AbstractInsnNode insn, String key) {
        int op = insn.getOpcode();
        if (insn instanceof LdcInsnNode ldc)
            return ldc.cst instanceof Type type && !key.equals("classValue") ? null : ldc.cst;
        return switch (key) {
            case "intValue" -> op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5 ? Integer.valueOf(op - Opcodes.ICONST_0)
                : insn instanceof IntInsnNode push && (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) ? push.operand : null;
            case "floatValue" -> op >= Opcodes.FCONST_0 && op <= Opcodes.FCONST_2 ? Float.valueOf(op - Opcodes.FCONST_0)
                : null;
            case "longValue" -> op == Opcodes.LCONST_0 || op == Opcodes.LCONST_1 ? Long.valueOf(op - Opcodes.LCONST_0)
                : null;
            case "doubleValue" -> op == Opcodes.DCONST_0 || op == Opcodes.DCONST_1
                ? Double.valueOf(op - Opcodes.DCONST_0) : null;
            // Mixin's classValue also lands on instanceof checks of that class.
            case "classValue" -> insn instanceof TypeInsnNode check && op == Opcodes.INSTANCEOF
                ? Type.getObjectType(check.desc) : null;
            default -> null;
        };
    }

    private static String describeConstant(AnnotationNode constant) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < vals(constant).size(); i += 2)
            parts.add(vals(constant).get(i) + " = " + vals(constant).get(i + 1));
        return "@Constant(" + String.join(", ", parts) + ")";
    }

    // --- locals and arguments ----------------------------------------------------------------------------------------

    /** Each {@code @Local} on the handler finds its local, by type and ordinal, at every place the injector lands. */
    private void checkLocals(MethodNode handler, Target target, List<AbstractInsnNode> points, List<String> problems) {
        Type[] parameters = Type.getArgumentTypes(handler.desc);
        for (int i = 0; i < parameters.length; i++) {
            AnnotationNode local = parameterAnnotation(handler, i, LOCAL);
            if (local == null)
                continue;
            for (int k = 0; k < vals(local).size(); k += 2)
                if (!Set.of("argsOnly", "ordinal").contains((String) vals(local).get(k))) {
                    problems.add(handler.name + ": @Local sets " + vals(local).get(k)
                        + ", which the preflight can't check yet");
                    return;
                }
            String type = parameters[i].getDescriptor();
            int ordinal = integer(local, "ordinal", -1);
            if (Boolean.TRUE.equals(value(local, "argsOnly"))) {
                int count = 0;
                for (Type argument : Type.getArgumentTypes(target.method().desc))
                    if (argument.getDescriptor().equals(type))
                        count++;
                if (!(ordinal < 0 ? count == 1 : count > ordinal))
                    problems.add(handler.name + ": @Local(argsOnly) " + parameters[i].getClassName()
                        + (ordinal < 0 ? "" : " #" + ordinal) + " isn't among the arguments of " + target.describe());
                continue;
            }
            for (AbstractInsnNode point : points) {
                LocalVariableNode[] locals;
                try {
                    locals = Locals.getLocalsAt(target.owner(), target.method(), point, Locals.Settings.DEFAULT);
                } catch (RuntimeException e) {
                    problems.add(handler.name + ": the locals of " + target.describe() + " can't be read (" + e + ")");
                    return;
                }
                int count = 0;
                for (LocalVariableNode found : locals)
                    if (found != null && found.desc.equals(type))
                        count++;
                if (!(ordinal < 0 ? count == 1 : count > ordinal)) {
                    problems.add(handler.name + ": @Local " + parameters[i].getClassName()
                        + (ordinal < 0 ? "" : " #" + ordinal) + " isn't a local where it hooks " + target.describe());
                    return;
                }
            }
        }
    }

    /**
     * The slot an argsOnly {@code @ModifyVariable} changes, or -2 when it isn't one the preflight can check. Reports
     * (when {@code problems} isn't null) a variable that's no longer among the arguments.
     */
    private static int modifiedSlot(MethodNode handler, AnnotationNode injector, Target target, List<String> problems) {
        if (!Boolean.TRUE.equals(value(injector, "argsOnly")) || value(injector, "index") != null) {
            if (problems != null)
                problems.add(handler.name + ": a @ModifyVariable that isn't argsOnly by type, which the preflight"
                    + " can't check yet");
            return -2;
        }
        Type[] handlerArgs = Type.getArgumentTypes(handler.desc);
        if (handlerArgs.length == 0)
            return -2;
        String type = handlerArgs[0].getDescriptor();
        int ordinal = integer(injector, "ordinal", -1);
        MethodNode method = target.method();
        int slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        int seen = 0;
        int chosen = -1;
        for (Type argument : Type.getArgumentTypes(method.desc)) {
            if (argument.getDescriptor().equals(type)) {
                if (ordinal < 0 ? seen == 0 : seen == ordinal)
                    chosen = slot;
                seen++;
            }
            slot += argument.getSize();
        }
        if (chosen < 0 || ordinal < 0 && seen != 1) {
            if (problems != null)
                problems.add(handler.name + ": the argument " + handlerArgs[0].getClassName() + " it changes isn't among"
                    + " the arguments of " + target.describe());
            return -1;
        }
        return chosen;
    }

    /**
     * An @Inject handler takes either no target arguments or all of them, in order, before its callback. (The target can
     * itself take a callback, when it's another mod's handler, so the callback is found by position, not by type.)
     */
    private static void checkCapturedArguments(MethodNode handler, Target target, List<String> problems) {
        Type[] handlerArgs = Type.getArgumentTypes(handler.desc);
        Type[] targetArgs = Type.getArgumentTypes(target.method().desc);
        if (handlerArgs.length > 0 && isCallback(handlerArgs[0]))
            return;
        boolean same = handlerArgs.length > targetArgs.length && isCallback(handlerArgs[targetArgs.length]);
        for (int i = 0; same && i < targetArgs.length; i++)
            same = handlerArgs[i].equals(targetArgs[i]);
        if (!same)
            problems.add(handler.name + ": the arguments it takes no longer match " + target.describe());
    }

    private static boolean isCallback(Type type) {
        return type.getDescriptor().equals(CALLBACK_INFO) || type.getDescriptor().equals(CALLBACK_INFO_RETURNABLE);
    }

    // --- reading ---------------------------------------------------------------------------------------------------

    /** A class as Mixin will see it (access transformers applied, no mixins yet), or null when it can't be read. */
    private ClassNode read(String internalName) {
        if (classes.containsKey(internalName))
            return classes.get(internalName);
        ClassNode node = null;
        try {
            node = source.read(internalName);
        } catch (Exception | LinkageError e) {
            unreadable.put(internalName, e.toString());
        }
        classes.put(internalName, node);
        return node;
    }

    private FieldNode findField(List<ClassNode> targets, String name, String desc, boolean inherited) {
        for (ClassNode target : targets)
            for (ClassNode at = target; at != null; at = inherited ? superOf(at) : null)
                for (FieldNode field : at.fields)
                    if (field.name.equals(name) && field.desc.equals(desc))
                        return field;
        return null;
    }

    private MethodNode findMethod(List<ClassNode> targets, String name, String desc, boolean inherited) {
        for (ClassNode target : targets)
            for (ClassNode at = target; at != null; at = inherited ? superOf(at) : null)
                for (MethodNode method : at.methods)
                    if (method.name.equals(name) && method.desc.equals(desc))
                        return method;
        return null;
    }

    private ClassNode superOf(ClassNode node) {
        return node.superName == null || node.superName.startsWith("java/") ? null : read(node.superName);
    }

    private static AbstractInsnNode firstReal(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions)
            if (insn.getOpcode() >= 0)
                return insn;
        return null;
    }

    // --- annotations -------------------------------------------------------------------------------------------------

    private static List<AnnotationNode> all(List<AnnotationNode> invisible, List<AnnotationNode> visible) {
        List<AnnotationNode> out = new ArrayList<>();
        if (invisible != null)
            out.addAll(invisible);
        if (visible != null)
            out.addAll(visible);
        return out;
    }

    private static AnnotationNode annotation(List<AnnotationNode> invisible, List<AnnotationNode> visible, String desc) {
        return find(all(invisible, visible), desc);
    }

    private static AnnotationNode find(List<AnnotationNode> annotations, String desc) {
        for (AnnotationNode annotation : annotations)
            if (annotation.desc.equals(desc))
                return annotation;
        return null;
    }

    private static AnnotationNode parameterAnnotation(MethodNode method, int index, String desc) {
        for (List<AnnotationNode>[] lists : List.of(
            method.invisibleParameterAnnotations == null ? new List[0] : method.invisibleParameterAnnotations,
            method.visibleParameterAnnotations == null ? new List[0] : method.visibleParameterAnnotations))
            if (index < lists.length && lists[index] != null) {
                AnnotationNode found = find(lists[index], desc);
                if (found != null)
                    return found;
            }
        return null;
    }

    /** An annotation's name/value pairs; empty for one with no attributes set (ASM leaves those null). */
    private static List<Object> vals(AnnotationNode annotation) {
        return annotation.values == null ? List.of() : annotation.values;
    }

    private static Object value(AnnotationNode annotation, String key) {
        if (annotation.values != null)
            for (int i = 0; i < annotation.values.size(); i += 2)
                if (annotation.values.get(i).equals(key))
                    return annotation.values.get(i + 1);
        return null;
    }

    private static String string(AnnotationNode annotation, String key, String fallback) {
        return value(annotation, key) instanceof String text ? text : fallback;
    }

    private static int integer(AnnotationNode annotation, String key, int fallback) {
        return value(annotation, key) instanceof Integer number ? number : fallback;
    }

    private static List<Object> list(AnnotationNode annotation, String key) {
        Object found = value(annotation, key);
        if (found instanceof List<?> several)
            return new ArrayList<>(several);
        return found == null ? List.of() : List.of(found);
    }

    private static String describeAt(AnnotationNode at) {
        String target = string(at, "target", "");
        int ordinal = integer(at, "ordinal", -1);
        List<Object> args = list(at, "args");
        return "@At(" + string(at, "value", "?") + (target.isEmpty() ? "" : " " + target)
            + (args.isEmpty() ? "" : " " + args) + (ordinal < 0 ? "" : " #" + ordinal) + ")";
    }

    // --- names -----------------------------------------------------------------------------------------------------

    private static String describe(List<Target> methods) {
        List<String> out = new ArrayList<>();
        for (Target target : methods)
            if (!out.contains(target.describe()))
                out.add(target.describe());
        return String.join(", ", out);
    }

    private static String names(List<ClassNode> classes) {
        List<String> out = new ArrayList<>();
        for (ClassNode node : classes)
            out.add(simple(dotted(node.name)));
        return String.join(", ", out);
    }

    private static String internal(String name) {
        return name.replace('.', '/');
    }

    private static String dotted(String internalName) {
        return internalName.replace('/', '.');
    }

    private static String simple(String dottedName) {
        return dottedName.substring(dottedName.lastIndexOf('.') + 1);
    }
}
