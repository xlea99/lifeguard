package net.xlea99.lifeguard.entry.zettify_all_paintings;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Client setup for {@link ZettifyAllPaintings}: the creative tab entries (testing). */
public final class ZettifyAllPaintingsClientSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        // Runtime-gated: the listener asks the switch.
        wiring.whenInstalled(ZettifyAllPaintings.ENTRY,
            bus -> bus.addListener(BuildCreativeModeTabContentsEvent.class, ClientVirtualPaintings::addToCreativeTab));
    }
}
