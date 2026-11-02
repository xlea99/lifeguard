package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import me.dantaeusb.zetter.item.PaintingItem;
import me.dantaeusb.zetter.storage.PaintingData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A virtual painting's item (and its framed item: FrameItem extends PaintingItem) always holds one canonical form, and
 * is named by its painting's own title, translated where it's shown, not by the literal cached name (the title key).
 * Both always on while Zetter is installed, like the other safety hooks: items stay stackable with the switch off.
 * See {@link ZettifyAllPaintings}.
 */
@Mixin(PaintingItem.class)
public abstract class PaintingItemMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void lifeguard$virtualPaintingTitle(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        ResourceLocation variant = VirtualPaintings.variant(PaintingItem.getPaintingCode(stack));
        if (variant != null)
            cir.setReturnValue(Component.translatableWithFallback(VirtualPaintings.titleKey(variant),
                VirtualPaintings.humanize(variant)));
    }

    @Inject(method = "storePaintingData", at = @At("TAIL"))
    private static void lifeguard$virtualPaintingCanonical(ItemStack stack, String paintingCode, PaintingData paintingData,
                                                           int generation, CallbackInfo ci) {
        ResourceLocation variant = VirtualPaintings.variant(paintingCode);
        if (variant != null)
            VirtualPaintings.storeCanonical(stack, variant);
    }
}
