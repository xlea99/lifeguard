package net.xlea99.lifeguard.entry.zettify_all_paintings;

/**
 * Render-thread flag for the shared {@code zetter_canvas_shading} hooks: set while Zetter's frame renderer draws a canvas
 * that gets vanilla lighting ({@link ZettifyAllPaintings#SHADING}: virtual paintings; {@link ZettifyAllPaintings#SHADING_ALL}:
 * every painting), so the canvas instance draws that one quad lit like a vanilla painting (the same instance also draws
 * for the easel, hands and tooltips, which stay as Zetter draws them).
 */
public final class VirtualPaintingShading {
    public static boolean shadeThisCanvas;

    private VirtualPaintingShading() {
    }
}
