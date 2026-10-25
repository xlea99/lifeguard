package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Wraps a flower pot model: quads textured with the pot sprite are re-pointed at its color's permutation, and a design
 * is laid over the four outer sides as copies a hair outward, textured with the design in its material. Every other quad (the plant,
 * the dirt, a mod pot's own texture) passes through.
 *
 * <ul>
 *     <li>{@link #forBlock}: the looks come from the block entity's model data; with none, the original answers.</li>
 *     <li>{@link #forItem}: the item draws the block model; its looks come off the stack, through a wrapper pinned to
 *     them.</li>
 * </ul>
 */
public final class FlowerPotModel extends BakedModelWrapper<BakedModel> {
    public static final ResourceLocation POT_SPRITE = ResourceLocation.withDefaultNamespace("block/flower_pot");
    /** How far a design sits off the pot, in blocks: enough not to flicker, too little to see. */
    private static final float LIFT = 0.0008F;

    @Nullable
    private final DyeColor fixedColor;
    @Nullable
    private final DecoratedPotTrim fixedDesign;
    private final boolean pinned;
    private final ItemOverrides overrides;

    // Caches live for one bake (a resource reload builds new wrappers). Quads are stable objects inside a baked model,
    // so the original quad itself is the key.
    private static final Map<ResourceLocation, TextureAtlasSprite> SPRITES = new ConcurrentHashMap<>();
    private final Map<TextureAtlasSprite, Map<BakedQuad, BakedQuad>> retextured = new ConcurrentHashMap<>();
    private final Map<TextureAtlasSprite, Map<BakedQuad, BakedQuad>> lifted = new ConcurrentHashMap<>();

    private FlowerPotModel(BakedModel original, @Nullable DyeColor color, @Nullable DecoratedPotTrim design, boolean pinned,
                           ItemOverrides overrides) {
        super(original);
        this.fixedColor = color;
        this.fixedDesign = design;
        this.pinned = pinned;
        this.overrides = overrides;
    }

    /** A new bake: sprites are looked up again. */
    public static void clearSprites() {
        SPRITES.clear();
    }

    public static FlowerPotModel forBlock(BakedModel original) {
        return new FlowerPotModel(original, null, null, false, original.getOverrides());
    }

    public static FlowerPotModel forItem(BakedModel original) {
        record Looks(@Nullable DyeColor color, @Nullable DecoratedPotTrim design) {
        }
        Map<Looks, FlowerPotModel> pinned = new ConcurrentHashMap<>();
        ItemOverrides resolver = new ItemOverrides() {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                Looks looks = new Looks(FlowerPots.colorOf(stack), FlowerPots.designOf(stack));
                if (looks.color() == null && looks.design() == null)
                    return original.getOverrides().resolve(original, stack, level, entity, seed);
                return pinned.computeIfAbsent(looks, l -> new FlowerPotModel(original, l.color(), l.design(), true, ItemOverrides.EMPTY));
            }
        };
        return new FlowerPotModel(original, null, null, false, resolver);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    // Two item-rendering hooks return "the model to draw" and the wrapper would delegate both to the original, which
    // answers itself: apply the original's transform, but answer with this wrapper.
    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        this.originalModel.applyTransform(context, pose, leftHand);
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(this);
    }

    // Item rendering goes through the three-argument path, which would otherwise skip straight to the original.
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return this.getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
                                    @Nullable RenderType renderType) {
        List<BakedQuad> original = this.originalModel.getQuads(state, side, rand, data, renderType);
        if (original.isEmpty() || !ClayworksFlowerPots.ENTRY.active())
            return original;
        DyeColor color = this.pinned ? this.fixedColor : data.get(FlowerPotBlockEntity.COLOR);
        DecoratedPotTrim design = this.pinned ? this.fixedDesign : data.get(FlowerPotBlockEntity.DESIGN);
        TextureAtlasSprite dyed = color == null ? null : sprite(POT_SPRITE.withSuffix("_" + color.getSerializedName()));
        TextureAtlasSprite etched = design == null || !ClayworksFlowerPots.DESIGNS.active() ? null : sprite(designSprite(design));
        if (dyed == null && etched == null)
            return original;
        List<BakedQuad> out = new ArrayList<>(original.size() + 8);
        List<BakedQuad> overlay = new ArrayList<>();
        for (BakedQuad quad : original) {
            if (!quad.getSprite().contents().name().equals(POT_SPRITE)) {
                out.add(quad);
                continue;
            }
            out.add(dyed == null ? quad : this.retextured.computeIfAbsent(dyed, s -> new ConcurrentHashMap<>())
                .computeIfAbsent(quad, q -> copy(q, dyed, 0)));
            if (etched != null && outerSide(quad))
                overlay.add(this.lifted.computeIfAbsent(etched, s -> new ConcurrentHashMap<>()).computeIfAbsent(quad, q -> copy(q, etched, LIFT)));
        }
        out.addAll(overlay);
        return out;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        TextureAtlasSprite base = this.originalModel.getParticleIcon(data);
        DyeColor color = this.pinned ? this.fixedColor : data.get(FlowerPotBlockEntity.COLOR);
        if (color == null || !base.contents().name().equals(POT_SPRITE) || !ClayworksFlowerPots.ENTRY.active())
            return base;
        TextureAtlasSprite dyed = sprite(POT_SPRITE.withSuffix("_" + color.getSerializedName()));
        return dyed != null ? dyed : base;
    }

    /**
     * Whether a pot quad is one of the four outer side faces, the only place a design goes (as on a decorated pot).
     * The bottom, the rim's top and the inner walls sample the same pixels of the pot texture, and showed the design
     * mangled. A side face lies on the pot's outside: vanilla's pot spans 5/16 to 11/16.
     */
    private static boolean outerSide(BakedQuad quad) {
        Direction face = quad.getDirection();
        if (face.getAxis() == Direction.Axis.Y)
            return false;
        int[] vertices = quad.getVertices();
        float plane = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION + (face.getAxis() == Direction.Axis.X ? 0 : 2)]);
        return face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? plane > 0.66F : plane < 0.34F;
    }

    /** A design's flower pot texture in its material: {@code <ns>:block/flower_pot_designs/<path>_<material>}. */
    private static ResourceLocation designSprite(DecoratedPotTrim design) {
        ResourceLocation asset = design.pattern().value().assetId();
        return ResourceLocation.fromNamespaceAndPath(asset.getNamespace(),
            "block/flower_pot_designs/" + asset.getPath() + "_" + design.material().value().assetName());
    }

    /** A sprite from the block atlas, or null (logged once) if it wasn't stitched. */
    @Nullable
    private static TextureAtlasSprite sprite(ResourceLocation id) {
        TextureAtlasSprite sprite = SPRITES.computeIfAbsent(id, i -> {
            TextureAtlasSprite found = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(i);
            if (found.contents().name().equals(MissingTextureAtlasSprite.getLocation()))
                Lifeguard.LOGGER.warn("Lifeguard: no sprite {} in the block atlas; flower pots needing it draw without it", i);
            return found;
        });
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? null : sprite;
    }

    // Same geometry (pushed out along its face by lift), UVs moved from the pot sprite's rectangle in the atlas to the
    // new sprite's.
    private static BakedQuad copy(BakedQuad quad, TextureAtlasSprite to, float lift) {
        TextureAtlasSprite from = quad.getSprite();
        int[] vertices = quad.getVertices().clone();
        float width = from.getU1() - from.getU0();
        float height = from.getV1() - from.getV0();
        Direction face = quad.getDirection();
        for (int i = 0; i < 4; i++) {
            int base = i * IQuadTransformer.STRIDE;
            int uv = base + IQuadTransformer.UV0;
            float u = (Float.intBitsToFloat(vertices[uv]) - from.getU0()) / width;
            float v = (Float.intBitsToFloat(vertices[uv + 1]) - from.getV0()) / height;
            vertices[uv] = Float.floatToRawIntBits(to.getU(u));
            vertices[uv + 1] = Float.floatToRawIntBits(to.getV(v));
            if (lift != 0) {
                int pos = base + IQuadTransformer.POSITION;
                vertices[pos] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[pos]) + face.getStepX() * lift);
                vertices[pos + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[pos + 1]) + face.getStepY() * lift);
                vertices[pos + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[pos + 2]) + face.getStepZ() * lift);
            }
        }
        return new BakedQuad(vertices, quad.getTintIndex(), face, to, quad.isShade(), quad.hasAmbientOcclusion());
    }
}
