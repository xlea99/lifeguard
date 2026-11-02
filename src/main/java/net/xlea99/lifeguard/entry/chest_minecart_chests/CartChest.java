package net.xlea99.lifeguard.entry.chest_minecart_chests;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.xlea99.lifeguard.sync.Payloads;

/**
 * Which chest a chest minecart carries: an item id under {@link #KEY}, in the chest minecart item's custom data and the
 * cart's persistent data (plain data, as chest boats keep theirs). It moves item -> cart when the item is placed on a
 * rail or dispensed (ItemStack.useOn and DispenserBlock), and cart -> item when it breaks or is picked. A vanilla chest
 * records nothing. Clients learn a cart's chest from {@link CartChestPayload}.
 */
public final class CartChest {
    public static final String KEY = "lifeguard:minecart_chest";
    /** The chest of the item being placed or dispensed right now, for the cart it adds. Server thread. */
    private static final ThreadLocal<String> PLACING = new ThreadLocal<>();

    private CartChest() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CartChest::onJoin);
        NeoForge.EVENT_BUS.addListener(CartChest::onStartTracking);
    }

    /** The chest a chest minecart item carries, or "" for none. */
    public static String of(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getString(KEY);
    }

    /** The chest a cart carries, or "" for none. */
    public static String of(Entity cart) {
        return cart.getPersistentData().getString(KEY);
    }

    private static ItemStack with(ItemStack stack, String chest) {
        if (!chest.isEmpty() && !stack.isEmpty())
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(KEY, chest));
        return stack;
    }

    /** A crafted chest minecart remembers the wooden chest that went in (any shaped or shapeless recipe makes one). */
    public static ItemStack crafted(ItemStack result, CraftingInput input) {
        if (!ChestMinecartChests.ENTRY.active() || !result.is(Items.CHEST_MINECART))
            return result;
        for (ItemStack in : input.items())
            if (in.is(Tags.Items.CHESTS_WOODEN) && !in.is(Tags.Items.CHESTS_TRAPPED) && !in.is(Items.CHEST))
                return with(result, BuiltInRegistries.ITEM.getKey(in.getItem()).toString());
        return result;
    }

    /** The item a chest minecart drops or is picked as carries its chest. */
    public static ItemStack carried(Entity cart, ItemStack stack) {
        return cart instanceof MinecartChest ? with(stack, of(cart)) : stack;
    }

    /** Around a placement or dispense of {@code stack}: returns what to restore after. */
    public static String startPlacing(ItemStack stack) {
        String previous = PLACING.get();
        PLACING.set(of(stack));
        return previous;
    }

    public static void endPlacing(String previous) {
        if (previous == null)
            PLACING.remove();
        else
            PLACING.set(previous);
    }

    private static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof MinecartChest))
            return;
        String chest = PLACING.get();
        if (chest != null && !chest.isEmpty())
            event.getEntity().getPersistentData().putString(KEY, chest);
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!ChestMinecartChests.ENTRY.active() || !(event.getTarget() instanceof MinecartChest)
            || !(event.getEntity() instanceof ServerPlayer player))
            return;
        String chest = of(event.getTarget());
        if (!chest.isEmpty())
            Payloads.send(player, new CartChestPayload(event.getTarget().getId(), chest));
    }
}
