package net.xlea99.lifeguard.entry.cc_crafted_tether_potions;

import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Caverns & Chasms' tether, impact and trail potions crafted, not brewed ({@link CcCraftedTetherPotions}): any potion,
 * spinel and string, shapeless ({@link TetherPotionRecipe}). Each rig matches a kind of bottle: a drinkable potion makes
 * a tether potion, a splash potion an impact potion, a lingering one a trail potion, keeping the potion as it is. They're
 * potions strapped into a rig, not a kind of potion, so C&C's brewing routes go (mixin/cc_crafted_tether_potions): the
 * stand stops taking a rig as a bottle, so a rig is never changed once it's made.
 */
public final class CraftedTetherPotions {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Lifeguard.MODID);
    public static final Supplier<RecipeSerializer<TetherPotionRecipe>> SERIALIZER =
        SERIALIZERS.register("cc_tether_potion", () -> new SimpleCraftingRecipeSerializer<>(TetherPotionRecipe::new));

    private CraftedTetherPotions() {
    }

    /** The recipe type, whenever Caverns & Chasms is installed (the switch gates the recipe file). */
    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    /** The rig made from this bottle, or null if it isn't one of the three. */
    @Nullable
    public static Item rigOf(Item bottle) {
        return bottle == Items.POTION ? CCItems.TETHER_POTION.get()
            : bottle == Items.SPLASH_POTION ? CCItems.IMPACT_POTION.get()
            : bottle == Items.LINGERING_POTION ? CCItems.TRAIL_POTION.get() : null;
    }

    public static boolean isRig(Item item) {
        return item == CCItems.TETHER_POTION.get() || item == CCItems.IMPACT_POTION.get() || item == CCItems.TRAIL_POTION.get();
    }
}
