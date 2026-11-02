package net.xlea99.lifeguard.entry.pack_animal_chests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.xlea99.lifeguard.sync.Payloads;
import org.jetbrains.annotations.Nullable;

/**
 * Alex's Mobs' elephants ({@link PackAnimalChests#ALEXS_MOBS}). They take any wooden chest and forget which: a sheared
 * or killed elephant dropped a plain chest (EntityElephant.mobInteract / dropEquipment, Alex's Mobs 1.22.9, read
 * 2026-10-31). Now the chest put on is kept in its persistent data under {@link PackAnimalChest#KEY} and given back,
 * whatever the switch (as chest boats: switching off loses no chest); players seeing it learn it from
 * {@link PackAnimalChestPayload}, and its chest layer ({@code elephant_chest.png}, the whole model drawn again) is
 * recolored for it. Nothing of Alex's Mobs is compiled in: the hooks name its classes, these take vanilla types.
 */
public final class ElephantChests {
    private static final ResourceLocation ELEPHANT = ResourceLocation.fromNamespaceAndPath("alexsmobs", "elephant");
    /** What the player clicking an elephant held, while its mobInteract runs. Server thread. */
    private static final ThreadLocal<Item> HELD = new ThreadLocal<>();
    /** The living entity being drawn right now. Render thread. */
    @Nullable
    private static Entity drawing;

    private ElephantChests() {
    }

    static boolean isElephant(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(ELEPHANT);
    }

    /** Around a click on an elephant: returns what to restore after. */
    public static Item startClick(Player player, InteractionHand hand) {
        Item previous = HELD.get();
        HELD.set(player.getItemInHand(hand).getItem());
        return previous;
    }

    public static void endClick(@Nullable Item previous) {
        if (previous == null)
            HELD.remove();
        else
            HELD.set(previous);
    }

    /** A chest went on by a click (server side): remember which, and tell everyone who sees it. */
    public static void chested(Entity elephant, boolean chested) {
        Item held = HELD.get();
        if (!chested || held == null || !(elephant.level() instanceof ServerLevel level)
            || !held.builtInRegistryHolder().is(Tags.Items.CHESTS_WOODEN))
            return;
        String chest = held == Items.CHEST ? "" : BuiltInRegistries.ITEM.getKey(held).toString();
        if (chest.isEmpty())
            elephant.getPersistentData().remove(PackAnimalChest.KEY);
        else
            elephant.getPersistentData().putString(PackAnimalChest.KEY, chest);
        if (PackAnimalChests.ALEXS_MOBS.active() && elephant.isAddedToLevel()) {
            PackAnimalChestPayload payload = new PackAnimalChestPayload(elephant.getId(), chest);
            for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayersWatching(elephant))
                Payloads.send(player, payload);
        }
    }

    /** What a shear or death drops (server side): the chest it wore instead of a plain one, then forgotten. */
    public static ItemLike dropped(Entity elephant, ItemLike item) {
        if (elephant.level().isClientSide() || item.asItem() != Items.CHEST)
            return item;
        ResourceLocation id = ResourceLocation.tryParse(elephant.getPersistentData().getString(PackAnimalChest.KEY));
        elephant.getPersistentData().remove(PackAnimalChest.KEY);
        Item chest = id == null || id.getPath().isEmpty() ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        return chest == Items.AIR ? item : chest;
    }

    /** The chest an elephant wears, for a player starting to see it: "" for none or a plain one. */
    static String chestOf(Entity elephant) {
        return elephant.getPersistentData().getString(PackAnimalChest.KEY);
    }

    // --- client -------------------------------------------------------------------------------------------------------

    /** Around drawing a living entity: returns what to restore after. */
    public static Entity startDrawing(Entity entity) {
        Entity previous = drawing;
        drawing = entity;
        return previous;
    }

    public static void endDrawing(@Nullable Entity previous) {
        drawing = previous;
    }

    /** The texture an elephant's chest layer draws with: Alex's, or recolored for the chest it wears. */
    public static ResourceLocation chestLayer(ResourceLocation texture) {
        if (drawing == null || !PackAnimalChests.ALEXS_MOBS.active() || !isElephant(drawing))
            return texture;
        return PackAnimalTextures.swap(texture, ClientPackAnimalChests.chestOf(chestOf(drawing)), PackAnimalTextures.Bags.ELEPHANT);
    }
}
