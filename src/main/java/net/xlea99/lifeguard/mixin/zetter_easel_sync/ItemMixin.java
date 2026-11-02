package net.xlea99.lifeguard.mixin.zetter_easel_sync;

import me.dantaeusb.zetter.core.ZetterItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.zetter_easel_sync.BlankCanvases;
import net.xlea99.lifeguard.entry.zetter_easel_sync.ZetterEaselSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A canvas sitting in an inventory (every way off an easel ends there: sneak-take, the screen's slot, shift-click,
 * picking up a broken easel's drop) is checked twice a second on the server and wiped back to a fresh blank canvas if
 * it got a code but was never painted. CanvasItem doesn't override inventoryTick, so Item's runs for it.
 * See {@link ZetterEaselSync}.
 */
@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void lifeguard$wipeUnpaintedCanvas(ItemStack stack, Level level, Entity entity, int slot, boolean selected,
                                               CallbackInfo ci) {
        if (level.isClientSide() || level.getGameTime() % 10 != 0 || !stack.is(ZetterItems.CANVAS.get())
            || !ZetterEaselSync.ENTRY.active())
            return;
        BlankCanvases.wipeIfBlank(stack, level);
    }
}
