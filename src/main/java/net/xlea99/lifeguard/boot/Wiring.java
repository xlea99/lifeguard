package net.xlea99.lifeguard.boot;

import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;
import net.xlea99.lifeguard.core.Entry;

/**
 * What an {@link EntrySetup} hands its code to, saying when it runs. Every call is decided once, as the mod is
 * constructed: switches change only with a restart.
 */
public final class Wiring {
    private final IEventBus modBus;

    Wiring(IEventBus modBus) {
        this.modBus = modBus;
    }

    /** Behavior: runs only when the entry is active this session. Off, none of it exists. */
    public void whenActive(Entry entry, Runnable setup) {
        if (entry.active())
            setup.run();
    }

    public void whenActive(Entry entry, Consumer<IEventBus> setup) {
        whenActive(entry, () -> setup.accept(modBus));
    }

    /**
     * Runs whenever everything the entry needs is installed, whatever its switch: content a world may hold (registered
     * blocks and items must not vanish with a switch), or listeners that ask the switch themselves each time
     * (runtime-gated entries, whose switch the server can change for its world).
     */
    public void whenInstalled(Entry entry, Runnable setup) {
        if (entry.installed())
            setup.run();
    }

    public void whenInstalled(Entry entry, Consumer<IEventBus> setup) {
        whenInstalled(entry, () -> setup.accept(modBus));
    }

    /** Runs always: setup that serves several entries and checks each one's own entry itself. */
    public void always(Consumer<IEventBus> setup) {
        setup.accept(modBus);
    }
}
