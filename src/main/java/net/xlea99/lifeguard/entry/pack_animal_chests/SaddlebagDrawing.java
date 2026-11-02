package net.xlea99.lifeguard.entry.pack_animal_chests;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.xlea99.lifeguard.client.ChestTones;
import net.xlea99.lifeguard.client.ChestTones.Tone;
import net.xlea99.lifeguard.client.ChestTones.Transfer;
import net.xlea99.lifeguard.client.OkLab;
import org.jetbrains.annotations.Nullable;

/**
 * A pack animal's chest recolored for the chest it wears. Its drawing (the donkey's, mule's and llama's 8x8x3 bags,
 * vanilla's and Fresh Animations'; Alex's Mobs' 9x13x19 elephant chests) is a hand-drawn chest too small or too unlike
 * a chest model to draw the chest's own texture onto, with three kinds of pixel told apart by color ({@link #role}): the
 * dark frame, the wood, the gray latch. Each takes the chest's own colors from the matching place on the chest's texture
 * ({@link Roles}: where vanilla's chest is dark or wood on its outside faces, and its latch box). Wood and latch move by
 * {@link Transfer}, from the drawing's own colors (vanilla's chest, for the bags drawn in its colors, so vanilla's chest
 * gives back vanilla's bag; the drawing's own, for Alex's, whose wood spans far more lightness: from vanilla's it blew
 * light woods out to white). The frame keeps the chest's own outline color but at most the drawing's frame-to-wood
 * lightness ratio, so light woods keep a dark outline. A chest without a latch keeps the drawing's latch as drawn: these
 * are recolors, so they always wear one (the user, 2026-10-31: boats and carts draw the real chest, recolors don't).
 * The look was settled on offline mockups first (approved before the code).
 */
final class SaddlebagDrawing {
    /** Darker than this (OKLab lightness) is frame, on the drawing and on vanilla's chest. */
    private static final double FRAME_L = 0.40;
    /** A drawing's pixel grayer than this, and not frame, is latch. */
    private static final double LATCH_CHROMA = 0.03;
    private static final double MIN_FRAME_L = 0.08;
    /** On a 64x64 chest texture: the lid's and body's side faces, the lid's top and the body's bottom (not the insides). */
    private static final int[][] OUTSIDE = {{0, 14, 14, 5}, {14, 14, 14, 5}, {28, 14, 14, 5}, {42, 14, 14, 5},
        {0, 33, 14, 10}, {14, 33, 14, 10}, {28, 33, 14, 10}, {42, 33, 14, 10}, {14, 0, 14, 14}, {28, 19, 14, 14}};
    private static final int[] LATCH = {0, 0, 6, 5};

    private SaddlebagDrawing() {
    }

    enum Role { FRAME, WOOD, LATCH }

    static Role role(int abgr) {
        double[] lab = OkLab.fromAbgr(abgr);
        if (lab[0] < FRAME_L)
            return Role.FRAME;
        return Math.hypot(lab[1], lab[2]) < LATCH_CHROMA ? Role.LATCH : Role.WOOD;
    }

    /** Colors by role (wood without accents for a chest), as tones. */
    record Roles(List<Tone> frame, List<Tone> wood, List<Tone> latch) {
        /** A chest texture's: where vanilla's chest is frame or wood on its outside faces, and its latch box. */
        static Roles of(NativeImage chest, NativeImage vanilla) {
            Map<Integer, Integer> frame = new HashMap<>(), wood = new HashMap<>(), latch = new HashMap<>();
            for (int[] face : OUTSIDE)
                for (int y = face[1]; y < face[1] + face[3]; y++)
                    for (int x = face[0]; x < face[0] + face[2]; x++) {
                        int v = pixel(vanilla, x, y), c = pixel(chest, x, y);
                        if (v >>> 24 != 0 && c >>> 24 != 0)
                            (OkLab.fromAbgr(v)[0] < FRAME_L ? frame : wood).merge(c, 1, Integer::sum);
                    }
            for (int y = LATCH[1]; y < LATCH[1] + LATCH[3]; y++)
                for (int x = LATCH[0]; x < LATCH[0] + LATCH[2]; x++) {
                    int c = pixel(chest, x, y);
                    if (c >>> 24 != 0)
                        latch.merge(c, 1, Integer::sum);
                }
            List<Tone> woods = ChestTones.tones(wood);
            return new Roles(ChestTones.tones(frame), woods.isEmpty() ? woods : ChestTones.withoutAccents(woods), ChestTones.tones(latch));
        }

