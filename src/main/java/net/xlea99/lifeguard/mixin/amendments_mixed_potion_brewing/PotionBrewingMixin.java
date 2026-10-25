package net.xlea99.lifeguard.mixin.amendments_mixed_potion_brewing;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;

/**
 * A potion mixed in an Amendments cauldron brews into a splash or lingering potion with its effects, as it did on
 * 1.20.1. A mixed potion has effects but no base potion, and the brewing stand only carries a base potion over, so it
 * used up the gunpowder or dragon's breath and left the potion as it was. Here such a potion is brewed as if it were a
 * water bottle; when that only changes the bottle (a splash, a lingering potion), the new bottle gets its effects.
 * Anything that would change a water bottle's potion leaves the mixed potion alone, as before.
 */
@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    // Vanilla's names are swapped: the first stack is the ingredient, the second the bottle.
    @WrapMethod(method = "mix")
    private ItemStack lifeguard$brewMixed(ItemStack ingredient, ItemStack bottle, Operation<ItemStack> original) {
        ItemStack brewed = original.call(ingredient, bottle);
        PotionContents contents = bottle.get(DataComponents.POTION_CONTENTS);
        if (brewed != bottle || contents == null || contents.potion().isPresent() || !contents.hasEffects())
            return brewed;
        ItemStack water = bottle.copy();
        water.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        ItemStack asWater = original.call(ingredient, water);
        PotionContents asWaterContents = asWater.get(DataComponents.POTION_CONTENTS);
        if (asWater.isEmpty() || asWater.is(bottle.getItem()) || asWaterContents == null || !asWaterContents.is(Potions.WATER))
            return brewed;
        asWater.set(DataComponents.POTION_CONTENTS, contents);
        return asWater;
    }
}
