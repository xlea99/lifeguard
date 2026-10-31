package net.xlea99.lifeguard.boids;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.VariantHolder;

/**
 * What variant a fish is, for species that school only with their own variant ({@link BoidSpecies#sameVariant()}):
 * Rainbow Reef's tangs, butterflyfish and parrotfish do, so a blue tang never follows a yellow one. Read through
 * vanilla's {@link VariantHolder} when a mob has it, else through a public no-argument {@code variant()} or
 * {@code getVariant()} on its class (Rainbow Reef's, and how most mods name theirs), found once per class. A fish with
 * neither has no variant: it schools with every fish of its kind that has none either.
 */
public final class BoidVariants {

    private static final Map<Class<?>, Optional<MethodHandle>> READERS = new ConcurrentHashMap<>();

    private BoidVariants() {
    }

    /** The fish's variant, or null if it has none we can read. */
    @Nullable
    public static Object of(Entity fish) {
        if (fish instanceof VariantHolder<?> holder)
            return holder.getVariant();
        Optional<MethodHandle> reader = READERS.computeIfAbsent(fish.getClass(), BoidVariants::find);
        if (reader.isEmpty())
            return null;
        try {
            return reader.get()
                .invoke(fish);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Optional<MethodHandle> find(Class<?> type) {
        for (String name : new String[] {"variant", "getVariant"}) {
            MethodHandle reader = handle(type, name);
            if (reader != null && reader.type()
                .returnType() != void.class)
                return Optional.of(reader);
        }
        return Optional.empty();
    }

    /**
     * A public, non-static, no-argument method of this name on the class (inherited too), as a handle; null if there's
     * none. Made accessible, so one declared in a package-private base class still works.
     */
    @Nullable
    static MethodHandle handle(Class<?> type, String name) {
        try {
            Method method = type.getMethod(name);
            if (Modifier.isStatic(method.getModifiers()))
                return null;
            method.setAccessible(true);
            return MethodHandles.lookup()
                .unreflect(method);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

}
