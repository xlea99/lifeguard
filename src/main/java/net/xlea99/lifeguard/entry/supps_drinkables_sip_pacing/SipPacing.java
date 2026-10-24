package net.xlea99.lifeguard.entry.supps_drinkables_sip_pacing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mehvahdjukaar.moonlight.api.fluids.FoodProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sipping from a goblet or a placed jar takes as long as drinking the drink itself. Both sips are instant, so pouring
 * bottles in and sipping them was a much faster way to drink than drinking the bottles, which matters most for an
 * endless drink like Alex's Caves' purple soda (whose bottle is slow to drink on purpose). After a sip, the player's
 * goblets and jars cool down together for the drink's own drinking time (shown on them in the hotbar), so neither, nor
 * switching between them, is ever faster than drinking the bottles. A jar carried in hand already drinks at that pace.
 */
public final class SipPacing {

    private SipPacing() {
    }

    /** Wraps a goblet's or placed jar's {@link SoftFluidTank#tryDrinkUpFluid}. */
    public static boolean sip(SoftFluidTank tank, Player player, Level level, Operation<Boolean> original) {
        if (!SuppsDrinkablesSipPacing.ENTRY.active())
            return original.call(tank, player, level);
        ItemCooldowns cooldowns = player.getCooldowns();
        if (cooldowns.isOnCooldown(ModRegistry.GOBLET.get().asItem()) || cooldowns.isOnCooldown(ModRegistry.JAR_ITEM.get()))
            return false;
        // Read before drinking: the last sip empties the tank.
        FoodProvider food = tank.isEmpty() ? FoodProvider.EMPTY : tank.getFluid().getFoodProvider();
        if (!original.call(tank, player, level))
            return false;
        int duration = food.isEmpty() ? 0 : new ItemStack(food.getFoodItem()).getUseDuration(player);
        if (duration > 0) {
            cooldowns.addCooldown(ModRegistry.GOBLET.get().asItem(), duration);
            cooldowns.addCooldown(ModRegistry.JAR_ITEM.get(), duration);
        }
        return true;
    }
}
