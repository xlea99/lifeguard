package net.xlea99.lifeguard.mixin.supps_pedestal_compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.PedestalBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PedestalBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.xlea99.lifeguard.Lifeguard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Items in {@code #lifeguard:pedestal_turned} get a quarter turn on a pedestal: Alex's Caves' raygun lies level, grip
 * down, instead of standing on its barrel.
 *
 * <p>A pedestal draws what it holds the way an item frame does, and a mod can turn a 3D item for item frames so it lies
 * flat against the wall (Alex's Caves does with its raygun): on a pedestal, that stands it on end. Supplementaries' own
 * pose tags ({@code #supplementaries:pedestal_upright}/{@code pedestal_downright}) lean an item's flat sprite, which
 * doesn't help a 3D model; this turns the item itself, just on a pedestal (item frames keep the mod's pose).
 */
@Mixin(PedestalBlockTileRenderer.class)
public abstract class PedestalBlockTileRendererMixin {

    @Unique
    private static final TagKey<Item> lifeguard$TURNED =
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "pedestal_turned"));

    @Inject(method = "render(Lnet/mehvahdjukaar/supplementaries/common/block/tiles/PedestalBlockTile;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V"))
    private void lifeguard$turn(PedestalBlockTile tile, float partialTicks, PoseStack pose, MultiBufferSource buffer,
                                int light, int overlay, CallbackInfo ci) {
        if (tile.getDisplayedItem().is(lifeguard$TURNED))
            pose.mulPose(Axis.ZN.rotationDegrees(90));
    }
}
