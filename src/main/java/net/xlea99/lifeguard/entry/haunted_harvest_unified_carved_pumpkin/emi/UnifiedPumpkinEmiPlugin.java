package net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.HauntedHarvestUnifiedCarvedPumpkin;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.UnifiedPumpkin;

/** Shearing a pumpkin carves Haunted Harvest's pumpkin, vanilla's face ({@link UnifiedPumpkin}), not vanilla's, in EMI too. */
@EmiEntrypoint
public final class UnifiedPumpkinEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!HauntedHarvestUnifiedCarvedPumpkin.EMI.active())
            return;
        registry.removeRecipes(ResourceLocation.fromNamespaceAndPath("emi", "/world/shearing/minecraft/pumpkin"));
        registry.addRecipe(EmiWorldInteractionRecipe.builder()
            .id(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "/world/shearing/pumpkin"))
            .leftInput(EmiStack.of(Items.PUMPKIN))
            .rightInput(EmiStack.of(Items.SHEARS), true)
            .output(EmiStack.of(UnifiedPumpkin.hauntedItem(new ItemStack(Items.CARVED_PUMPKIN), UnifiedPumpkin.VANILLA_FACE)))
            .output(EmiStack.of(Items.PUMPKIN_SEEDS, 4))
            .build());
    }
}
