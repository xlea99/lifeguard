package net.xlea99.lifeguard.entry.sexy_runic_glints;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TexturingStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * Stand-ins for the render states a glint is drawn with: its texture, how it's blended with the item underneath
 * (texturing) and, for item glints, its shader. Each one looks up its color's {@link GlintMaterial} whenever it's
 * applied, so settings change live; with no settings the glint looks exactly like Quark's (item glints always go through
 * {@link GlintShader}, whose plain path is vanilla's shader).
 */
public final class GlintShards {

    private GlintShards() {
    }

    /**
     * Which texture the glint uses: the color's own ({@link GlintMaterials#texture}), else Quark's. While a shaderpack
     * is on, always Quark's path (where {@code dye_depot_runes} keeps plain glints for Dye Depot's colors).
     */
    public static EmptyTextureStateShard texture(String color, EmptyTextureStateShard original) {
        return new EmptyTextureStateShard(() -> {
            ResourceLocation texture = plain() ? null : GlintMaterials.texture(color);
            if (texture == null) {
                original.setupRenderState();
                return;
            }
            bindBlurred(texture);
        }, original::clearRenderState);
    }

    private static void bindBlurred(ResourceLocation texture) {
        Minecraft.getInstance().getTextureManager().getTexture(texture).setFilter(true, false); // blurred, like vanilla's glints
        RenderSystem.setShaderTexture(0, texture);
    }

    /** Whether rune glints draw plain (Quark's own): under an Iris shaderpack ({@link Shaderpacks}). */
    public static boolean plain() {
        return Shaderpacks.active();
    }

    /**
     * Glint render types that take full item vertices for {@link GlintShader}: Quark's item glints, and the variants from
     * {@link GlintVariants}. Render thread only.
     */
    private static final Set<RenderType> FULL_VERTEX_TYPES = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void registerFullVertexType(RenderType type) {
        FULL_VERTEX_TYPES.add(type);
    }

    public static boolean isFullVertexType(RenderType type) {
        return FULL_VERTEX_TYPES.contains(type);
    }

    /** How strongly a color's glint is colorized: its {@code colorize} for tint colors, else 0 (plain). */
    static float colorize(@Nullable GlintMaterial material) {
        return material != null && material.blend() == GlintMaterial.Blend.TINT ? material.colorize() : 0;
    }

    /**
     * The colorize the shader shard just set up for the glint being drawn (0 = plain); the texturing shard, set up after
     * it, picks its blend to match. Render thread only.
     */
    private static float activeColorize;

