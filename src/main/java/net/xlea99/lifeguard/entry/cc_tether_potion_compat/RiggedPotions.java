package net.xlea99.lifeguard.entry.cc_tether_potion_compat;

import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import com.teamabnormals.caverns_and_chasms.core.registry.CCSoundEvents;
import javax.annotation.Nullable;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Caverns & Chasms' tether, impact and trail potions: a potion rigged up with spinel to be worn (tether), to break over
 * you when you're hit (impact) or to leave a trail of clouds (trail). Each matches a kind of bottle: tether a drinkable
 * potion, impact a splash potion, trail a lingering one. Moonlight's containers only know the three bottles, so a jar,
 * goblet or cauldron refused the rigged ones; here one is broken open into the container instead: it pours in as its
 * bottle would (same capacity and mixing rules), a glass bottle comes back, and the spinel is lost.
 */
public final class RiggedPotions {

    private RiggedPotions() {
    }

    /** The bottle a rigged potion holds (same potion, one bottle), or null if it isn't one. */
    @Nullable
    public static ItemStack brokenOpen(ItemStack held) {
        Item bottle = held.is(CCItems.TETHER_POTION.get()) ? Items.POTION
            : held.is(CCItems.IMPACT_POTION.get()) ? Items.SPLASH_POTION
            : held.is(CCItems.TRAIL_POTION.get()) ? Items.LINGERING_POTION : null;
        return bottle == null ? null : new ItemStack(bottle.builtInRegistryHolder(), 1, held.getComponentsPatch());
    }

    /**
     * A rigged potion used on a Moonlight tank (a jar, a goblet, an Amendments cauldron): broken open into it. False when
     * it isn't one or doesn't fit, so the click carries on as before.
     */
    public static boolean breakIntoTank(SoftFluidTank tank, Player player, InteractionHand hand, Level level, BlockPos pos) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack potion = brokenOpen(held);
        if (potion == null || !tank.drainItem(potion, level, pos, true, false).getResult().consumesAction())
            return false;
        if (!level.isClientSide) {
            InteractionResultHolder<ItemStack> poured = tank.drainItem(potion, level, pos, false, false);
            smashed(player, hand, held, poured.getObject(), level, pos);
        }
        return true;
    }

    /** Hands back what's left of the rigged potion and plays it breaking. */
    public static void smashed(Player player, InteractionHand hand, ItemStack held, ItemStack emptied, Level level, BlockPos pos) {
        player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, emptied));
        level.playSound(null, pos, CCSoundEvents.TETHER_POTION_BREAK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
