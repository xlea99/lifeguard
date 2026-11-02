package net.xlea99.lifeguard.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.xlea99.lifeguard.boot.BrokenEntries;
import net.xlea99.lifeguard.core.Entry;
import org.jetbrains.annotations.Nullable;

/**
 * Woodworks' record of the chest a donkey, mule or llama wears: Blueprint tracked data {@code woodworks:chest_variant}
 * (an item id, synced to clients; set when a chest goes on, never cleared; read 2026-10-31 from Woodworks 4.0.2), found
 * by name so nothing of Woodworks' is compiled in. Only call with Woodworks installed (it brings Blueprint).
 */
public final class WoodworksChests {
    private static final ResourceLocation DATA = ResourceLocation.fromNamespaceAndPath("woodworks", "chest_variant");
    @Nullable
    private static com.teamabnormals.blueprint.common.world.storage.tracking.TrackedData<?> data;
    private static boolean looked;

    private WoodworksChests() {
    }

    /**
     * The chest Woodworks last put on {@code horse}, as an item: air for a plain one, or when Woodworks' data can't be
     * found ({@code user} is then broken, said once).
     */
    public static Item of(AbstractChestedHorse horse, Entry user) {
        var manager = com.teamabnormals.blueprint.common.world.storage.tracking.TrackedDataManager.INSTANCE;
        if (!looked) {
            looked = true;
            data = manager.getTrackedData(DATA);
        }
        if (data == null) {
            BrokenEntries.mark(user, "Woodworks' tracked data " + DATA);
            return Items.AIR;
        }
        Item chest = manager.getValue(horse, data) instanceof ResourceLocation id ? BuiltInRegistries.ITEM.get(id) : Items.AIR;
        return chest == Items.CHEST ? Items.AIR : chest;
    }
}