    /**
     * Draws a glint with {@link GlintShader}, for render types that carry full item vertices (which vanilla's glint
     * shaders can't read). With colorize it also gets the lightmap and the texture the glint lies over:
     *
     * @param itemTexture the texture under the glint (the block atlas for items, the armor's for armor, ...), or null if
     *        the glint can't see what it lies over (then it's a plain tint, never colorized)
     * @param trim worn armor's trim (its sprite on the trim atlas), drawn over the armor in a pass of its own that the
     *        glint can't see: the shader also gets the trim atlas and the sprite's place on it (looked up when drawn, so
     *        it follows resource reloads), and reads the trim's pixel where it has one
     * @param banner banner patterns drawn over the surface in passes of their own (a shield's plate): the shader gets each
     *        pattern's place on the surface's atlas (looked up when drawn) and color, and layers them itself
     * @param itemCoordScale how much the glint's texture coordinates have been scaled up ({@link GlintContext}), undone
     *        to find the item's pixel; 1 if they weren't
     */
    public static ShaderStateShard shader(String color, ShaderStateShard original, @Nullable ResourceLocation itemTexture,
                                          @Nullable ResourceLocation trim, @Nullable BannerLayers banner, float itemCoordScale) {
        return new ShaderStateShard() {

            /** Textures that were in slots 1-3 (1 and 2 are normally overlay and lightmap) before; put back after. */
            private int savedSampler1 = -1;
            private int savedSampler2 = -1;
            private int savedSampler3 = -1;

            @Override
            public void setupRenderState() {
                ShaderInstance shader = GlintShader.colorize();
                if (shader == null) {
                    activeColorize = 0;
                    original.setupRenderState();
                    return;
                }
                GlintMaterial material = GlintMaterials.get(color);
                float colorize = itemTexture == null ? 0 : colorize(material);
                activeColorize = colorize;
                RenderSystem.setShader(() -> shader);
                // The shader is shared by every glint that uses it, so all of its settings are set every time.
                shader.safeGetUniform("Colorize").set(colorize);
                shader.safeGetUniform("HasTrim").set(trim != null && colorize > 0 ? 1.0F : 0.0F);
                shader.safeGetUniform("BannerCount").set(banner != null && colorize > 0 ? banner.layers().size() : 0.0F);
                shader.safeGetUniform("ItemCoordScale").set(itemCoordScale);
                // A plain glint fades out with distance toward "no effect": black for an added glint, which is what
                // vanilla does, but 50% gray for a tint, which a fade to black would darken instead.
                boolean tint = material != null && material.blend() == GlintMaterial.Blend.TINT;
                shader.safeGetUniform("FadeTo").set(tint ? 0.5F : 0.0F);
                shader.safeGetUniform("Strength").set(tint ? material.strength() : 1.0F);
                shader.safeGetUniform("Selective").set(tint ? material.selective() : 0.0F);
                setRamp(shader, tint && colorize > 0 ? material.ramp() : null);
                if (colorize <= 0)
                    return;
                savedSampler1 = RenderSystem.getShaderTexture(1);
                savedSampler2 = RenderSystem.getShaderTexture(2);
                RenderSystem.setShaderTexture(1, itemTexture);
                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                if (trim != null) {
                    TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                        .getAtlas(Sheets.ARMOR_TRIMS_SHEET).getSprite(trim);
                    savedSampler3 = RenderSystem.getShaderTexture(3);
                    RenderSystem.setShaderTexture(3, Sheets.ARMOR_TRIMS_SHEET);
                    setRect(shader.safeGetUniform("TrimRect"), sprite);
                }
                if (banner != null)
                    setBanner(shader, banner);
            }

            @Override
            public void clearRenderState() {
                activeColorize = 0;
                if (savedSampler1 >= 0) {
                    RenderSystem.setShaderTexture(1, savedSampler1);
                    RenderSystem.setShaderTexture(2, savedSampler2);
                    savedSampler1 = savedSampler2 = -1;
                }
                if (savedSampler3 >= 0) {
                    RenderSystem.setShaderTexture(3, savedSampler3);
                    savedSampler3 = -1;
                }
            }
        };
    }

    /** The banner layers' places on their atlas (the one bound as the item texture) and colors, for the shader. */
    private static void setBanner(ShaderInstance shader, BannerLayers banner) {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(banner.atlas());
        setRect(shader.safeGetUniform("BannerPlate"), atlas.getSprite(banner.plate()));
        for (int i = 0; i < banner.layers().size(); i++) {
            BannerLayers.Layer layer = banner.layers().get(i);
            setRect(shader.safeGetUniform("BannerRect" + i), atlas.getSprite(layer.sprite()));
            shader.safeGetUniform("BannerColor" + i)
                .set((layer.rgb() >> 16 & 255) / 255F, (layer.rgb() >> 8 & 255) / 255F, (layer.rgb() & 255) / 255F);
        }
    }

