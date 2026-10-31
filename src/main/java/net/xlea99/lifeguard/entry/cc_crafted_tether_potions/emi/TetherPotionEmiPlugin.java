package net.xlea99.lifeguard.entry.cc_crafted_tether_potions.emi;

import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.cc_crafted_tether_potions.CcCraftedTetherPotions;
import net.xlea99.lifeguard.entry.cc_crafted_tether_potions.TetherPotionRecipe;

/**
 * The tether potion recipe in EMI, one recipe per potion and bottle, as EMI does for tipped arrows: EMI can't list a
 * recipe that works out its result from the grid. Found by EMI; does nothing unless the entry's EMI part is active.
 */
@EmiEntrypoint
public final class TetherPotionEmiPlugin implements EmiPlugin {
    private static final Map<Item, String> BOTTLES = Map.of(Items.POTION, "tether", Items.SPLASH_POTION, "impact",
        Items.LINGERING_POTION, "trail");

    @Override
    public void register(EmiRegistry registry) {
        if (!CcCraftedTetherPotions.EMI.active())
            return;
        EmiStack spinel = EmiStack.of(CCItems.SPINEL.get());
        EmiStack string = EmiStack.of(Items.STRING);
        for (Holder.Reference<Potion> potion : BuiltInRegistries.POTION.holders().toList())
            for (Map.Entry<Item, String> bottle : BOTTLES.entrySet()) {
                ItemStack input = PotionContents.createItemStack(bottle.getKey(), potion);
                ItemStack rigged = TetherPotionRecipe.rigged(input);
                ResourceLocation key = potion.key().location();
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID,
                    "/cc_tether_potion/" + bottle.getValue() + "/" + key.getNamespace() + "/" + key.getPath());
                registry.addRecipe(new EmiCraftingRecipe(List.of(EmiStack.of(input), spinel, string), EmiStack.of(rigged),
                    id, true));
            }
    }
}
