package net.xlea99.lifeguard.entry.dye_the_world_open_sacks;

import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Dye the World's open sacks in Dye Depot's colors show their open top ({@link DyeTheWorldOpenSacks}).
 *
 * <p>Dye the World adds Supplementaries Squared sacks in Dye Depot's 16 colors. Its open-sack models give the top the
 * closed sack's texture, though it ships an open one for every color, so an open sack looked tied shut. The corrected
 * models are ours ({@code lifeguard:block/dye_the_world_sacks/sack_<color>_open}, each Dye the World's own but for that
 * one texture); once models are baked, every open state of those sacks is pointed at them. Our own model names, not
 * replacement files, so it doesn't depend on which mod's assets load first.
 */
public final class OpenSacks {
    private static final List<String> COLORS = List.of("amber", "aqua", "beige", "coral", "forest", "ginger", "indigo",
        "maroon", "mint", "navy", "olive", "rose", "slate", "tan", "teal", "verdant");

    private OpenSacks() {
    }

    public static void register(IEventBus modBus) {
        // Our models are built on Supplementaries' sack model: without the sacks there is nothing to fix, and
        // registering them anyway would only log their missing parent.
        if (!ModList.get().isLoaded("suppsquared"))
            return;
        modBus.addListener(OpenSacks::onRegisterModels);
        modBus.addListener(OpenSacks::onModifyBakingResult);
    }

    private static ModelResourceLocation fixedModel(String color) {
        return ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "block/dye_the_world_sacks/sack_" + color + "_open"));
    }

    private static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        COLORS.forEach(color -> event.register(fixedModel(color)));
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (String color : COLORS) {
            Block sack = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("suppsquared", "sack_" + color));
            BakedModel fixed = models.get(fixedModel(color));
            if (sack == Blocks.AIR || fixed == null
                || !(sack.getStateDefinition().getProperty("open") instanceof BooleanProperty open))
                continue;
            for (BlockState state : sack.getStateDefinition().getPossibleStates())
                if (state.getValue(open))
                    models.put(BlockModelShaper.stateToModelLocation(state), fixed);
        }
    }
}
