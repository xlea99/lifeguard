package net.xlea99.lifeguard.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.ChestTones.Tone;
import net.xlea99.lifeguard.client.ChestTones.Transfer;
import net.xlea99.lifeguard.core.Entry;
import org.jetbrains.annotations.Nullable;

/**
 * An item's icon in the colors of the chest it carries (chest boats, chest rafts, chest minecarts): a second layer over
 * the item's own icon, on the chest only. Each entry adds a {@link Stamp}: the vanilla icon whose chest is drawn on the
 * same pixels by every item it covers (checked 2026-10-31, icon_stamp.py), which pixels are chest (a mask hand-painted
 * by the user: the sides are shaded in colors the chest texture doesn't use, so no color test finds them; the outline is
 * left as drawn), which items it covers, how a stack says its chest, and the entry whose switch it asks. Each pixel's own
 * color is recolored by {@link Transfer} ({@link ChestTones}): its lightness moved from vanilla's wood to the chest's, its
 * color the chest's own wood at that lightness. So the shading keeps every step (rank-mapping to the chest's colors
 * flattened a side's three shades into one) and no accent color is ever picked (matching by place turned acacia's gray
 * where its trim sits; mocks icon_gray.py, icon_flat.py, icon_transfer2.py).
 *
 * <p>Icons live in the item atlas, so the overlays are made as it's stitched, by a sprite source of ours
 * ({@code assets/minecraft/atlases/blocks.json}): late enough that every generated chest texture (Every Compat's, made
 * by its Moonlight pack at reload) can be read, which a pack of our own couldn't promise (Moonlight runs its packs in no
 * fixed order). A wrapper on each covered item's model adds the layer for a stack carrying a chest. The sprite source is
 * registered whatever entries are installed ({@code wiring.always}): the atlas file names it.
 */
public final class ChestIcons {
    private static final ResourceLocation VANILLA_CHEST = ResourceLocation.withDefaultNamespace("textures/entity/chest/normal.png");
    /** A chest color counts as its wood (not its gray frame or latch) above this spread between its channels. */
    private static final int WOOD_SPREAD = 40;

    /**
     * One kind of icon: {@code name} (its sprites' folder), the vanilla {@code icon} the mask was painted on, the 16x16
     * {@code mask} ({@code #} = chest), the item ids it {@code covers}, the chest a stack carries as an item id
     * ({@code chestOf}, "" for none), and the {@code entry} whose switch it asks.
     */
    public record Stamp(String name, ResourceLocation icon, String[] mask, Predicate<ResourceLocation> covers,
                        Function<ItemStack, String> chestOf, Entry entry) {
    }

    private static final List<Stamp> STAMPS = new CopyOnWriteArrayList<>();
    private static final SpriteSourceType TYPE = new SpriteSourceType(MapCodec.unit(new Source()));
    /** (stamp, chest) -> the overlay's quads, baked from its sprite the first time; empty when it has none. */
    private static final Map<String, List<BakedQuad>> OVERLAYS = new ConcurrentHashMap<>();
    private static boolean registered;

    private ChestIcons() {
    }

