package net.xlea99.lifeguard.entry.chest_boat_chests;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * A chest's own drawing shrunk onto a boat's little chest. Both are box unwraps, face for face: the block chest's body
 * 14x10x14 and lid 14x5x14 with a 2x4x1 latch (ChestRenderer), onto a {@link Layout}: vanilla's chest boat (12x8x12,
 * 12x4x12, the same latch; every mod drawing its boats with vanilla's ChestBoatModel) or Alex's Caves' own chest model
 * (12x7x12, 12x5x12, a 2x3x1 latch). Each boat face is its block face with interior rows and columns dropped, the ones
 * whose loss changes the least, never the outer frame. Which block face (and flip) feeds which face was learned from
 * each target's own hand-made chest, Mojang's and Alex's (measured offline from their boat and chest textures);
 * where vanilla's sides tie, each side takes its own.
 *
 * <p>Two rules keep a chest's design whole, as Mojang's own shrink does (it dropped the same off-center columns on the
 * lid and body fronts): a side's lid and body drop the SAME columns, so a design across the seam (a latch, C&C azalea's
 * ring) stays aligned, and none from the middle; rows go from the half away from the seam, where latches and rings
 * sit. Against Mojang's hand-made oak chest boat: within a shade on 77% of its pixels ({@code shrink_rules.py},
 * 2026-10-31). A chest without a latch leaves the latch's spot blank (half of Quark's, most of Woodworks'): the boat's
 * latch is left blank too, and boats draw cut out, so it's gone the same way.
 */
final class ChestDrawing {
    private static final int TOP = 0, BOTTOM = 1, EAST = 2, NORTH = 3, WEST = 4, SOUTH = 5;
    /** The block chest texture's layout width. */
    private static final int CHEST_LAYOUT = 64;
    /** Of a 14-wide face, the columns never dropped: where latches, rings and emblems sit. */
    private static final IntPredicate OUTSIDE_MIDDLE = i -> i < 4 || i > 9;
    private static final IntPredicate ANY = i -> true;

    /** One face of a box, where Mojang's unwrap puts it on the texture. */
    private record Face(int x, int y, int w, int h) {
    }

    private record Copy(Face from, Face to, boolean flipH, boolean flipV) {
    }

    /**
     * Where a target draws its chest: its texture's layout width, the four sides as {lid, body}, the tops and bottoms,
     * the latch, and which rows of a (flipped) lid and body side may go: the half away from the seam (the lid face's
     * last rows and the body face's first, after the flips, on both targets).
     */
    record Layout(int width, List<Copy[]> sides, List<Copy> tops, List<Copy> latch, IntPredicate lidRows, IntPredicate bodyRows) {
    }

    private static Face[] box(int u, int v, int w, int h, int d) {
        return new Face[] {new Face(u + d, v, w, d), new Face(u + d + w, v, w, d), new Face(u, v + d, d, h),
            new Face(u + d, v + d, w, h), new Face(u + d + w, v + d, d, h), new Face(u + 2 * d + w, v + d, w, h)};
    }

    private static final Face[] BODY = box(0, 19, 14, 10, 14), LID = box(0, 0, 14, 5, 14), LOCK = box(0, 0, 2, 4, 1);

    /** The four sides: the target's front (north) is the block chest's south, and so on round; all flipped both ways. */
    private static List<Copy[]> sides(Face[] lid, Face[] body) {
        return List.of(
            new Copy[] {new Copy(LID[EAST], lid[EAST], true, true), new Copy(BODY[EAST], body[EAST], true, true)},
            new Copy[] {new Copy(LID[SOUTH], lid[NORTH], true, true), new Copy(BODY[SOUTH], body[NORTH], true, true)},
            new Copy[] {new Copy(LID[WEST], lid[WEST], true, true), new Copy(BODY[WEST], body[WEST], true, true)},
            new Copy[] {new Copy(LID[NORTH], lid[SOUTH], true, true), new Copy(BODY[NORTH], body[SOUTH], true, true)});
    }

