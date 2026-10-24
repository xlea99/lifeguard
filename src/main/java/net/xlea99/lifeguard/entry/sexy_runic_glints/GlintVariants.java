package net.xlea99.lifeguard.entry.sexy_runic_glints;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.mixin.sexy_runic_glints.CompositeRenderTypeAccessor;
import net.xlea99.lifeguard.mixin.sexy_runic_glints.CompositeStateAccessor;
import net.xlea99.lifeguard.mixin.sexy_runic_glints.EmptyTextureStateShardAccessor;

/**
 * Colorize for glints over anything other than an item on the block atlas: worn armor, elytra, shields, tridents.
 *
 * <p>Colorize reads the pixel under the glint, so the glint's render type has to bind the texture underneath. Quark's
 * item glints always lie over the block atlas and bind it themselves, but each armor material, the shield atlas and the
 * trident have textures of their own, and glints are drawn in batches after everything else, so one render type can't
 * know which texture it lies over. A color with colorize therefore gets its own glint render type per surface (texture,
 * and trim for worn armor), built here like the Quark glint it replaces but with full item vertices, {@link GlintShader}
 * and that surface. Tint colors without colorize get a plain variant too, so they fade out with distance correctly.
 * Other colors keep Quark's glints untouched (except Quark's item glints, which always run {@link GlintShader} and so
 * need a variant on any other texture to stay plain).
 *
 * <p>Extends RenderType only to reach its protected render states; never instantiated.
 */
public final class GlintVariants extends RenderType {

    private GlintVariants() {
        super("", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 0, false, false, () -> {
        }, () -> {
        });
        throw new UnsupportedOperationException();
    }

    /** Which of Quark's glint render types a variant stands in for; they differ in glint motion, target and layering. */
    public enum Kind {
        /** Items (glint): item glint motion, drawn into the item/entity target. */
        ITEM,
        /** Translucent items under Fabulous graphics (glintTranslucent): as ITEM, with vanilla's translucent shader. */
        ITEM_TRANSLUCENT,
        /** Items drawn directly (glintDirect): item glint motion, main target. Shields use this. */
        ITEM_DIRECT,
        /** Entities (entityGlint): entity glint motion, item/entity target. */
        ENTITY,
        /** Entities drawn directly (entityGlintDirect): entity glint motion, main target. Tridents use this. */
        ENTITY_DIRECT,
        /** Armor (armorGlint, armorEntityGlint): entity glint motion, pushed toward the camera like the armor. */
        ARMOR
    }

    private record Original(String color, Kind kind) {
    }

    /**
     * What a variant reads under its glint, and how its coordinates were scaled (see {@link GlintContext}). The trim is
     * its sprite's name on the trim atlas, not the sprite: reloading resources rebuilds the atlas, and variants keyed by
     * sprite would pile up with every reload. Banner patterns likewise (each design seen gets its own variant).
     */
    private record Surface(@Nullable ResourceLocation texture, @Nullable ResourceLocation trim, @Nullable BannerLayers banner,
                           float coordScale) {
    }

    /** Quark's glint render types, with their color and kind. Built once at startup, on the render thread. */
    private static final Map<RenderType, Original> QUARK_TYPES = new IdentityHashMap<>();
    /** Variants: Quark glint type -> surface -> variant. Render thread only. */
    private static final Map<RenderType, Map<Surface, RenderType>> VARIANTS = new IdentityHashMap<>();

    public static void registerQuarkType(RenderType type, String color, Kind kind) {
        QUARK_TYPES.put(type, new Original(color, kind));
        if (isItemKind(kind)) // rebuilt with full item vertices for GlintShader (see GlintRenderTypesMixin)
            GlintShards.registerFullVertexType(type);
    }

    /**
     * The glint to draw over the given surface.
     *
     * @param texture the texture under the glint, or null if unknown or the glint can't see all it lies over
     * @param trim worn armor's trim (its sprite on the trim atlas), or null
     * @param banner banner patterns drawn over the surface (a shield's plate), or null
     * @param coordScale how much the glint's texture coordinates are scaled up (1 = not at all)
     */
    public static RenderType forSurface(RenderType glint, @Nullable ResourceLocation texture, @Nullable ResourceLocation trim,
                                        @Nullable BannerLayers banner, float coordScale) {
        Original original = QUARK_TYPES.get(glint);
        if (original == null)
            return glint;
        boolean itemGlint = isItemKind(original.kind());
        // Plain (a shaderpack) means Quark's own glint, except its item glints, which no longer draw like vanilla (full
        // item vertices, GlintShader) and so get a copy that does.
        if (GlintShards.plain())
            return itemGlint ? PLAIN.computeIfAbsent(glint, key -> createPlain(original)) : glint;
        // Quark's item glints already colorize over the block atlas, and are plain otherwise.
        if (itemGlint && TextureAtlas.LOCATION_BLOCKS.equals(texture) && trim == null && banner == null && coordScale == 1)
            return glint;
        // Other glints need a variant to colorize, or for a plain tint to fade out with distance toward "no effect"
        // (vanilla's glint shader fades toward black, which darkens a tint). Quark's item glints need one on any other
        // surface even when plain, since they'd otherwise read the block atlas there.
        GlintMaterial material = GlintMaterials.get(original.color());
        boolean colorizes = texture != null && GlintShards.colorize(material) > 0;
        boolean tint = material != null && material.blend() == GlintMaterial.Blend.TINT;
        if (!colorizes && !tint && !itemGlint)
            return glint;
        Surface surface = colorizes ? new Surface(texture, trim, banner, coordScale) : new Surface(null, null, null, coordScale);
        return VARIANTS.computeIfAbsent(glint, key -> new HashMap<>()).computeIfAbsent(surface, key -> create(original, key));
    }