        /** A drawing's own colors in {@code regions} (scaled by {@code scale}), sorted by {@link #role}. */
        static Roles ofDrawing(NativeImage image, int scale, int[][] regions) {
            Map<Integer, Integer> frame = new HashMap<>(), wood = new HashMap<>(), latch = new HashMap<>();
            for (int[] r : regions)
                for (int y = r[1] * scale; y < (r[1] + r[3]) * scale; y++)
                    for (int x = r[0] * scale; x < (r[0] + r[2]) * scale; x++) {
                        int abgr = image.getPixelRGBA(x, y);
                        if (abgr >>> 24 != 0)
                            (switch (role(abgr)) {
                                case FRAME -> frame;
                                case WOOD -> wood;
                                case LATCH -> latch;
                            }).merge(abgr, 1, Integer::sum);
                    }
            return new Roles(ChestTones.tones(frame), ChestTones.tones(wood), ChestTones.tones(latch));
        }
    }

    /** A chest texture's pixel at (x, y) of the 64x64 layout, whatever its resolution. */
    private static int pixel(NativeImage image, int x, int y) {
        return image.getPixelRGBA(x * image.getWidth() / 64, y * image.getHeight() / 64);
    }

    /**
     * Recolors the drawing's {@code regions} ({x, y, width, height} on an image {@code width} wide; a bigger image scales
     * them) for {@code chest}, moving its colors from {@code from} (null: the drawing's own). False, drawing nothing,
     * when it can't: a chest with no wood, or an image the regions don't fit.
     */
    static boolean draw(NativeImage image, int width, int[][] regions, @Nullable Roles from, Roles chest) {
        int scale = image.getWidth() / width;
        if (scale < 1 || chest.wood().isEmpty())
            return false;
        for (int[] r : regions)
            if ((r[0] + r[2]) * scale > image.getWidth() || (r[1] + r[3]) * scale > image.getHeight())
                return false;
        if (from == null)
            from = Roles.ofDrawing(image, scale, regions);
        if (from.wood().isEmpty() || from.frame().isEmpty())
            return false;
        Transfer wood = Transfer.of(from.wood(), chest.wood());
        List<Tone> outline = chest.frame().isEmpty() ? chest.wood() : chest.frame();
        Transfer frame = Transfer.of(from.frame(), outline);
        @Nullable Transfer latch = chest.latch().isEmpty() || from.latch().isEmpty() ? null : Transfer.of(from.latch(), chest.latch());
        for (int[] r : regions)
            for (int y = r[1] * scale; y < (r[1] + r[3]) * scale; y++)
                for (int x = r[0] * scale; x < (r[0] + r[2]) * scale; x++) {
                    int abgr = image.getPixelRGBA(x, y);
                    if (abgr >>> 24 == 0)
                        continue;
                    Role role = role(abgr);
                    if (role == Role.LATCH && latch == null)
                        continue;
                    int recolored = switch (role) {
                        case WOOD -> wood.apply(abgr);
                        case LATCH -> latch.apply(abgr);
                        case FRAME -> {
                            double l = OkLab.fromAbgr(abgr)[0];
                            yield ChestTones.at(Math.max(MIN_FRAME_L, Math.min(frame.lightness(l), l * wood.toMean() / wood.fromMean())), outline);
                        }
                    };
                    image.setPixelRGBA(x, y, abgr & 0xFF000000 | recolored & 0x00FFFFFF);
                }
        return true;
    }
}