    private static List<Copy> latch(Face[] lock, boolean bottomFlipH) {
        return List.of(
            new Copy(LOCK[BOTTOM], lock[TOP], false, false), new Copy(LOCK[TOP], lock[BOTTOM], bottomFlipH, !bottomFlipH),
            new Copy(LOCK[EAST], lock[EAST], true, true), new Copy(LOCK[SOUTH], lock[NORTH], true, true),
            new Copy(LOCK[WEST], lock[WEST], true, true), new Copy(LOCK[NORTH], lock[SOUTH], true, true));
    }

    /** Vanilla's ChestBoatModel, which every chest boat but Alex's Caves' is drawn with (128 wide). */
    static final Layout CHEST_BOAT;
    /** Alex's Caves' ACBoatChestModel and its own chest texture (64 wide). */
    static final Layout ALEXS_CAVES;

    static {
        Face[] body = box(0, 76, 12, 8, 12), lid = box(0, 59, 12, 4, 12);
        CHEST_BOAT = new Layout(128, sides(lid, body),
            List.of(new Copy(BODY[BOTTOM], body[TOP], true, false), new Copy(BODY[TOP], body[BOTTOM], false, true),
                new Copy(LID[BOTTOM], lid[TOP], false, true), new Copy(LID[TOP], lid[BOTTOM], true, false)),
            latch(box(0, 59, 2, 4, 1), false), i -> i == 1 || i == 2, i -> i >= 5 && i <= 8);
        Face[] acBody = box(0, 18, 12, 7, 12), acLid = box(0, 0, 12, 5, 12);
        ALEXS_CAVES = new Layout(64, sides(acLid, acBody),
            List.of(new Copy(BODY[BOTTOM], acBody[TOP], false, true), new Copy(BODY[TOP], acBody[BOTTOM], false, false),
                new Copy(LID[BOTTOM], acLid[TOP], false, true), new Copy(LID[TOP], acLid[BOTTOM], true, false)),
            latch(box(0, 1, 2, 3, 1), true), i -> i == 1 || i == 2, i -> i >= 4 && i <= 8);
    }

    private ChestDrawing() {
    }

    /** Draws {@code chest}'s drawing over the chest on {@code target}, laid out as {@code layout} (any resolution). */
    static void draw(NativeImage chest, NativeImage target, Layout layout) {
        double from = chest.getWidth() / (double) CHEST_LAYOUT;
        int to = Math.max(1, target.getWidth() / layout.width());
        for (Copy[] side : layout.sides()) {
            int[][] lid = read(chest, side[0], from), body = read(chest, side[1], from);
            List<Integer> cols = keptColumns(stack(lid, body), side[0].to().w(), OUTSIDE_MIDDLE);
            lid = pick(lid, keptRows(pick(lid, all(lid.length), cols), side[0].to().h(), layout.lidRows()), cols);
            body = pick(body, keptRows(pick(body, all(body.length), cols), side[1].to().h(), layout.bodyRows()), cols);
            write(target, side[0].to(), lid, to, false);
            write(target, side[1].to(), body, to, false);
        }
        for (Copy copy : layout.tops())
            write(target, copy.to(), shrink(read(chest, copy, from), copy.to(), OUTSIDE_MIDDLE), to, false);
        for (Copy copy : layout.latch())
            write(target, copy.to(), shrink(read(chest, copy, from), copy.to(), ANY), to, true);
    }

    private static int[][] shrink(int[][] face, Face to, IntPredicate allowed) {
        List<Integer> cols = keptColumns(face, to.w(), allowed);
        return pick(face, keptRows(pick(face, all(face.length), cols), to.h(), allowed), cols);
    }

    private static int[][] read(NativeImage image, Copy copy, double scale) {
        Face face = copy.from();
        int[][] px = new int[face.h()][face.w()];
        for (int y = 0; y < face.h(); y++)
            for (int x = 0; x < face.w(); x++)
                px[copy.flipV() ? face.h() - 1 - y : y][copy.flipH() ? face.w() - 1 - x : x] =
                    image.getPixelRGBA((int) ((face.x() + x) * scale), (int) ((face.y() + y) * scale));
        return px;
    }

