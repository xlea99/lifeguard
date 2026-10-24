package net.xlea99.lifeguard.entry.sexy_runic_glints;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Special circumstances for the glints drawn into a buffer source, given by wrapping the source a renderer passes on:
 *
 * <ul>
 * <li>{@code coordScale}: the glints' texture coordinates are scaled up by this much (see {@link GlintUvScale}), and
 * colorize undoes it to find the pixel under the glint;</li>
 * <li>{@code seesSurface = false}: something is drawn over the glinted surface in a pass the glint can't see (a
 * renderer's own overlay), so the glint must stay plain; colorize would paint over it;</li>
 * <li>{@code banner}: banner patterns are drawn over the glinted plate in passes of their own, and colorize reads them
 * itself ({@link BannerLayers}).</li>
 * </ul>
 *
 * The foil buffer helper hook reads these back ({@link #coordScale}, {@link #seesSurface}, {@link #banner}) when it picks
 * the glint.
 */
public record GlintContext(MultiBufferSource source, float coordScale, boolean seesSurface, @Nullable BannerLayers banner)
    implements MultiBufferSource {

    /** {@code source} with the given circumstances, or {@code source} itself if they're the ordinary ones. */
    public static MultiBufferSource wrap(MultiBufferSource source, float coordScale, boolean seesSurface) {
        return wrap(source, coordScale, seesSurface, null);
    }

    /** {@code source} (wrapped or not) with banner patterns over its glinted plate as well. */
    public static MultiBufferSource withBanner(MultiBufferSource source, BannerLayers banner) {
        if (source instanceof GlintContext context)
            return wrap(context.source(), context.coordScale(), context.seesSurface(), banner);
        return wrap(source, 1, true, banner);
    }

    private static MultiBufferSource wrap(MultiBufferSource source, float coordScale, boolean seesSurface,
                                          @Nullable BannerLayers banner) {
        if (!Float.isFinite(coordScale) || coordScale <= 0 || Math.abs(coordScale - 1) < 1e-4)
            coordScale = 1;
        if (coordScale == 1 && seesSurface && banner == null)
            return source;
        return new GlintContext(source, coordScale, seesSurface, banner);
    }

    public static float coordScale(MultiBufferSource source) {
        return source instanceof GlintContext context ? context.coordScale() : 1;
    }

    public static boolean seesSurface(MultiBufferSource source) {
        return !(source instanceof GlintContext context) || context.seesSurface();
    }

    @Nullable
    public static BannerLayers banner(MultiBufferSource source) {
        return source instanceof GlintContext context ? context.banner() : null;
    }

    /** Glint buffers scale texture coordinates by {@code coordScale}; everything else passes through. */
    @Override
    public VertexConsumer getBuffer(RenderType type) {
        VertexConsumer buffer = source.getBuffer(type);
        return coordScale != 1 && isGlint(type) ? new Scaled(buffer, coordScale) : buffer;
    }

    private static final Map<RenderType, Boolean> IS_GLINT = new IdentityHashMap<>();

    /** Glint render types (vanilla's, Quark's rune ones, ours) all have "glint" in their name. Render thread only. */
    private static boolean isGlint(RenderType type) {
        return IS_GLINT.computeIfAbsent(type, t -> t.toString().contains("glint"));
    }

    /** Passes everything through, scaling texture coordinates. */
    private record Scaled(VertexConsumer delegate, float factor) implements VertexConsumer {

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u * factor, v * factor);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
