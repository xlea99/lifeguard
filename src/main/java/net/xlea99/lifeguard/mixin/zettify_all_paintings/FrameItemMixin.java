package net.xlea99.lifeguard.mixin.zettify_all_paintings;

import me.dantaeusb.zetter.item.FrameItem;
import me.dantaeusb.zetter.item.PaintingItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zettify_all_paintings.ZettifyAllPaintings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A framed virtual painting is named like the unframed one (PaintingItemMixin): FrameItem overrides getName with its
 * own, which runs the stored title through translation as a key, so a mod that ships no title showed the raw key
 * (user, 2026-11-01: {@code painting.alexsmobs.nft.title}). Always on while Zetter is installed. See
 * {@link ZettifyAllPaintings}.
 */
@Mixin(FrameItem.class)
public abstract class FrameItemMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void lifeguard$virtualPaintingTitle(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        ResourceLocation variant = VirtualPaintings.variant(PaintingItem.getPaintingCode(stack));
        if (variant != null)
            cir.setReturnValue(Component.translatableWithFallback(VirtualPaintings.titleKey(variant),
                VirtualPaintings.humanize(variant)));
    }
}
