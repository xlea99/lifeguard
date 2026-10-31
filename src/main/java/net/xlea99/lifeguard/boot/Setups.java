package net.xlea99.lifeguard.boot;

import net.neoforged.bus.api.IEventBus;

/** Runs every entry's {@link EntrySetup}, found by name beside each declaration in the {@link Catalog}. */
public final class Setups {
    /** {@code <Declaration>Setup}: runs on both sides. */
    public static final String COMMON_SUFFIX = "Setup";
    /** {@code <Declaration>ClientSetup}: runs on the client only. */
    public static final String CLIENT_SUFFIX = "ClientSetup";

    private Setups() {
    }

    public static void runCommon(IEventBus modBus) {
        run(modBus, COMMON_SUFFIX);
    }

    /** Only ever called from the client entry point, so a server never loads a client setup class. */
    public static void runClient(IEventBus modBus) {
        run(modBus, CLIENT_SUFFIX);
    }

    private static void run(IEventBus modBus, String suffix) {
        Wiring wiring = new Wiring(modBus);
        for (Class<?> declarations : Catalog.declarations()) {
            String name = declarations.getName() + suffix;
            Class<?> setupClass;
            try {
                setupClass = Class.forName(name, true, declarations.getClassLoader());
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (!EntrySetup.class.isAssignableFrom(setupClass))
                throw new IllegalStateException(name + " is named like an entry setup but doesn't implement EntrySetup");
            try {
                ((EntrySetup) setupClass.getDeclaredConstructor().newInstance()).setup(wiring);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Lifeguard couldn't run " + name, e);
            }
        }
    }
}
