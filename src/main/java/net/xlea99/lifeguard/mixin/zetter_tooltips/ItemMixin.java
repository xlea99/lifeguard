package net.xlea99.lifeguard.mixin.zetter_tooltips;

import java.util.List;
import me.dantaeusb.zetter.item.CanvasItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.xlea99.lifeguard.entry.zetter_tooltips.ZetterTooltips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Item's default appendHoverText (empty) hands a Zetter canvas, painting or frame to Zetter's own 1.20.1-shaped one
 * (PaintingItem and FrameItem inherit CanvasItem; the call dispatches to the most specific). See {@link ZetterTooltips}.
 */
@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void lifeguard$zetterTooltips(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                          TooltipFlag flag, CallbackInfo ci) {
        if ((Object) this instanceof CanvasItem canvas)
            canvas.appendHoverText(stack, context.level(), tooltip, flag);
    }
}
