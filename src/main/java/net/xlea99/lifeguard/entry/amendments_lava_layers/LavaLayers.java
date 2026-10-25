package net.xlea99.lifeguard.entry.amendments_lava_layers;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Lava kept as layers in an Amendments cauldron, like every other liquid, shared by the mixins that make it so.
 *
 * <p>Vanilla holds lava in its own all-or-nothing lava cauldron, which Amendments switches to as soon as one of its
 * cauldrons holds exactly a bucket of lava. Here a vanilla lava cauldron instead turns into an Amendments one holding
 * four layers the moment it's placed, whatever placed it, and Amendments stops switching back. What vanilla's lava
 * cauldron did (burn what falls in, give up a bucket, heat Farmer's Delight's pots) the Amendments one does too.
 */
public final class LavaLayers {

    /** Layers in a full Amendments cauldron: a bucket. */
    public static final int FULL = 4;

    private LavaLayers() {
    }

    /** Amendments' own cauldrons (its enhanced_cauldron feature) are on; without them there's nowhere to keep layers. */
    public static boolean cauldronsOn() {
        return CommonConfigs.LIQUID_CAULDRON.get();
    }

    /** The lava tank of an Amendments cauldron at this position, or null if it holds something else. */
    @Nullable
    public static SoftFluidTank lava(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile cauldron
            && cauldron.getSoftFluidTank().getFluid().is(MLBuiltinSoftFluids.LAVA) ? cauldron.getSoftFluidTank() : null;
    }

    /** Whatever is at this position becomes an Amendments cauldron holding this many layers of lava. */
    public static void toLavaLayers(Level level, BlockPos pos, int layers) {
        CauldronConversion.setCorrectCauldronStateAndTile(level.getBlockState(pos), level, pos,
            SoftFluidStack.of(MLBuiltinSoftFluids.LAVA.getHolder(level), layers));
    }
}
