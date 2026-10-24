package net.xlea99.lifeguard.entry.faucet_pouring_recipes.emi;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * One faucet pouring recipe, laid out as it happens: the faucet (its placed block, side on), the fluid falling from it,
 * the block it lands on, an arrow, and what that block becomes.
 *
 * <pre>
 *  F
 *  |  1 bottle
 * [I] -> [R]
 * </pre>
 */
final class FaucetPouringEmiRecipe implements EmiRecipe {
    private static final int SLOT_Y = 36;
    private static final int FAUCET_Y = 2;
    /** The spout's lowest pixel is 5 of 16 up its block (faucet_model.json); the stream starts there. */
    private static final int STREAM_Y = FAUCET_Y + 16 - 5;
    /** Down to the input slot's inner edge. */
    private static final int STREAM_HEIGHT = SLOT_Y + 1 - STREAM_Y;
    private static final int STREAM_WIDTH = 4;

    private final ResourceLocation id;
    private final EmiIngredient input;
    private final List<Holder<SoftFluid>> fluids;
    private final boolean anyFluid;
    private final int amount;
    /** One, or several shown cycling in step with the input (all the concretes, in their powders' order). */
    private final List<EmiStack> outputs;
    @Nullable
    private final Float chance;

    FaucetPouringEmiRecipe(ResourceLocation id, EmiIngredient input, List<Holder<SoftFluid>> fluids, boolean anyFluid, int amount, List<EmiStack> outputs,
                           @Nullable Float chance) {
        this.id = id;
        this.input = input;
        this.fluids = fluids;
        this.anyFluid = anyFluid;
        this.amount = amount;
        this.outputs = outputs;
        this.chance = chance;
    }

    /** A recipe for any fluid at all, rather than particular ones. */
    boolean anyFluid() {
        return anyFluid;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return FaucetPouringEmiPlugin.CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 84;
    }

    @Override
    public int getDisplayHeight() {
        return SLOT_Y + 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(1, FAUCET_Y, 16, 16, (draw, mouseX, mouseY, delta) -> drawFaucet(draw));
        widgets.addDrawable(9 - STREAM_WIDTH / 2, STREAM_Y, STREAM_WIDTH, STREAM_HEIGHT, (draw, mouseX, mouseY, delta) -> drawStream(draw));
        widgets.addTooltip((mouseX, mouseY) -> List.of(
            ClientTooltipComponent.create(fluid().value().getTranslatedName().getVisualOrderText()),
            ClientTooltipComponent.create(amountText().getVisualOrderText())), 9 - STREAM_WIDTH / 2, STREAM_Y, STREAM_WIDTH, STREAM_HEIGHT);
        widgets.addText(amountText(), 20, 22, 0xFF7F7F7F, false);
        widgets.addSlot(input, 0, SLOT_Y);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, SLOT_Y + 1);
        if (chance != null)
            widgets.addText(Component.literal(Math.round(chance * 100) + "%"), 30, SLOT_Y - 8, 0xFF7F7F7F, false);
        widgets.addSlot(EmiIngredient.of(outputs), 56, SLOT_Y).recipeContext(this);
    }

    private Component amountText() {
        return Component.translatable(amount == 1 ? "lifeguard.emi.faucet_pouring.bottle" : "lifeguard.emi.faucet_pouring.bottles", amount);
    }

    /** With more than one fluid (a tag, a list, or any fluid at all), a different one every second. */
    private Holder<SoftFluid> fluid() {
        return fluids.get((int) (System.currentTimeMillis() / 1000 % fluids.size()));
    }

    /** The faucet as placed on a wall, seen from the side, its spout over the block below. */
    private static void drawFaucet(GuiGraphics draw) {
        BlockState faucet = ModRegistry.FAUCET.get().defaultBlockState().setValue(FaucetBlock.FACING, Direction.EAST);
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack pose = draw.pose();
        pose.pushPose();
        pose.translate(0, 16, 100);
        pose.scale(16, -16, 16);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Lighting.setupForFlatItems();
        minecraft.getBlockRenderer().renderSingleBlock(faucet, pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        buffers.endBatch();
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    /** The fluid's own flowing texture (animated, as in the world), tinted with its color. */
    private void drawStream(GuiGraphics draw) {
        SoftFluid fluid = fluid().value();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getFlowingTexture());
        int color;
        try {
            color = SoftFluidStack.of(fluid()).getFlowingColor(null, null);
        } catch (RuntimeException e) {
            color = fluid.getTintColor();
        }
        if (color == -1)
            color = 0xFFFFFF;
        draw.blit(0, 0, 0, STREAM_WIDTH, STREAM_HEIGHT, sprite,
            (color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);
    }
}
