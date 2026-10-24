package net.xlea99.lifeguard.entry.sexy_runic_glints;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import javax.annotation.Nullable;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Whether an Iris shaderpack is in use. Shaderpacks draw the world through their own programs, which replace vanilla's
 * shaders but know nothing of {@link GlintShader}, so its glints vanish; while one is on, every rune glint falls back to
 * plain Quark rendering (see {@link GlintShards}, {@link GlintVariants}). Asked live, since shaderpacks can be switched
 * mid-game. Iris's public API is looked up by name, so it isn't required.
 */
public final class Shaderpacks {
    @Nullable
    private static final MethodHandle IS_SHADER_PACK_IN_USE = find();

    private Shaderpacks() {
    }

    public static boolean active() {
        if (IS_SHADER_PACK_IN_USE == null)
            return false;
        try {
            return (boolean) IS_SHADER_PACK_IN_USE.invoke();
        } catch (Throwable e) {
            return false;
        }
    }

    /** {@code IrisApi.getInstance().isShaderPackInUse()}, bound to the instance, or null without Iris. */
    @Nullable
    private static MethodHandle find() {
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            MethodHandle handle = MethodHandles.publicLookup()
                .findVirtual(api, "isShaderPackInUse", MethodType.methodType(boolean.class))
                .bindTo(instance);
            Lifeguard.LOGGER.info("Iris found: rune glints fall back to plain ones while a shaderpack is on");
            return handle;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable e) {
            Lifeguard.LOGGER.warn("Lifeguard couldn't reach Iris's API; rune glints may vanish under shaderpacks", e);
            return null;
        }
    }
}
