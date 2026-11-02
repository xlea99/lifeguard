package net.xlea99.lifeguard.entry.chest_boat_chests;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.ChestTextures;

/**
 * A chest boat's texture with the chest it carries drawn on, made the first time such a boat is drawn and dropped once
 * none has been for a minute (boat textures are standalone, not in an atlas: one image per pair in view). Every chest
 * boat, vanilla's and every mod's, has its chest at the same place on its texture, in Mojang's chest boat layout, so
 * one drawing serves them all ({@link ChestDrawing}), from the chest's own texture ({@link ChestTextures}).
 */
public final class ChestBoatTextures {
    private static final long KEEP_NANOS = 60_000_000_000L;
    private static final int SWEEP_TICKS = 200;

    private record Key(ResourceLocation boat, Item chest, ChestDrawing.Layout layout) {
    }

    /** A made texture, or a pair that can't be made (texture null), and when it was last drawn. */
    private static final class Made {
        final ResourceLocation texture;
        long used;

        Made(ResourceLocation texture) {
            this.texture = texture;
        }
    }

    private static final Map<Key, Made> MADE = new HashMap<>();
    /** Chests whose texture couldn't be found, said once each. */
    private static final Set<Item> UNKNOWN = new HashSet<>();
    private static int ticks;

    private ChestBoatTextures() {
    }

    /** Vanilla's renderer (and those extending it): the texture half of its (texture, model) pair. */
    public static Pair<ResourceLocation, ListModel<Boat>> swap(Boat boat, Pair<ResourceLocation, ListModel<Boat>> pair) {
        ResourceLocation texture = swap(boat, pair.getFirst());
        return texture == pair.getFirst() ? pair : Pair.of(texture, pair.getSecond());
    }

    /** The texture to draw {@code boat} with: its own, or its own with the chest it carries drawn on. */
    public static ResourceLocation swap(Boat boat, ResourceLocation texture) {
        return swap(boat, texture, ChestDrawing.CHEST_BOAT);
    }

    /** Alex's Caves draws a chest boat's chest from its own chest texture, with its own chest model. */
    public static ResourceLocation swapAlexsCavesChest(Boat boat, ResourceLocation chestTexture) {
        return swap(boat, chestTexture, ChestDrawing.ALEXS_CAVES);
    }

    private static ResourceLocation swap(Boat boat, ResourceLocation texture, ChestDrawing.Layout layout) {
        if (!ChestBoatChests.ENTRY.active())
            return texture;
        Item chest = ClientBoatChests.chestOf(boat);
        if (chest == Items.AIR)
            return texture;
        Made made = MADE.computeIfAbsent(new Key(texture, chest, layout), ChestBoatTextures::make);
        made.used = System.nanoTime();
        return made.texture != null ? made.texture : texture;
    }

    private static Made make(Key key) {
        ResourceLocation chestTexture = ChestTextures.of(key.chest());
        if (chestTexture == null) {
            if (UNKNOWN.add(key.chest()))
                Lifeguard.LOGGER.warn("Lifeguard: chest boats don't know where {} draws its chest; boats carrying it keep "
                    + "the vanilla one", BuiltInRegistries.ITEM.getKey(key.chest()));
            return new Made(null);
        }
        try (NativeImage boat = read(key.boat()); NativeImage chest = read(chestTexture)) {
            NativeImage out = new NativeImage(boat.getWidth(), boat.getHeight(), true);
            out.copyFrom(boat);
            ChestDrawing.draw(chest, out, key.layout());
            ResourceLocation id = name(key);
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(out));
            return new Made(id);
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.warn("Lifeguard: couldn't draw {}'s chest ({}) onto {}; it keeps its own chest",
                BuiltInRegistries.ITEM.getKey(key.chest()), chestTexture, key.boat(), e);
            return new Made(null);
        }
    }

    /**
     * The same name every time for the same boat texture, chest and layout: Minecraft keeps a render type per texture name
     * for good, so a new name per remake (after a sweep) would pile those up.
     */
    private static ResourceLocation name(Key key) {
        ResourceLocation chest = BuiltInRegistries.ITEM.getKey(key.chest());
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "dynamic/chest_boat/"
            + (key.layout() == ChestDrawing.ALEXS_CAVES ? "alexs_caves/" : "chest_boat/") + key.boat().getNamespace() + "/"
            + key.boat().getPath() + "/" + chest.getNamespace() + "/" + chest.getPath());
    }

    private static NativeImage read(ResourceLocation texture) throws IOException {
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(texture)) {
            return NativeImage.read(in);
        }
    }

    /** Drops what no boat has been drawn with for a while. Every client tick, acting every SWEEP_TICKS. */
    static void sweep() {
        if (++ticks < SWEEP_TICKS)
            return;
        ticks = 0;
        long now = System.nanoTime();
        for (Iterator<Made> it = MADE.values().iterator(); it.hasNext(); ) {
            Made made = it.next();
            if (made.texture != null && now - made.used > KEEP_NANOS) {
                Minecraft.getInstance().getTextureManager().release(made.texture);
                it.remove();
            }
        }
    }

    /** Everything made goes (a resource reload may have changed any of it; leaving a world). */
    static void clear() {
        for (Made made : MADE.values())
            if (made.texture != null)
                Minecraft.getInstance().getTextureManager().release(made.texture);
        MADE.clear();
        UNKNOWN.clear();
    }
}
