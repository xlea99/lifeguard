package net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns;

import net.mehvahdjukaar.hauntedharvest.blocks.ModCarvedPumpkinBlock;
import net.mehvahdjukaar.hauntedharvest.blocks.PumpkinType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.ModList;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Haunted Harvest's soul, redstone and cupric jack o'lanterns without Autumnity ({@link HauntedHarvestTorchLanterns}).
 *
 * <p>Haunted Harvest has them all (blocks, models, textures, a torch for each: soul torch, redstone torch, Caverns &
 * Chasms' cupric torch, Endergetic's ender torch), but only registers them as its Autumnity compat. Without Autumnity,
 * {@code mixin.haunted_harvest_torch_lanterns.CompatHandlerMixin} runs that same registration, with the lantern types
 * filed as {@code hauntedharvest:autumnity/<name>} ({@code AutumnityCompatImplMixin}), which is how they're told apart
 * here. Haunted Harvest's loot tables for them only load with Autumnity, so ours (same drops, the carving kept) stand in.
 */
public final class TorchLanterns {
    /** Where the unlocked lantern types are filed in Haunted Harvest's namespace (and its texture folder for them). */
    public static final String FOLDER = "autumnity/";
    // Looked up on first use: vanilla asks every block for its loot table while bootstrapping, before the mod list exists.
    @Nullable
    private static Boolean autumnity;

    private TorchLanterns() {
    }

    /** True for a lantern type that exists only because of this entry. */
    public static boolean unlockedHere(PumpkinType type) {
        if (autumnity == null)
            autumnity = ModList.get().isLoaded("autumnity");
        if (autumnity)
            return false;
        ResourceLocation id = PumpkinType.REGISTRY.getKey(type);
        return id != null && id.getNamespace().equals("hauntedharvest") && id.getPath().startsWith(FOLDER);
    }

    /** Our stand-in loot table for one of the unlocked lanterns; null for any other block. */
    @Nullable
    public static ResourceKey<LootTable> lootTable(Block block) {
        if (!(block instanceof ModCarvedPumpkinBlock pumpkin) || !unlockedHere(pumpkin.getType(pumpkin.defaultBlockState()).value()))
            return null;
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "blocks/haunted_harvest/" + path));
    }
}
