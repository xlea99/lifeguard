package net.xlea99.lifeguard.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantChestsModule;

/**
 * Where a chest item's chest is drawn: the single chest texture file its renderer uses. Three families in Deep End
 * (read 2026-10-31): Every Compat's (both its {@code q/} and {@code abnww/} chests are its own CompatChestBlock, drawn
 * from {@code everycomp:entity/chest/<the item's path>}, a texture it makes at launch), Quark's variant chests, and
 * Blueprint's (Woodworks', C&C's). Each mod's classes load only when it's installed.
 */
public final class ChestTextures {
    private ChestTextures() {
    }

    @Nullable
    public static ResourceLocation of(Item item) {
        if (!(item instanceof BlockItem blockItem))
            return null;
        Block block = blockItem.getBlock();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id.getNamespace().equals("everycomp") && block instanceof ChestBlock)
            return ResourceLocation.fromNamespaceAndPath("everycomp", "textures/entity/chest/" + id.getPath() + ".png");
        ResourceLocation texture = ModList.get().isLoaded("quark") ? QuarkChests.of(block) : null;
        if (texture == null && ModList.get().isLoaded("blueprint"))
            texture = BlueprintChests.of(block);
        return texture;
    }

    /** As Quark's VariantChestRenderer: {@code quark:<folder>/<path>/normal} in the chest atlas. */
    private static final class QuarkChests {
        @Nullable
        static ResourceLocation of(Block block) {
            return block instanceof VariantChestsModule.IVariantChest chest
                ? Quark.asResource("textures/" + chest.getTextureFolder() + "/" + chest.getTexturePath() + "/normal.png") : null;
        }
    }

    /** As Blueprint's BlueprintChestBlockEntityRenderer: the materials registered under the block's chest name. */
    private static final class BlueprintChests {
        @Nullable
        static ResourceLocation of(Block block) {
            if (!(block instanceof com.teamabnormals.blueprint.core.api.IChestBlock chest))
                return null;
            var materials = com.teamabnormals.blueprint.client.BlueprintChestMaterials.getMaterials(chest.getChestMaterialsName());
            if (materials == null)
                return null;
            ResourceLocation texture = materials.singleMaterial().texture();
            return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
        }
    }
}
