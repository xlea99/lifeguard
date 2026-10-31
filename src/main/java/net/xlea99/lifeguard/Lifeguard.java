package net.xlea99.lifeguard;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.boot.Boot;
import net.xlea99.lifeguard.boot.Setups;
import net.xlea99.lifeguard.data.DataConditions;
import net.xlea99.lifeguard.data.UsedIds;
import net.xlea99.lifeguard.magnet.Magnets;
import net.xlea99.lifeguard.sync.BrokenNotice;
import net.xlea99.lifeguard.sync.Sync;
import org.slf4j.Logger;

/** Lifeguard's entry point: the shared machinery, then every entry's own setup ({@link Setups}). */
@Mod(Lifeguard.MODID)
public class Lifeguard {
    public static final String MODID = "lifeguard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lifeguard(IEventBus modEventBus, ModContainer modContainer) {
        // The mixin plugin normally got here first; this only matters if it didn't.
        Boot.start();

        DataConditions.register(modEventBus);
        UsedIds.register(modEventBus);
        modEventBus.addListener(Sync::registerPayloads);
        Setups.runCommon(modEventBus);
        // Alex's Caves magnets for every block that is one (shared by several entries' metals): joins AC's after each
        // block's entry has said so.
        Magnets.register(modEventBus);
        modEventBus.addListener(Sync::registerTasks);
        NeoForge.EVENT_BUS.addListener(Sync::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(BrokenNotice::onLoggedIn);
    }
}
