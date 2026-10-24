package net.xlea99.lifeguard.mixin.supps_plunderer_raid_weapon;

import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Plunderer buffed for a later raid wave gets an enchanted golden sword, not a crossbow. Supplementaries copied the
 * Pillager's raid buff, which swaps the weapon for a Quick Charge crossbow; the Plunderer has no ranged attack, so it
 * clubbed you with it. Same odds and waves as upstream (enchant odds by difficulty, waves after the third), with the
 * Vindicator's raid enchantments, vanilla's melee raider.
 */
@Mixin(PlundererEntity.class)
public abstract class PlundererEntityMixin {

    @Inject(method = "applyRaidBuffs", at = @At("HEAD"), cancellable = true)
    private void lifeguard$swordNotCrossbow(ServerLevel level, int wave, boolean unused, CallbackInfo ci) {
        ci.cancel();
        PlundererEntity self = (PlundererEntity) (Object) this;
        Raid raid = self.getCurrentRaid();
        if (raid == null || self.getRandom().nextFloat() > raid.getEnchantOdds() || wave <= raid.getNumGroups(Difficulty.EASY))
            return;
        ResourceKey<EnchantmentProvider> provider = wave > raid.getNumGroups(Difficulty.NORMAL)
            ? VanillaEnchantmentProviders.RAID_VINDICATOR_POST_WAVE_5
            : VanillaEnchantmentProviders.RAID_VINDICATOR;
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        EnchantmentHelper.enchantItemFromProvider(sword, level.registryAccess(), provider,
            level.getCurrentDifficultyAt(self.blockPosition()), self.getRandom());
        self.setItemSlot(EquipmentSlot.MAINHAND, sword);
    }
}