    /** {@code holes}: see-through pixels are copied too (the latch: a chest without one leaves it blank). */
    private static void write(NativeImage image, Face face, int[][] px, int scale, boolean holes) {
        for (int y = 0; y < face.h(); y++)
            for (int x = 0; x < face.w(); x++) {
                int abgr = px[y][x];
                if (abgr >>> 24 == 0 && !holes)
                    continue;  // a hole in the chest's art keeps the target's own pixel
                for (int dy = 0; dy < scale; dy++)
                    for (int dx = 0; dx < scale; dx++)
                        image.setPixelRGBA((face.x() + x) * scale + dx, (face.y() + y) * scale + dy, abgr);
            }
    }

    /** The columns to keep, down to {@code w}: least change dropped first, never the frame, only {@code allowed} ones. */
    private static List<Integer> keptColumns(int[][] px, int w, IntPredicate allowed) {
        List<Integer> cols = all(px[0].length);
        while (cols.size() > w && cols.size() > 2) {
            int drop = -1;
            long least = Long.MAX_VALUE;
            for (int pass = 0; pass < 2 && drop < 0; pass++)  // a second pass may drop any interior line
                for (int i = 1; i < cols.size() - 1; i++) {
                    if (pass == 0 && !allowed.test(cols.get(i)))
                        continue;
                    long cost = 0;
                    for (int[] row : px)
                        cost += diff(row[cols.get(i)], row[cols.get(i - 1)]) + diff(row[cols.get(i)], row[cols.get(i + 1)]);
                    if (cost < least) {
                        least = cost;
                        drop = i;
                    }
                }
            cols.remove(drop);
        }
        return cols.subList(0, Math.min(w, cols.size()));
    }

    /** The rows to keep, down to {@code h}, as {@link #keptColumns}. */
    private static List<Integer> keptRows(int[][] px, int h, IntPredicate allowed) {
        List<Integer> rows = all(px.length);
        while (rows.size() > h && rows.size() > 2) {
            int drop = -1;
            long least = Long.MAX_VALUE;
            for (int pass = 0; pass < 2 && drop < 0; pass++)
                for (int i = 1; i < rows.size() - 1; i++) {
                    if (pass == 0 && !allowed.test(rows.get(i)))
                        continue;
                    long cost = 0;
                    for (int x = 0; x < px[0].length; x++)
                        cost += diff(px[rows.get(i)][x], px[rows.get(i - 1)][x]) + diff(px[rows.get(i)][x], px[rows.get(i + 1)][x]);
                    if (cost < least) {
                        least = cost;
                        drop = i;
                    }
                }
            rows.remove(drop);
        }
        return rows.subList(0, Math.min(h, rows.size()));
    }

    private static List<Integer> all(int n) {
        List<Integer> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            out.add(i);
        return out;
    }

    private static int[][] pick(int[][] px, List<Integer> rows, List<Integer> cols) {
        int[][] out = new int[rows.size()][cols.size()];
        for (int y = 0; y < rows.size(); y++)
            for (int x = 0; x < cols.size(); x++)
                out[y][x] = px[rows.get(y)][cols.get(x)];
        return out;
    }

    /** The lid face above the body face: one picture, so a design across the seam counts as one. */
    private static int[][] stack(int[][] lid, int[][] body) {
        int[][] out = new int[lid.length + body.length][];
        System.arraycopy(lid, 0, out, 0, lid.length);
        System.arraycopy(body, 0, out, lid.length, body.length);
        return out;
    }

    /** How different two pixels are: each channel's difference, summed. */
    private static int diff(int a, int b) {
        int d = 0;
        for (int shift = 0; shift < 32; shift += 8)
            d += Math.abs((a >>> shift & 0xFF) - (b >>> shift & 0xFF));
        return d;
    }
}
