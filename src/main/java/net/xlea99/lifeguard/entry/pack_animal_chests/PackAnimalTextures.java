package net.xlea99.lifeguard.entry.pack_animal_chests;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ChestedHorseRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.ChestTextures;

/**
 * A pack animal's skin with its saddlebags recolored for the chest it wears, made the first time such an animal is
 * drawn and dropped once none has been for a minute (one image per skin and chest in view). The bags sit at fixed places
 * on vanilla's skins, kept by Fresh Animations' models too (read 2026-10-31, FA 1.11.1): {@link Bags}.
 */
public final class PackAnimalTextures {
    private static final ResourceLocation VANILLA_CHEST = ResourceLocation.withDefaultNamespace("textures/entity/chest/normal.png");
    private static final long KEEP_NANOS = 60_000_000_000L;
    private static final int SWEEP_TICKS = 200;

    /**
     * Where a texture's chests are ({x, y, width, height}, on a texture {@code width} wide), and whether they're drawn in
     * vanilla's chest colors (their colors then move from vanilla's chest; otherwise from their own).
     */
    public enum Bags {
        /** ChestedHorseModel: both bags (22x11 unwraps of 8x8x3 boxes) share one place on the donkey's and mule's skin. */
        DONKEY(64, true, new int[][] {{26, 21, 22, 11}}),
        /** LlamaModel: each bag has its own place, on every llama skin and the trader llama's. */
        LLAMA(128, true, new int[][] {{45, 28, 22, 11}, {45, 41, 22, 11}}),
        /** Alex's Mobs' elephant: both 9x13x19 chests share one 56x32 unwrap on its chest layer, in Alex's own colors. */
        ELEPHANT(256, false, new int[][] {{57, 125, 56, 32}});

        final int width;
        final boolean vanillaColors;
        final int[][] at;

        Bags(int width, boolean vanillaColors, int[][] at) {
            this.width = width;
            this.vanillaColors = vanillaColors;
            this.at = at;
        }
    }

    private record Key(ResourceLocation skin, Item chest, Bags bags) {
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

    private PackAnimalTextures() {
    }

    /**
     * The skin {@code renderer} draws {@code entity} with: as picked, or with bags in the colors of the chest it wears.
     * Only vanilla's own donkey/mule and llama renderers: another renderer's skin may not keep the bags where they are.
     */
    public static ResourceLocation swap(LivingEntityRenderer<?, ?> renderer, Entity entity, ResourceLocation skin) {
        if (renderer instanceof LlamaRenderer && entity instanceof Llama llama)
            return swap(llama, skin, Bags.LLAMA);
        if (renderer instanceof ChestedHorseRenderer<?> && entity instanceof AbstractChestedHorse horse)
            return swap(horse, skin, Bags.DONKEY);
        return skin;
    }

    /** The texture to draw {@code horse} with: its own skin, or its skin with bags in the colors of the chest it wears. */
    public static ResourceLocation swap(AbstractChestedHorse horse, ResourceLocation skin, Bags bags) {
        if (!PackAnimalChests.ENTRY.active())
            return skin;
        return swap(skin, ClientPackAnimalChests.chestOf(horse), bags);
    }

    /** The texture to draw with: {@code skin}, or {@code skin} with its chests in the colors of {@code chest} (air: none). */
    public static ResourceLocation swap(ResourceLocation skin, Item chest, Bags bags) {
        if (chest == Items.AIR || chest == Items.CHEST)
            return skin;
        Made made = MADE.computeIfAbsent(new Key(skin, chest, bags), PackAnimalTextures::make);
        made.used = System.nanoTime();
        return made.texture != null ? made.texture : skin;
    }

    private static Made make(Key key) {
        ResourceLocation chestTexture = ChestTextures.of(key.chest());
        if (chestTexture == null) {
            if (UNKNOWN.add(key.chest()))
                Lifeguard.LOGGER.warn("Lifeguard: pack animal chests don't know where {} draws its chest; animals wearing it "
                    + "keep the vanilla one", BuiltInRegistries.ITEM.getKey(key.chest()));
            return new Made(null);
        }
        try (NativeImage skin = read(key.skin()); NativeImage chest = read(chestTexture); NativeImage vanilla = read(VANILLA_CHEST)) {
            NativeImage out = new NativeImage(skin.getWidth(), skin.getHeight(), true);
            out.copyFrom(skin);
            if (!SaddlebagDrawing.draw(out, key.bags().width, key.bags().at,
                key.bags().vanillaColors ? SaddlebagDrawing.Roles.of(vanilla, vanilla) : null, SaddlebagDrawing.Roles.of(chest, vanilla))) {
                out.close();
                return new Made(null);
            }
            ResourceLocation id = name(key);
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(out));
            return new Made(id);
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.warn("Lifeguard: couldn't recolor {}'s saddlebags for {} ({}); it keeps its own", key.skin(),
                BuiltInRegistries.ITEM.getKey(key.chest()), chestTexture, e);
            return new Made(null);
        }
    }

    /**
     * The same name every time for the same skin and chest: Minecraft keeps a render type per texture name for good, so a
     * new name per remake (after a sweep) would pile those up.
     */
    private static ResourceLocation name(Key key) {
        ResourceLocation chest = BuiltInRegistries.ITEM.getKey(key.chest());
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "dynamic/pack_animal/" + key.skin().getNamespace() + "/"
            + key.skin().getPath() + "/" + chest.getNamespace() + "/" + chest.getPath());
    }

    private static NativeImage read(ResourceLocation texture) throws IOException {
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(texture)) {
            return NativeImage.read(in);
        }
    }

    /** Drops what no animal has been drawn with for a while. Every client tick, acting every SWEEP_TICKS. */
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
