package net.xlea99.lifeguard.data;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.xlea99.lifeguard.Lifeguard;

/** Lifeguard's datapack conditions. */
public final class DataConditions {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
        DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Lifeguard.MODID);

    static {
        CONDITIONS.register("entry", () -> EntryCondition.CODEC);
    }

    private DataConditions() {
    }

    public static void register(IEventBus modEventBus) {
        CONDITIONS.register(modEventBus);
    }
}
