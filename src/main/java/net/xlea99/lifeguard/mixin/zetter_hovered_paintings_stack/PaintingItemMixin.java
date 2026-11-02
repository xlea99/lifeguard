package net.xlea99.lifeguard.mixin.zetter_hovered_paintings_stack;

import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.item.CanvasItem;
import me.dantaeusb.zetter.item.PaintingItem;
import me.dantaeusb.zetter.storage.PaintingData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zetter_hovered_paintings_stack.ZetterHoveredPaintingsStack;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A painting item stores its real resolution when it's made, as a canvas does. Virtual paintings are skipped: their
 * canonical form (x16) is zettify_all_paintings' to write, whichever hook runs last. See
 * {@link ZetterHoveredPaintingsStack}.
 */
@Mixin(PaintingItem.class)
public abstract class PaintingItemMixin {
    @Inject(method = "storePaintingData", at = @At("TAIL"))
    private static void lifeguard$storeRealResolution(ItemStack stack, String paintingCode, PaintingData paintingData,
                                                      int generation, CallbackInfo ci) {
        if (!ZetterHoveredPaintingsStack.ENTRY.active() || VirtualPaintings.isVirtual(paintingCode))
            return;
        CompoundTag tag = Helper.getOrCreateTag(stack);
        tag.putInt(CanvasItem.NBT_TAG_CACHED_RESOLUTION, paintingData.getResolution().getNumeric());
        Helper.setTag(stack, tag);
    }
}
