package net.xlea99.lifeguard.entry.zetter_canvas_texture_cleanup;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class ZetterCanvasTextureCleanup {
    /**
     * None: Zetter's client leaked canvas textures. CanvasRenderer.addCanvas, run every time a canvas's data arrives (about
     * once a second while painting, the server's resync; every painting coming into view; every virtual painting on
     * each world join), drops the old CanvasRenderer.Instance WITHOUT closing it: its DynamicTexture (GPU texture and
     * native image) is lost. And Instance.close() closes the texture but leaves it registered in the TextureManager, which
     * keeps a fresh entry per reload. Now the replaced instance is closed, and closing also releases the registration.
     * Found by reading the code (audit U5, 2026-11-01). (Zetter 0.21.7, CanvasRenderer.)
     */
    public static final Entry ENTRY = Entries.fix("zetter_canvas_texture_cleanup", Taste.NONE).home("zetter")
        .requiresMods("zetter")
        .side(Side.CLIENT).restart(Restart.GAME);

    private ZetterCanvasTextureCleanup() {
    }
}
