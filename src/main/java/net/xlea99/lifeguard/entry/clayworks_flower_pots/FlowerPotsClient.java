package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.xlea99.lifeguard.Lifeguard;

/** Wraps every flower pot's model (every potted plant of every mod, found by walking the registry) and the pot item's. */
public final class FlowerPotsClient {
    private FlowerPotsClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(FlowerPotsClient::onModifyBakingResult);
        modBus.addListener((RegisterSpriteSourceTypesEvent event) -> {
            TrimDesignSource.type = new SpriteSourceType(TrimDesignSource.CODEC);
            event.register(TrimDesignSource.ID, TrimDesignSource.type);
        });
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        FlowerPotModel.clearSprites();
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        int blocks = 0;
        int wrapped = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof FlowerPotBlock))
                continue;
            blocks++;
            for (BlockState state : block.getStateDefinition().getPossibleStates())
                if (models.computeIfPresent(BlockModelShaper.stateToModelLocation(state), (id, model) -> FlowerPotModel.forBlock(model)) != null)
                    wrapped++;
        }
        boolean item = models.computeIfPresent(ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(Items.FLOWER_POT)),
            (id, model) -> FlowerPotModel.forItem(model)) != null;
        Lifeguard.LOGGER.info("Lifeguard: wrapped {} models across {} flower pot blocks; item model {}", wrapped, blocks,
            item ? "wrapped" : "NOT FOUND");
    }
}
