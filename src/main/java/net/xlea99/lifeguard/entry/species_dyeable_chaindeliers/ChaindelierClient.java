package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * The client side of dyed Chaindeliers ({@link Chaindeliers}): the glow mask source, the model wrappers, the tints, and
 * the tooltip (in Species' own words for its Hopelights and Speclights). Registered whenever Species is there; each
 * part asks the switch as it draws.
 */
public final class ChaindelierClient {
    /** The glow masks' tint indices: this plus the level (Species' model tints nothing). */
    public static final int TINT_BASE = 0;

    private static final ResourceLocation CHAINDELIER = ResourceLocation.fromNamespaceAndPath("species", "chaindelier");

    private ChaindelierClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterSpriteSourceTypesEvent event) -> {
            SpectreGlowMasks.type = new SpriteSourceType(SpectreGlowMasks.CODEC);
            event.register(SpectreGlowMasks.ID, SpectreGlowMasks.type);
        });
        modBus.addListener(ChaindelierClient::onBlockColors);
        modBus.addListener(ChaindelierClient::onItemColors);
        modBus.addListener(ChaindelierClient::onModifyBakingResult);
        NeoForge.EVENT_BUS.addListener(ChaindelierClient::onTooltip);
    }

    /** The tint for a mask level, or white for anything else. */
    private static int tint(int color, int tintIndex) {
        int level = tintIndex - TINT_BASE;
        return level >= 0 && level <= SpectreGlowMasks.LEVELS ? SpectreGlowMasks.tint(color, level) : 0xFFFFFF;
    }

    private static Block block() {
        Block block = BuiltInRegistries.BLOCK.get(CHAINDELIER);
        return block == Blocks.AIR ? null : block;
    }

    private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        Block block = block();
        if (block == null)
            return;
        event.register((state, level, pos, tintIndex) -> {
            int color = level != null && pos != null && level.getBlockEntity(pos) instanceof DyeableChaindelier dyed
                ? dyed.lifeguard$getColor() : Chaindeliers.DEFAULT_COLOR;
            return tint(color, tintIndex);
        }, block);
    }

    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        Item item = Chaindeliers.item();
        if (item == null)
            return;
        event.register((stack, tintIndex) -> {
            Integer color = Chaindeliers.colorOf(stack);
            return 0xFF000000 | tint(color != null ? color : Chaindeliers.DEFAULT_COLOR, tintIndex);
        }, item);
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Block block = block();
        if (block == null)
            return;
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        int wrapped = 0;
        for (BlockState state : block.getStateDefinition().getPossibleStates())
            if (models.computeIfPresent(BlockModelShaper.stateToModelLocation(state), (id, model) -> ChaindelierModel.forBlock(model)) != null)
                wrapped++;
        boolean item = models.computeIfPresent(ModelResourceLocation.inventory(CHAINDELIER), (id, model) -> ChaindelierModel.forItem(model)) != null;
        Lifeguard.LOGGER.info("Lifeguard: wrapped {} Chaindelier models for dyed glow; item model {}", wrapped, item ? "wrapped" : "NOT FOUND");
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (!Chaindeliers.enabled() || !Chaindeliers.is(event.getItemStack()))
            return;
        Integer color = Chaindeliers.colorOf(event.getItemStack());
        Component line = color == null
            ? Component.translatable("item.species.spectre_light.dyeable").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            : Component.translatable("item.species.spectre_light.color").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("#%06X", color)).withStyle(Style.EMPTY.withColor(color)));
        event.getToolTip().add(Math.min(1, event.getToolTip().size()), line);
    }
}