    private static void setRect(AbstractUniform uniform, TextureAtlasSprite sprite) {
        uniform.set(sprite.getU0(), sprite.getV0(), sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
    }

    /** A tint color's ramp ({@link GlintMaterial.Ramp}) for the shader; RampCount 0 = none, colorize with one color. */
    private static void setRamp(ShaderInstance shader, @Nullable GlintMaterial.Ramp ramp) {
        shader.safeGetUniform("RampCount").set(ramp == null ? 0.0F : ramp.stops().length);
        if (ramp == null)
            return;
        for (int i = 0; i < ramp.stops().length; i++) {
            float[] stop = ramp.stops()[i];
            shader.safeGetUniform("Ramp" + i).set(stop[0], stop[1], stop[2], stop[3]);
        }
        shader.safeGetUniform("RampField").set(ramp.fieldLow(), ramp.fieldHigh());
        shader.safeGetUniform("RampRay").set(ramp.rayLow(), ramp.rayHigh());
    }

    /**
     * How the glint is blended with what's under it (its motion stays vanilla's).
     *
     * <p>The blend lives here rather than in a transparency state of our own because other mods recognize glints by their
     * transparency state: Iris files any render type whose transparency isn't vanilla's own GLINT_TRANSPARENCY object as
     * ordinary translucency, draws it in the wrong order, and the glint's equal-depth test then misses the item it lies
     * on. So glints keep vanilla's transparency state, and this state, set up right after it, changes the blend for the
     * draw where the color asks for it.
     */
    public static TexturingStateShard texturing(String color, TexturingStateShard original) {
        return new TexturingStateShard(original + "_" + color, () -> {
            original.setupRenderState();
            GlintMaterial material = GlintMaterials.get(color);
            // plain: Quark's blend (see plain())
            if (material == null || plain() || material.blend() != GlintMaterial.Blend.TINT)
                return;
            // match what this glint's shader shard set up (colorize only happens in GlintShader)
            if (colorizing())
                setupColorize();
            else
                setupTint();
            if (material.ramp() != null && !colorizing())
                setupPlainRampTexture(color);
        }, () -> {
            original.clearRenderState();
            if (savedGlintAlpha >= 0) {
                RenderSystem.setShaderGlintAlpha(savedGlintAlpha);
                savedGlintAlpha = -1;
            }
        });
    }

    /** Glint Strength in force before a tint glint swapped it out; restored when the tint is done. */
    private static float savedGlintAlpha = -1;

    /** Whether the glint being drawn is colorized: drawn by {@link GlintShader}, whose shard set colorize up. */
    private static boolean colorizing() {
        return activeColorize > 0 && RenderSystem.getShader() != null && RenderSystem.getShader() == GlintShader.colorize();
    }

    /**
     * A ramp color's texture is a gray ray map, which only means something to the colorize shader; drawn as a plain tint
     * (where the glint can't see the item, or a renderer we don't know draws Quark's glint with vanilla's shader) it
     * would darken the item between rays. Plain, it uses the color's own tint texture instead.
     */
    private static void setupPlainRampTexture(String color) {
        ResourceLocation texture = GlintMaterials.ownTexture(color);
        if (texture != null)
            bindBlurred(texture);
    }

    /**
     * 2x multiply: 50% gray leaves the item alone, darker darkens and recolors it, lighter brightens it. The glint shader
     * scales the texture by the Glint Strength option (0.75 by default) before blending, which would turn "leave alone"
     * into "darken by a quarter". So run at full strength for this draw, keeping only strength 0 meaning off. A multiply
     * can't be scaled by a slider without a shader.
     */
    private static void setupTint() {
        savedGlintAlpha = RenderSystem.getShaderGlintAlpha();
        if (savedGlintAlpha <= 0) {
            RenderSystem.blendFuncSeparate(SourceFactor.ZERO, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE); // draws nothing
            return;
        }
        RenderSystem.setShaderGlintAlpha(1.0F);
        RenderSystem.blendFuncSeparate(SourceFactor.DST_COLOR, DestFactor.SRC_COLOR, SourceFactor.ZERO, DestFactor.ONE);
    }

    /**
     * Colorize: the shader works out the item's final color itself and draws it over the item (premultiplied: result =
     * shader color + item x (1 - shader alpha)) instead of multiplying, so it isn't capped at doubling a channel. Full
     * Glint Strength like {@link #setupTint}; strength 0 still turns the glint off.
     */
    private static void setupColorize() {
        savedGlintAlpha = RenderSystem.getShaderGlintAlpha();
        if (savedGlintAlpha <= 0) {
            RenderSystem.blendFuncSeparate(SourceFactor.ZERO, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE); // draws nothing
            return;
        }
        RenderSystem.setShaderGlintAlpha(1.0F);
        RenderSystem.blendFuncSeparate(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
    }
}
