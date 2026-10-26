package net.xlea99.lifeguard.mixin.map_atlases_fixes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.map_atlases_fixes.AtlasFixes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pepjebs.mapatlases.MapAtlasesMod;

/** Taking a merge of two atlases gives back the empty map the even split couldn't share ({@link AtlasFixes}). */
@Mixin(targets = "net.minecraft.world.inventory.CartographyTableMenu$5")
public abstract class CartographyResultSlotMixin {
    @Shadow
    @Final
    CartographyTableMenu this$0;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void lifeguard$returnMergeLeftovers(Player player, ItemStack result, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer)) // the server's inventory syncs; the client's guess would be a ghost item
            return;
        ItemStack atlas = this.this$0.slots.get(0).getItem();
        ItemStack other = this.this$0.slots.get(1).getItem();
        if (!atlas.is(MapAtlasesMod.MAP_ATLAS.get()) || !other.is(MapAtlasesMod.MAP_ATLAS.get()) || !result.is(MapAtlasesMod.MAP_ATLAS.get()))
            return;
        AtlasFixes.mergeLeftovers(atlas, other)
            .forEach((type, count) -> player.getInventory().placeItemBackInInventory(new ItemStack(type.getEmpty(), count)));
    }
}
