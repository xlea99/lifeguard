package net.xlea99.lifeguard.entry.chest_minecart_chests;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.xlea99.lifeguard.client.ChestIcons;
import org.jetbrains.annotations.Nullable;

/**
 * The client half of {@link ChestMinecartChests}: a cart's chest from the server, the tooltip, and the block a cart
 * draws. A minecart draws its block with {@code renderSingleBlock}, which draws a chest (an animated entity-block)
 * through its item's renderer: Quark's, Blueprint's (Woodworks', C&C's) and Every Compat's chests all bring their own,
 * so handing the cart the chest's block draws that chest, real texture and all.
 */
public final class ClientCartChests {
    /** The chest on vanilla's chest minecart icon (16x16, {@code #} = chest), painted by the user (2026-10-31). */
    private static final String[] MASK = {
        "................",
        ".......###......",
        ".....######.....",
        "...#########....",
        "....#########...",
        "...#.#######....",
        "...#..####..#...",
        ".......#..#.....",
        "......#.#.......",
        "......#.........",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"};
    /** chest item -> the block state a cart wearing it draws, or null when nothing would draw it (kept vanilla). */
    private static final Map<Item, BlockState> SHOWN = new HashMap<>();

    private ClientCartChests() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientCartChests::onTooltip);
        ChestIcons.add(new ChestIcons.Stamp("minecart", ResourceLocation.withDefaultNamespace("textures/item/chest_minecart.png"),
            MASK, id -> id.equals(BuiltInRegistries.ITEM.getKey(Items.CHEST_MINECART)), CartChest::of, ChestMinecartChests.ENTRY));
    }

    static void received(int entity, String chest) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(entity) instanceof MinecartChest cart)
            cart.getPersistentData().putString(CartChest.KEY, chest);
    }

    /** The chest a cart or chest minecart item carries, as an item (air when none or unknown). */
    static Item chestOf(String chest) {
        ResourceLocation id = chest.isEmpty() ? null : ResourceLocation.tryParse(chest);
        return id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
    }

    /** What {@code cart} draws as its block: its own, or the block of the chest it carries. */
    public static BlockState shown(AbstractMinecart cart, BlockState original) {
        if (!(cart instanceof MinecartChest) || cart.hasCustomDisplay() || !ChestMinecartChests.ENTRY.active())
            return original;
        Item chest = chestOf(CartChest.of(cart));
        if (chest == Items.AIR || chest == Items.CHEST)
            return original;
        BlockState shown = SHOWN.computeIfAbsent(chest, ClientCartChests::stateFor);
        return shown != null ? shown : original;
    }

    @Nullable
    private static BlockState stateFor(Item chest) {
        if (!(chest instanceof BlockItem item))
            return null;
        BlockState state = item.getBlock().defaultBlockState();
        // Drawn through its item's renderer: one that never registered a renderer would draw nothing at all.
        if (state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED
            && IClientItemExtensions.of(new ItemStack(chest)) == IClientItemExtensions.DEFAULT)
            return null;
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH) : state;
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (!ChestMinecartChests.ENTRY.active())
            return;
        Item chest = chestOf(CartChest.of(event.getItemStack()));
        if (chest != Items.AIR)
            event.getToolTip().add(Math.min(1, event.getToolTip().size()),
                Component.translatable("lifeguard.chest_minecart_chests.tooltip", chest.getDescription()).withStyle(ChatFormatting.GRAY));
    }
}
