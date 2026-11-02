package net.xlea99.lifeguard.mixin.zetter_copies_like_books;

import me.dantaeusb.zetter.core.ZetterItems;
import me.dantaeusb.zetter.item.PaintingItem;
import me.dantaeusb.zetter.item.crafting.CopyingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.zetter_copies_like_books.ZetterCopiesLikeBooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The copy recipe refuses a painting of generation 2 (Copy of copy). See {@link ZetterCopiesLikeBooks}. */
@Mixin(CopyingRecipe.class)
public abstract class CopyingRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
        at = @At("RETURN"), cancellable = true)
    private void lifeguard$noCopyOfCopyOfCopy(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !ZetterCopiesLikeBooks.ENTRY.active())
            return;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ZetterItems.PAINTING.get()) && PaintingItem.getGeneration(stack) >= 2) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
