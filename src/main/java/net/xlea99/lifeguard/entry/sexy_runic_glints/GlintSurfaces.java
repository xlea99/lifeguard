package net.xlea99.lifeguard.entry.sexy_runic_glints;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * What a glint drawn in a pass of its own lies over.
 *
 * <p>Most glints are drawn together with the thing under them (the foil buffer helpers pair the two), so the render type
 * under them tells {@link GlintVariants} which texture to read. Some renderers instead draw the model first and the glint
 * afterward in a separate pass that isn't told what was drawn: vanilla's worn armor does, with the armor's trim and dye
 * drawn in passes of their own too. For those, the renderer's hook opens a surface around the whole thing, notes what it
 * draws, and has its glint chosen here:
 *
 * <pre>
 * GlintSurfaces.begin();                  // e.g. at the start of drawing an armor piece
 * GlintSurfaces.note(texture, color);     // the model's texture and color (the first one noted counts)
 * GlintSurfaces.trim(sprite);             // an overlay drawn over it, if any
 * ... glint = GlintSurfaces.glint(glint); // where the glint's render type is picked
 * ... color = GlintSurfaces.color(color); // where the glint's vertex color is set
 * GlintSurfaces.end();
 * </pre>
 *
 * Surfaces nest, in case one renderer's pass happens inside another's. Render thread only.
 */
public final class GlintSurfaces {

    private static final class Surface {
        @Nullable
        ResourceLocation texture;
        @Nullable
        ResourceLocation trim;
        int color = -1;
    }

    private static final Deque<Surface> OPEN = new ArrayDeque<>();

    private GlintSurfaces() {
    }

    public static void begin() {
        OPEN.push(new Surface());
    }

    public static void end() {
        OPEN.poll();
    }

    /** A model drawn on the open surface (its color as ARGB): the first one counts (leather's second is its overlay). */
    public static void note(ResourceLocation texture, int color) {
        Surface surface = OPEN.peek();
        if (surface == null || surface.texture != null)
            return;
        surface.texture = texture;
        surface.color = color;
    }

    /** A trim drawn over the open surface: its sprite on the armor trim atlas. */
    public static void trim(TextureAtlasSprite sprite) {
        Surface surface = OPEN.peek();
        if (surface != null)
            surface.trim = sprite.contents().name();
    }

    /** The glint to draw over the open surface (see {@link GlintVariants#forSurface}); the glint as is if none. */
    public static RenderType glint(RenderType glint) {
        Surface surface = OPEN.peek();
        return surface == null ? glint : GlintVariants.forSurface(glint, surface.texture, surface.trim, null, 1);
    }

    /** The open surface's color (white if undyed) for the glint's vertices: colorize keeps a dye's brightness. */
    public static int color(int original) {
        Surface surface = OPEN.peek();
        return surface == null ? original : surface.color;
    }
}
