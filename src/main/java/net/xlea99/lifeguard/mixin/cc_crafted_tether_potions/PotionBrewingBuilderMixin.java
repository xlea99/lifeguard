package net.xlea99.lifeguard.mixin.cc_crafted_tether_potions;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.cc_crafted_tether_potions.CcCraftedTetherPotions;
import net.xlea99.lifeguard.entry.cc_crafted_tether_potions.CraftedTetherPotions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caverns & Chasms brews its tether, impact and trail potions (CCMobEffects.registerBrewingRecipes): each rig is a
 * brewing "container" (a bottle the stand takes), made from its bottle with spinel and from each other (tether +
 * gunpowder = impact, impact + dragon's breath = trail). Crafted instead ({@link CraftedTetherPotions}), they leave the
 * brewing list just before it's built, after every mod has added to it: no rig recipes, and no rig as a bottle, so the
 * stand refuses them (vanilla's slot checks only take bottles on the list) and a rig is never changed once made. The
 * list is built on each side, the client's on joining, after the server's switches have arrived.
 */
@Mixin(PotionBrewing.Builder.class)
public abstract class PotionBrewingBuilderMixin {
    @Shadow
    @Final
    private List<Ingredient> containers;
    @Shadow
    @Final
    private List<Object> containerMixes;

    @Inject(method = "build", at = @At("HEAD"))
    private void lifeguard$noRigBrewing(CallbackInfoReturnable<PotionBrewing> cir) {
        if (!CcCraftedTetherPotions.ENTRY.active())
            return;
        int bottles = containers.size();
        int mixes = containerMixes.size();
        containers.removeIf(container -> {
            for (ItemStack stack : container.getItems())
                if (CraftedTetherPotions.isRig(stack.getItem()))
                    return true;
            return false;
        });
        containerMixes.removeIf(mix -> {
            PotionBrewingMixAccessor accessor = (PotionBrewingMixAccessor) mix;
            return CraftedTetherPotions.isRig((Item) accessor.lifeguard$from().value())
                || CraftedTetherPotions.isRig((Item) accessor.lifeguard$to().value());
        });
        Lifeguard.LOGGER.info("Lifeguard: tether, impact and trail potions are crafted, not brewed ({} brewing recipes and {} "
            + "bottles taken out)", mixes - containerMixes.size(), bottles - containers.size());
    }
}
