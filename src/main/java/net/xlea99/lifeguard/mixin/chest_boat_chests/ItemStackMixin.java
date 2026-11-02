package net.xlea99.lifeguard.mixin.chest_boat_chests;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.chest_boat_chests.BoatChest;
import org.spongepowered.asm.mixin.Mixin;

/**
 * A chest boat item placing its boat, whatever mod's item it is: every use goes through here, so the boat it adds takes
 * the item's chest. See {@link BoatChest}.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapMethod(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;")
    private InteractionResultHolder<ItemStack> lifeguard$placesWithChest(Level level, Player player, InteractionHand hand,
                                                                       Operation<InteractionResultHolder<ItemStack>> original) {
        String previous = BoatChest.startPlacing((ItemStack) (Object) this);
        try {
            return original.call(level, player, hand);
        } finally {
            BoatChest.endPlacing(previous);
        }
    }
}