    /** The sprite source and the model wrapping, once, whichever entries ask. */
    public static synchronized void register(IEventBus modBus) {
        if (registered)
            return;
        registered = true;
        modBus.addListener((RegisterSpriteSourceTypesEvent event) ->
            event.register(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "chest_icons"), TYPE));
        modBus.addListener(ChestIcons::onModels);
    }

    public static void add(Stamp stamp) {
        STAMPS.add(stamp);
    }

    private static ResourceLocation sprite(Item chest, Stamp stamp) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(chest);
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID,
            "chest_icons/" + stamp.name() + "/" + id.getNamespace() + "/" + id.getPath());
    }

    // --- the overlays, made as the item atlas is stitched -----------------------------------------------------------

    private record Source() implements SpriteSource {
        @Override
        public void run(ResourceManager manager, Output output) {
            if (STAMPS.isEmpty())
                return;
            try (NativeImage vanilla = read(manager, VANILLA_CHEST)) {
                Wood wood = Wood.of(vanilla);
                List<Map<Integer, Integer>> stamped = new ArrayList<>();
                List<Integer> widths = new ArrayList<>();
                for (Stamp stamp : STAMPS)
                    try (NativeImage icon = read(manager, stamp.icon())) {
                        stamped.add(stamp(icon, stamp.mask()));
                        widths.add(icon.getWidth());
                    }
                int made = 0;
                Set<Item> unknown = new HashSet<>();
                for (Item item : BuiltInRegistries.ITEM) {
                    ResourceLocation texture = ChestTextures.of(item);
                    if (texture == null)
                        continue;
                    Transfer transfer = transfer(manager, wood, texture, item, unknown);
                    if (transfer == null)
                        continue;
                    for (int i = 0; i < STAMPS.size(); i++)
                        add(output, sprite(item, STAMPS.get(i)), overlay(stamped.get(i), transfer, widths.get(i)));
                    made++;
                }
                StringBuilder pixels = new StringBuilder();
                for (int i = 0; i < STAMPS.size(); i++)
                    pixels.append(i == 0 ? "" : ", ").append(STAMPS.get(i).name()).append(' ').append(stamped.get(i).size());
                Lifeguard.LOGGER.info("Lifeguard: chest icons for {} chests (pixels of chest: {})", made, pixels);
            } catch (IOException | RuntimeException e) {
                Lifeguard.LOGGER.warn("Lifeguard: couldn't make chest icons; items carrying chests keep their own icons", e);
            }
        }

        @Override
        public SpriteSourceType type() {
            return TYPE;
        }
    }

    private static void add(SpriteSource.Output output, ResourceLocation id, NativeImage image) {
        output.add(id, loader -> new SpriteContents(id, new FrameSize(image.getWidth(), image.getHeight()), image, ResourceMetadata.EMPTY));
    }

    private static NativeImage read(ResourceManager manager, ResourceLocation texture) throws IOException {
        try (InputStream in = manager.open(texture)) {
            return NativeImage.read(in);
        }
    }

    /**
     * The vanilla chest's wood: its saturated colors (not the gray frame or latch), each with how many pixels have it,
     * which pixels they are, and its lightness's weighted mean and spread.
     */
    private record Wood(List<Tone> tones, boolean[] at, int width, int height, double mean, double spread) {
        static Wood of(NativeImage chest) {
            Map<Integer, Integer> colors = new HashMap<>();
            boolean[] at = new boolean[chest.getWidth() * chest.getHeight()];
            for (int y = 0; y < chest.getHeight(); y++)
                for (int x = 0; x < chest.getWidth(); x++) {
                    int abgr = chest.getPixelRGBA(x, y);
                    int r = abgr & 0xFF, g = abgr >>> 8 & 0xFF, b = abgr >>> 16 & 0xFF;
                    if (abgr >>> 24 != 0 && Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > WOOD_SPREAD) {
                        colors.merge(abgr, 1, Integer::sum);
                        at[y * chest.getWidth() + x] = true;
                    }
                }
            List<Tone> tones = ChestTones.tones(colors);
            return new Wood(tones, at, chest.getWidth(), chest.getHeight(), ChestTones.mean(tones), ChestTones.spread(tones));
        }
    }

    /** The icon's chest: pixel index (y * width + x) -> its own color, for every opaque pixel the mask marks. */
    private static Map<Integer, Integer> stamp(NativeImage icon, String[] mask) {
        Map<Integer, Integer> stamp = new HashMap<>();
        for (int y = 0; y < icon.getHeight(); y++)
            for (int x = 0; x < icon.getWidth(); x++) {
                int abgr = icon.getPixelRGBA(x, y);
                if (abgr >>> 24 != 0 && mask[y * mask.length / icon.getHeight()].charAt(x * mask.length / icon.getWidth()) == '#')
                    stamp.put(y * icon.getWidth() + x, abgr);
            }
        return stamp;
    }

    /** {@code item}'s chest's wood (its colors where vanilla's chest has wood, accents dropped) as a transfer. */
    @Nullable
    private static Transfer transfer(ResourceManager manager, Wood wood, ResourceLocation texture, Item item, Set<Item> unknown) {
        Map<Integer, Integer> found = new HashMap<>();
        try (NativeImage chest = read(manager, texture)) {
            for (int y = 0; y < wood.height(); y++)
                for (int x = 0; x < wood.width(); x++) {
                    if (!wood.at()[y * wood.width() + x])
                        continue;
                    int abgr = chest.getPixelRGBA(x * chest.getWidth() / wood.width(), y * chest.getHeight() / wood.height());
                    if (abgr >>> 24 != 0)
                        found.merge(abgr, 1, Integer::sum);
                }
        } catch (IOException | RuntimeException e) {
            if (unknown.add(item))
                Lifeguard.LOGGER.warn("Lifeguard: couldn't read {}'s chest texture {} for its chest icons", item, texture);
            return null;
        }
        if (found.isEmpty())
            return null;
        List<Tone> palette = ChestTones.withoutAccents(ChestTones.tones(found));
        return new Transfer(wood.mean(), wood.spread(), ChestTones.mean(palette), ChestTones.spread(palette), palette);
    }

    private static NativeImage overlay(Map<Integer, Integer> stamp, Transfer transfer, int width) {
        NativeImage image = new NativeImage(width, width, true);
        stamp.forEach((index, abgr) -> image.setPixelRGBA(index % width, index / width, transfer.apply(abgr)));
        return image;
    }

    // --- the layer on the item's model --------------------------------------------------------------------------------

    private static void onModels(ModelEvent.ModifyBakingResult event) {
        OVERLAYS.clear();
        for (Map.Entry<ModelResourceLocation, BakedModel> model : event.getModels().entrySet()) {
            if (!model.getKey().variant().equals("inventory") || !BuiltInRegistries.ITEM.containsKey(model.getKey().id()))
                continue;
            for (Stamp stamp : STAMPS)
                if (stamp.covers().test(model.getKey().id())) {
                    model.setValue(new Icon(model.getValue(), stamp));
                    break;
                }
        }
    }

    /** A covered item's model: as it was, or with the layer of the chest the stack carries (made once per chest). */
    private static final class Icon extends BakedModelWrapper<BakedModel> {
        private final Stamp stamp;
        private final Map<Item, BakedModel> withChest = new ConcurrentHashMap<>();
        private final ItemOverrides overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                BakedModel resolved = originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
                if (resolved == null)
                    resolved = originalModel;
                if (!stamp.entry().active())
                    return resolved;
                Item chest = chestOf(stamp.chestOf().apply(stack));
                if (chest == Items.AIR)
                    return resolved;
                List<BakedQuad> overlay = overlay(chest, stamp);
                if (overlay.isEmpty())
                    return resolved;
                return resolved == originalModel ? withChest.computeIfAbsent(chest, c -> new WithOverlay(originalModel, overlay))
                    : new WithOverlay(resolved, overlay);
            }
        };

        Icon(BakedModel original, Stamp stamp) {
            super(original);
            this.stamp = stamp;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }

    private static Item chestOf(String chest) {
        ResourceLocation id = chest.isEmpty() ? null : ResourceLocation.tryParse(chest);
        return id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
    }

    private static List<BakedQuad> overlay(Item chest, Stamp stamp) {
        ResourceLocation sprite = sprite(chest, stamp);
        return OVERLAYS.computeIfAbsent(sprite.toString(), key -> bake(sprite));
    }

    private static List<BakedQuad> bake(ResourceLocation spriteId) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(spriteId);
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()))
            return List.of();
        return UnbakedGeometryHelper.bakeElements(UnbakedGeometryHelper.createUnbakedItemElements(1, sprite), material -> sprite,
            BlockModelRotation.X0_Y0);
    }

    /** The item's icon with the chest's layer drawn over it. */
    private static final class WithOverlay extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> overlay;

        WithOverlay(BakedModel base, List<BakedQuad> overlay) {
            super(base);
            this.overlay = overlay;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return side != null ? super.getQuads(state, side, rand) : concat(super.getQuads(state, null, rand));
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
                                        @Nullable RenderType renderType) {
            return side != null ? super.getQuads(state, side, rand, data, renderType) : concat(super.getQuads(state, null, rand, data, renderType));
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(this);
        }

        /**
         * The item renderer swaps in whatever this returns just before drawing, and BakedModelWrapper returns the
         * original's own answer, so the layer was dropped: the original still applies its transform, this stays drawn.
         */
        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
            BakedModel transformed = originalModel.applyTransform(context, poseStack, leftHand);
            return transformed == originalModel ? this : new WithOverlay(transformed, overlay);
        }

        private List<BakedQuad> concat(List<BakedQuad> base) {
            List<BakedQuad> all = new ArrayList<>(base.size() + overlay.size());
            all.addAll(base);
            all.addAll(overlay);
            return all;
        }
    }
}
