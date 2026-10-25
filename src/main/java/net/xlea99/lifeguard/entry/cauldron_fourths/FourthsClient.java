package net.xlea99.lifeguard.entry.cauldron_fourths;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.xlea99.lifeguard.client.ClientChecks;

/**
 * Checks, after every resource reload, that a full four-level water and powder snow cauldron has a model: the
 * entry's pack is required and on top, but a mod or another always-on pack shipping its own cauldron blockstate could
 * still cover it, and a full cauldron would then render as the missing model.
 */
public final class FourthsClient {
    private FourthsClient() {
    }

    public static void register() {
        ClientChecks.register(CauldronFourths.ENTRY, () -> {
            List<String> missing = new ArrayList<>();
            for (Block cauldron : List.of(Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON))
                if (missingModel(cauldron))
                    missing.add(cauldron.builtInRegistryHolder().key().location().toString());
            return missing.isEmpty() ? null
                : "no model for a full (level 4) " + String.join(", ", missing) + ": another pack's blockstate covers Lifeguard's";
        });
    }

    private static boolean missingModel(Block cauldron) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getBlockRenderer().getBlockModel(cauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 4))
            == minecraft.getModelManager().getMissingModel();
    }
}
