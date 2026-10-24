package net.xlea99.lifeguard.entry.faucet_pouring_recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

/**
 * One faucet pouring recipe: a faucet pouring {@code amount} of {@code fluid} onto a block matching {@code target}
 * turns it into {@code result}.
 *
 * <pre>{@code
 * {
 *   "target": {"predicate_type": "minecraft:block_match", "block": "minecraft:coarse_dirt"},
 *   "fluid": "moonlight:water",
 *   "amount": 1,
 *   "result": {"Name": "minecraft:mud"}
 * }
 * }</pre>
 *
 * <p>{@code target} is a vanilla block rule test (block, tag, exact state or random chance, as structure processors
 * use). {@code fluid} is a Moonlight soft fluid: an id, a {@code #tag} or a list; left out, any fluid (a sponge
 * soaks up anything). {@code amount} is in bottles (a bottle is 1, a cauldron 3, a bucket 4) and defaults to 1.
 */
public record PouringRecipe(RuleTest target, Optional<HolderSet<SoftFluid>> fluid, int amount, BlockState result) {
    public static final Codec<PouringRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        RuleTest.CODEC.fieldOf("target").forGetter(PouringRecipe::target),
        RegistryCodecs.homogeneousList(SoftFluidRegistry.KEY).optionalFieldOf("fluid").forGetter(PouringRecipe::fluid),
        ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(PouringRecipe::amount),
        BlockState.CODEC.fieldOf("result").forGetter(PouringRecipe::result)
    ).apply(instance, PouringRecipe::new));

    public boolean pours(Holder<SoftFluid> poured) {
        return fluid.map(set -> set.contains(poured)).orElse(true);
    }
}
