package net.xlea99.lifeguard;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.boot.Boot;
import net.xlea99.lifeguard.sync.Sync;
import org.slf4j.Logger;

@Mod(Lifeguard.MODID)
public class Lifeguard {
    public static final String MODID = "lifeguard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lifeguard(IEventBus modEventBus, ModContainer modContainer) {
        // The mixin plugin normally got here first; this only matters if it didn't.
        Boot.start();

        modEventBus.addListener(Sync::registerPayloads);
        modEventBus.addListener(Sync::registerTasks);
        NeoForge.EVENT_BUS.addListener(Sync::onServerAboutToStart);
    }
}
