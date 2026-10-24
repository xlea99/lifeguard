package net.xlea99.lifeguard.entry.faucet_pouring_recipes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xlea99.lifeguard.Lifeguard;

/**
 * The loaded faucet pouring recipes ({@code data/<namespace>/faucet_pouring/*.json}, see {@link PouringRecipe}), and
 * the faucet target that applies them. The target goes in front of Supplementaries' own (FaucetBehaviorsManagerMixin),
 * so a recipe can override a built-in; Supplementaries' hardcoded dirt-to-mud and sponge targets are switched off and
 * ship as recipes instead. The recipes are sent to each client (for recipe viewers) on join and on every reload. Only
 * loaded when the entry is active, so only with Supplementaries installed.
 */
public final class PouringRecipes extends SimpleJsonResourceReloadListener implements FaucetTarget.BlState {
    public static final PouringRecipes TARGET = new PouringRecipes();
    private static final Codec<Optional<PouringRecipe>> CONDITIONAL = ConditionalOps.createConditionalCodec(PouringRecipe.CODEC);

    private volatile List<PouringRecipe> recipes = List.of();
    /** The files behind {@link #recipes} (their conditions passed), as sent to clients. */
    private volatile List<String> json = List.of();

    private PouringRecipes() {
        super(new Gson(), "faucet_pouring");
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(TARGET));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            PouringRecipesPayload payload = new PouringRecipesPayload(TARGET.json);
            event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
        });
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        var ops = makeConditionalOps();
        List<PouringRecipe> loaded = new ArrayList<>();
        List<String> loadedJson = new ArrayList<>();
        new TreeMap<>(files).forEach((id, file) -> CONDITIONAL.parse(ops, file)
            .resultOrPartial(error -> Lifeguard.LOGGER.error("Lifeguard: faucet pouring recipe {} is invalid: {}", id, error))
            .flatMap(recipe -> recipe)
            .ifPresent(recipe -> {
                loaded.add(recipe);
                loadedJson.add(file.toString());
            }));
        recipes = List.copyOf(loaded);
        json = List.copyOf(loadedJson);
        Lifeguard.LOGGER.info("Lifeguard: {} faucet pouring recipes", loaded.size());
    }

    /**
     * Null when no recipe takes this block and fluid (the faucet tries its other targets); 0 when one does but the
     * source holds too little (it waits); otherwise the amount poured.
     */
    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, FluidOffer offer) {
        SoftFluidStack fluid = offer.fluid();
        for (PouringRecipe recipe : recipes) {
            if (!recipe.pours(fluid.getHolder()) || !recipe.target().test(state, level.random))
                continue;
            if (fluid.getCount() < recipe.amount())
                return 0;
            level.setBlock(pos, recipe.result(), 3);
            return recipe.amount();
        }
        return null;
    }
}
