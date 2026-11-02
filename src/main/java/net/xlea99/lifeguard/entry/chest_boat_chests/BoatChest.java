package net.xlea99.lifeguard.entry.chest_boat_chests;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
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
 * Which chest a chest boat carries: an item id under {@link #KEY}, in the chest boat item's custom data and the boat's
 * persistent data (plain data, no registered component, so clients without Lifeguard can still join). It moves item ->
 * boat when the item is used or dispensed (whatever mod's boat item or dispenser behavior places it: they all go
 * through ItemStack.use and DispenserBlock), and boat -> item when it breaks or is picked. A vanilla chest records
 * nothing: the boat looks as it always did. Clients learn a boat's chest from {@link BoatChestPayload}.
 */
public final class BoatChest {
    public static final String KEY = "lifeguard:boat_chest";
    /** The chest of the item being used or dispensed right now, for the boat it places. Server thread. */
    private static final ThreadLocal<String> PLACING = new ThreadLocal<>();

    private BoatChest() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BoatChest::onJoin);
        NeoForge.EVENT_BUS.addListener(BoatChest::onStartTracking);
    }

    /** The chest a chest boat item carries, or "" for none. */
    public static String of(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getString(KEY);
    }

    /** The chest a boat carries, or "" for none. */
    public static String of(Entity boat) {
        return boat.getPersistentData().getString(KEY);
    }

    /** A boat that holds a chest: vanilla's ChestBoat and everything built on it, and Spawn's (its own boat class). */
    public static boolean isChestBoat(Entity entity) {
        return entity instanceof Boat && entity instanceof ContainerEntity;
    }

    private static ItemStack with(ItemStack stack, String chest) {
        if (!chest.isEmpty() && !stack.isEmpty())
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(KEY, chest));
        return stack;
    }

    /** A crafted chest boat remembers the wooden chest that went in (any shaped or shapeless recipe makes one). */
    public static ItemStack crafted(ItemStack result, CraftingInput input) {
        if (!ChestBoatChests.ENTRY.active() || !result.is(ItemTags.CHEST_BOATS))
            return result;
        for (ItemStack in : input.items())
            if (in.is(Tags.Items.CHESTS_WOODEN) && !in.is(Tags.Items.CHESTS_TRAPPED) && !in.is(Items.CHEST))
                return with(result, BuiltInRegistries.ITEM.getKey(in.getItem()).toString());
        return result;
    }

    /** The item a chest boat drops or is picked as carries its chest. */
    public static ItemStack carried(Entity boat, ItemStack stack) {
        return isChestBoat(boat) ? with(stack, of(boat)) : stack;
    }

    /** Around a use or dispense of {@code stack}: returns what to restore after. */
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
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !isChestBoat(event.getEntity()))
            return;
        String chest = PLACING.get();
        if (chest != null && !chest.isEmpty())
            event.getEntity().getPersistentData().putString(KEY, chest);
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!ChestBoatChests.ENTRY.active() || !isChestBoat(event.getTarget()) || !(event.getEntity() instanceof ServerPlayer player))
            return;
        String chest = of(event.getTarget());
        if (!chest.isEmpty())
            Payloads.send(player, new BoatChestPayload(event.getTarget().getId(), chest));
    }
}
