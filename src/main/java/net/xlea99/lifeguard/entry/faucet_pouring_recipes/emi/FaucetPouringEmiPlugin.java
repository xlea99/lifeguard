package net.xlea99.lifeguard.entry.faucet_pouring_recipes.emi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiReloadManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SugarBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.BrokenEntries;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.ClientPouringRecipes;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.FaucetPouringRecipes;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.PouringRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * The faucet pouring category in EMI: every recipe the server sent, plus Supplementaries' concrete powder hardening,
 * which stays its own code (it asks each powder what it hardens into, so modded powders work with no recipe) but shows
 * here alongside the data recipes, one row per concrete powder in the game. Found by EMI; does nothing unless the
 * entry's EMI part is active.
 */
@EmiEntrypoint
public final class FaucetPouringEmiPlugin implements EmiPlugin {
    /** Recipes for any fluid at all (a sponge soaking up anything) go last; the rest keep their order. */
    static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
        ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "faucet_pouring"), EmiStack.of(ModRegistry.FAUCET.get()),
        EmiStack.of(ModRegistry.FAUCET.get()), Comparator.comparing(recipe -> recipe instanceof FaucetPouringEmiRecipe pouring && pouring.anyFluid()));

    /** EMI may build its recipes before the server's arrive on joining; rebuild once they do. */
    public static void reload() {
        if (EmiReloadManager.isLoaded())
            EmiReloadManager.reloadRecipes();
    }

    @Override
    public void register(EmiRegistry registry) {
        if (!FaucetPouringRecipes.EMI.active())
            return;
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ModRegistry.FAUCET.get()));

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        // Only fluids this pack really has: Supplementaries defines some for mods that may not be installed (Farmer's
        // Respite's teas), which Moonlight keeps registered but disabled.
        List<Holder<SoftFluid>> allFluids = level.registryAccess().registryOrThrow(SoftFluidRegistry.KEY).holders()
            .filter(fluid -> fluid.value().isEnabled() && !MLBuiltinSoftFluids.EMPTY.is(fluid))
            .<Holder<SoftFluid>>map(fluid -> fluid).toList();
        List<String> files = ClientPouringRecipes.json();
        for (int i = 0; i < files.size(); i++) {
            JsonObject file = JsonParser.parseString(files.get(i)).getAsJsonObject();
            PouringRecipe recipe = PouringRecipe.CODEC.parse(ops, file).result().orElse(null);
            Target target = target(file.getAsJsonObject("target"));
            if (recipe == null || target == null)
                continue;
            List<Holder<SoftFluid>> fluids = recipe.fluid().map(set -> set.stream().filter(fluid -> fluid.value().isEnabled()).toList())
                .orElse(allFluids);
            if (fluids.isEmpty())
                continue;
            registry.addRecipe(new FaucetPouringEmiRecipe(
                ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "/faucet_pouring/" + i), target.input(),
                fluids, recipe.fluid().isEmpty(), recipe.amount(), List.of(EmiStack.of(recipe.result().getBlock())), target.chance()));
        }

        // One row for every concrete powder: EMI cycles both slots on the same clock, so with the powders and their
        // concretes in the same order, each powder shows beside its own concrete.
        List<EmiStack> powders = new ArrayList<>();
        List<EmiStack> concretes = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            // Supplementaries' sugar cube is a "powder" whose concrete is water (it dissolves); the faucet checks it
            // first and dissolves it, as here.
            if (!(block instanceof ConcretePowderBlock powder) || block instanceof SugarBlock)
                continue;
            Block concrete;
            try {
                concrete = ObfuscationReflectionHelper.getPrivateValue(ConcretePowderBlock.class, powder, "concrete");
            } catch (RuntimeException e) {
                BrokenEntries.mark(FaucetPouringRecipes.EMI, "ConcretePowderBlock.concrete can't be read (" + e
                    + "), so the powder-to-concrete pours can't be shown");
                return;
            }
            powders.add(EmiStack.of(block));
            concretes.add(EmiStack.of(concrete));
        }
        Holder<SoftFluid> water = MLBuiltinSoftFluids.WATER.getHolder(level);
        if (!powders.isEmpty())
            registry.addRecipe(new FaucetPouringEmiRecipe(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "/faucet_pouring/concrete"),
                EmiIngredient.of(powders), List.of(water), false, 1, concretes, null));
        // Also Supplementaries' code: water dissolves a sugar cube, leaving nothing.
        registry.addRecipe(new FaucetPouringEmiRecipe(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "/faucet_pouring/sugar_cube"),
            EmiStack.of(ModRegistry.SUGAR_CUBE.get()), List.of(water), false, 1, List.of(), null));
    }

    private record Target(EmiIngredient input, @Nullable Float chance) {
    }

    /** What a recipe's target rule test matches, as EMI stacks; null for a rule test this can't show. */
    @Nullable
    private static Target target(JsonObject test) {
        String type = test.get("predicate_type").getAsString();
        type = type.contains(":") ? type : "minecraft:" + type;
        Float chance = test.has("probability") ? test.get("probability").getAsFloat() : null;
        return switch (type) {
            case "minecraft:block_match", "minecraft:random_block_match" -> new Target(block(test.get("block").getAsString()), chance);
            case "minecraft:blockstate_match", "minecraft:random_blockstate_match" ->
                new Target(block(test.getAsJsonObject("block_state").get("Name").getAsString()), chance);
            case "minecraft:tag_match" -> {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(test.get("tag").getAsString()));
                List<EmiStack> blocks = BuiltInRegistries.BLOCK.getTag(tag).stream().flatMap(set -> set.stream())
                    .map(holder -> EmiStack.of(holder.value())).filter(stack -> !stack.isEmpty()).toList();
                yield new Target(EmiIngredient.of(blocks), chance);
            }
            default -> null;
        };
    }

    private static EmiStack block(String id) {
        return EmiStack.of(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)));
    }
}
