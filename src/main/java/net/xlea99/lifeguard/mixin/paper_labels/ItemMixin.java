package net.xlea99.lifeguard.mixin.paper_labels;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.xlea99.lifeguard.entry.paper_labels.LabelPlacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Paper used on a block places a label ({@link LabelPlacing}). */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void lifeguard$paperPlacesLabel(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this == Items.PAPER)
            cir.setReturnValue(LabelPlacing.place(context));
    }
}
