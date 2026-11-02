package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.dantaeusb.zetter.item.PaintingItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The tooltip's "by <author>" for a virtual painting: the author key it stores, translated (empty when the mod ships
 * no author, so Zetter shows its own "Unknown"). Client only (appendHoverText is). See {@link ZettifyAllPaintings}.
 */
@Mixin(PaintingItem.class)
public abstract class PaintingItemTooltipMixin {
    @WrapOperation(method = "appendHoverText", at = @At(value = "INVOKE",
        target = "Lme/dantaeusb/zetter/item/PaintingItem;getCachedAuthorName(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;"))
    private String lifeguard$virtualPaintingAuthor(ItemStack stack, Operation<String> original) {
        ResourceLocation variant = VirtualPaintings.variant(PaintingItem.getPaintingCode(stack));
        if (variant == null)
            return original.call(stack);
        return Component.translatableWithFallback(VirtualPaintings.authorKey(variant), "").getString();
    }
}
