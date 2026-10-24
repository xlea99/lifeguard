package net.xlea99.lifeguard.mixin.quark_donkey_chests;

import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.quark_donkey_chests.QuarkDonkeyChests;
import net.xlea99.lifeguard.entry.quark_donkey_chests.WornChests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps Quark's chest variants on donkeys, mules and llamas exact (see {@link WornChests}): the chest a dying animal
 * wears drops as the Quark chest it is, in place of Quark's own swap ({@link VariantChestsModuleMixin}). And the record
 * of which Quark chest it wore always matches the chest it wears: taking the chest off, by any route, forgets it, and so
 * does putting on a plain one, so a plain chest put on later can't drop as a Quark chest it no longer wears.
 */
@Mixin(AbstractChestedHorse.class)
public abstract class AbstractChestedHorseMixin {

    // Drops the worn Quark chest up front and takes the chest off, so vanilla's own chest drop (and every mod's change
    // to it: Quark's swap, Woodworks' worn-chest drop) finds no chest and drops nothing. Not a @Redirect on that drop:
    // Woodworks redirects the same call, and two redirects on one call crash the game.
    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void lifeguard$dropWornChest(CallbackInfo ci) {
        AbstractChestedHorse horse = (AbstractChestedHorse) (Object) this;
        if (!QuarkDonkeyChests.ENTRY.active() || horse.level().isClientSide)
            return;
        ItemStack worn = WornChests.worn(horse);
        if (worn.isEmpty())
            return;
        horse.spawnAtLocation(worn);
        horse.setChest(false);
    }

    @Inject(method = "setChest", at = @At("HEAD"))
    private void lifeguard$forgetRemovedChest(boolean chested, CallbackInfo ci) {
        if (!chested && QuarkDonkeyChests.ENTRY.active())
            WornChests.forget((AbstractChestedHorse) (Object) this);
    }

    @Inject(method = "equipChest", at = @At("HEAD"))
    private void lifeguard$forgetForPlainChest(Player player, ItemStack chest, CallbackInfo ci) {
        if (QuarkDonkeyChests.ENTRY.active())
            WornChests.forget((AbstractChestedHorse) (Object) this);
    }
}
