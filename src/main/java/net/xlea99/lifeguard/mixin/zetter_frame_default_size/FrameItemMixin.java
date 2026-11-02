package net.xlea99.lifeguard.mixin.zetter_frame_default_size;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.dantaeusb.zetter.item.FrameItem;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zetter_frame_default_size.ZetterFrameDefaultSize;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Frame placement gets 1x1 when the item's size is missing or malformed. Runs on both sides (useOn does), so it's
 * runtime-gated. See {@link ZetterFrameDefaultSize}.
 */
@Mixin(FrameItem.class)
public abstract class FrameItemMixin {
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE",
        target = "Lme/dantaeusb/zetter/item/FrameItem;getBlockSize(Lnet/minecraft/world/item/ItemStack;)[I"))
    private int[] lifeguard$defaultSize(ItemStack stack, Operation<int[]> original) {
        int[] size = original.call(stack);
        if (ZetterFrameDefaultSize.ENTRY.active() && (size == null || size.length != 2 || size[0] < 1 || size[1] < 1))
            return new int[]{1, 1};
        return size;
    }
}
