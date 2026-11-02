package net.xlea99.lifeguard.mixin.zetter_hovered_paintings_stack;

import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.item.CanvasItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zetter_hovered_paintings_stack.ZetterHoveredPaintingsStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CanvasItem.getResolution becomes read-only: a stack without CachedResolution gets the default WITHOUT it being written
 * into the stack. Paintings and canvases store their real resolution when they're made; what's left without one is a
 * blank combined canvas (the artist table stores only its size), which the tooltip used to change on hover.
 * See {@link ZetterHoveredPaintingsStack}.
 */
@Mixin(CanvasItem.class)
public abstract class CanvasItemMixin {
    @Inject(method = "getResolution(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$readOnly(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!ZetterHoveredPaintingsStack.ENTRY.active())
            return;
        CompoundTag tag = Helper.getTag(stack);
        if (tag != null && !tag.contains(CanvasItem.NBT_TAG_CACHED_RESOLUTION))
            cir.setReturnValue(Helper.getResolution().getNumeric());
    }
}
