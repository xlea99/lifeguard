package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * A dyed Chaindelier's painted glow in its color ({@link Chaindeliers}). The beam is tinted by the renderer; this does
 * the glow painted into the model (the panes underneath, the candle flames). Every quad textured with one of
 * {@link #GLOWING} gets copies drawn over it with that texture's glow masks ({@link SpectreGlowMasks}), each tinted by
 * {@link ChaindelierClient}'s color handlers with its own level. An undyed one (no color in its model data) draws
 * Species' model untouched, and the item does the same off its stack.
 */
public final class ChaindelierModel extends BakedModelWrapper<BakedModel> {
    /** Species' Chaindelier textures with glow painted in. */
    public static final List<ResourceLocation> GLOWING = List.of(ResourceLocation.fromNamespaceAndPath("species", "block/chaindelier_bottom"),
        ResourceLocation.fromNamespaceAndPath("species", "block/chaindelier_side"));

    private final boolean pinnedDyed;
    private final ItemOverrides overrides;

    // For one bake; a resource reload builds new wrappers.
    private final Map<ResourceLocation, TextureAtlasSprite[]> masks = new ConcurrentHashMap<>();
    private final Map<BakedQuad, List<BakedQuad>> overlays = new ConcurrentHashMap<>();

    private ChaindelierModel(BakedModel original, boolean pinnedDyed, ItemOverrides overrides) {
        super(original);
        this.pinnedDyed = pinnedDyed;
        this.overrides = overrides;
    }

    public static ChaindelierModel forBlock(BakedModel original) {
        return new ChaindelierModel(original, false, original.getOverrides());
    }

    /** The item's color is on the stack: a dyed one resolves to a wrapper that always draws the glow (tinted per stack). */
    public static ChaindelierModel forItem(BakedModel original) {
        ChaindelierModel[] dyed = new ChaindelierModel[1];
        ItemOverrides resolver = new ItemOverrides() {
            @Override
            @Nullable
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                if (!Chaindeliers.enabled() || Chaindeliers.colorOf(stack) == null)
                    return original.getOverrides().resolve(original, stack, level, entity, seed);
                synchronized (dyed) {
                    if (dyed[0] == null)
                        dyed[0] = new ChaindelierModel(original, true, ItemOverrides.EMPTY);
                    return dyed[0];
                }
            }
        };
        return new ChaindelierModel(original, false, resolver);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    // As FlowerPotModel: item rendering asks these for "the model to draw", which the wrapper would hand to the original,
    // swapping itself out.
    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        this.originalModel.applyTransform(context, pose, leftHand);
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(this);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return this.getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
                                    @Nullable RenderType renderType) {
        List<BakedQuad> original = this.originalModel.getQuads(state, side, rand, data, renderType);
        Integer color = data.get(Chaindeliers.COLOR_PROPERTY);
        boolean dyed = this.pinnedDyed || color != null && color != Chaindeliers.DEFAULT_COLOR;
        if (!dyed || original.isEmpty() || !Chaindeliers.enabled())
            return original;
        List<BakedQuad> out = null;
        for (BakedQuad quad : original) {
            List<BakedQuad> overlay = this.overlays.computeIfAbsent(quad, this::overlay);
            if (overlay.isEmpty())
                continue;
            if (out == null)
                out = new ArrayList<>(original);
            out.addAll(overlay);
        }
        return out == null ? original : out;
    }

    /** One copy of a glowing quad per level with any glow in it, drawn over it; none for any other quad. */
    private List<BakedQuad> overlay(BakedQuad quad) {
        ResourceLocation texture = quad.getSprite().contents().name();
        if (!GLOWING.contains(texture))
            return List.of();
        TextureAtlasSprite[] levels = this.masks.computeIfAbsent(texture, ChaindelierModel::masksOf);
        List<BakedQuad> copies = new ArrayList<>();
        for (int level = 0; level < levels.length; level++)
            if (levels[level] != null)
                copies.add(retexture(quad, levels[level], ChaindelierClient.TINT_BASE + level));
        return copies;
    }

    /** A texture's mask per level, null for a level it has no glow at (or that wasn't stitched). */
    private static TextureAtlasSprite[] masksOf(ResourceLocation texture) {
        TextureAtlasSprite[] levels = new TextureAtlasSprite[SpectreGlowMasks.LEVELS + 1];
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        for (int level = 0; level <= SpectreGlowMasks.LEVELS; level++) {
            TextureAtlasSprite sprite = atlas.apply(SpectreGlowMasks.maskId(texture, level));
            if (!sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) && hasPixels(sprite))
                levels[level] = sprite;
        }
        return levels;
    }

    private static boolean hasPixels(TextureAtlasSprite sprite) {
        var contents = sprite.contents();
        for (int y = 0; y < contents.height(); y++)
            for (int x = 0; x < contents.width(); x++)
                if (!contents.isTransparent(0, x, y))
                    return true;
        return false;
    }

    // As FlowerPotModel's copy: same geometry, UVs moved onto the other sprite (1.21 sprites take 0..1), and a tint.
    private static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite to, int tint) {
        TextureAtlasSprite from = quad.getSprite();
        int[] vertices = quad.getVertices().clone();
        float width = from.getU1() - from.getU0();
        float height = from.getV1() - from.getV0();
        for (int i = 0; i < 4; i++) {
            int uv = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = (Float.intBitsToFloat(vertices[uv]) - from.getU0()) / width;
            float v = (Float.intBitsToFloat(vertices[uv + 1]) - from.getV0()) / height;
            vertices[uv] = Float.floatToRawIntBits(to.getU(u));
            vertices[uv + 1] = Float.floatToRawIntBits(to.getV(v));
        }
        return new BakedQuad(vertices, tint, quad.getDirection(), to, quad.isShade(), quad.hasAmbientOcclusion());
    }
}