    /** Quark's glint (see its GlintRenderTypes) for the given kind, with full item vertices and {@link GlintShader}. */
    private static RenderType create(Original original, Surface surface) {
        String color = original.color();
        String name = "lifeguard_" + original.kind().name().toLowerCase() + "_glint_" + color + "_" + surface.texture()
            + (surface.trim() == null ? "" : "_trim_" + surface.trim())
            + (surface.banner() == null ? "" : "_banner_" + Integer.toHexString(surface.banner().hashCode()))
            + "_x" + surface.coordScale();
        CompositeState.CompositeStateBuilder state = recipe(original, GlintShards.shader(color, RENDERTYPE_GLINT_SHADER,
            surface.texture(), surface.trim(), surface.banner(), surface.coordScale()));
        RenderType type = RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false,
            state.createCompositeState(false));
        GlintShards.registerFullVertexType(type);
        // Each variant also gets its own buffer from the buffer source (see BufferSourceMixin).
        CREATED.add(type);
        return type;
    }

    /** Plain copies of Quark's item glints, for shaderpacks: Quark glint type -> copy. Render thread only. */
    private static final Map<RenderType, RenderType> PLAIN = new IdentityHashMap<>();

    /**
     * Quark's item glint exactly as Quark builds it (vanilla's vertex format and glint shader, which shaderpacks swap for
     * their own glint program), for use under a shaderpack, where ours can't draw.
     */
    private static RenderType createPlain(Original original) {
        ShaderStateShard shader = switch (original.kind()) {
            case ITEM_TRANSLUCENT -> RENDERTYPE_GLINT_TRANSLUCENT_SHADER;
            case ITEM_DIRECT -> RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER;
            default -> RENDERTYPE_GLINT_SHADER;
        };
        RenderType type = RenderType.create("lifeguard_plain_" + original.kind().name().toLowerCase() + "_glint_"
            + original.color(), DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            recipe(original, shader).createCompositeState(false));
        CREATED.add(type);
        return type;
    }

    /** The render states of Quark's glint of the given kind (see its GlintRenderTypes), with the given shader. */
    private static CompositeState.CompositeStateBuilder recipe(Original original, ShaderStateShard shader) {
        String color = original.color();
        ResourceLocation glintTexture = ResourceLocation.fromNamespaceAndPath("quark", "textures/glint/enchanted_item_glint_" + color + ".png");
        CompositeState.CompositeStateBuilder state = CompositeState.builder()
            .setShaderState(shader)
            .setTextureState(GlintShards.texture(color, new TextureStateShard(glintTexture, true, false)))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY) // vanilla's own object: other mods recognize glints by it
            .setTexturingState(GlintShards.texturing(color, isItemKind(original.kind()) ? GLINT_TEXTURING : ENTITY_GLINT_TEXTURING));
        if (original.kind() == Kind.ITEM || original.kind() == Kind.ITEM_TRANSLUCENT || original.kind() == Kind.ENTITY)
            state.setOutputState(ITEM_ENTITY_TARGET);
        if (original.kind() == Kind.ARMOR)
            state.setLayeringState(VIEW_OFFSET_Z_LAYERING);
        return state;
    }

    private static boolean isItemKind(Kind kind) {
        return kind == Kind.ITEM || kind == Kind.ITEM_TRANSLUCENT || kind == Kind.ITEM_DIRECT;
    }

    /** Every variant made so far. Render thread only. */
    private static final Set<RenderType> CREATED = Collections.newSetFromMap(new IdentityHashMap<>());

    public static boolean isVariant(RenderType type) {
        return CREATED.contains(type);
    }

    /** The texture a render type draws (an item sheet, {@code armorCutoutNoCull(texture)}, ...), if it has one. */
    @Nullable
    public static ResourceLocation textureOf(@Nullable RenderType type) {
        if (!(type instanceof CompositeRenderTypeAccessor composite))
            return null;
        return ((EmptyTextureStateShardAccessor) ((CompositeStateAccessor) (Object) composite.lifeguard$state())
            .lifeguard$textureState()).lifeguard$cutoutTexture().orElse(null);
    }
}
