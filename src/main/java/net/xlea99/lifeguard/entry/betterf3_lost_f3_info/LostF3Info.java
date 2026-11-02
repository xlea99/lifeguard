package net.xlea99.lifeguard.entry.betterf3_lost_f3_info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The F3 lines other mods add through NeoForge's DebugText event, for BetterF3 to show. Each frame, before BetterF3
 * draws, the overlay mixin fires the event on fresh copies of vanilla's two info lists and keeps only what listeners
 * added; while BetterF3 then builds its text, those lines are added to the lists it reads (its "Misc" modules show
 * every line they don't recognize). Client render thread only.
 */
public final class LostF3Info {
    private static List<String> left = List.of();
    private static List<String> right = List.of();
    private static boolean feeding;

    private LostF3Info() {
    }

    /** The event has run on these lists: keep what it added, and feed it to BetterF3 until the frame ends. */
    public static void collected(List<String> leftBefore, List<String> leftAfter,
                                 List<String> rightBefore, List<String> rightAfter) {
        left = added(leftBefore, leftAfter);
        right = added(rightBefore, rightAfter);
        feeding = true;
    }

    /** The F3 frame is over: vanilla's lists are vanilla's again. */
    public static void frameEnded() {
        feeding = false;
    }

    /**
     * Left lines go last. Right lines go first: BetterF3's right "Misc" module drops everything after a "Targeted Block"
     * header up to the next blank line, and vanilla's right list ends with those sections.
     */
    public static void feedLeft(List<String> lines) {
        if (feeding)
            lines.addAll(left);
    }

    public static void feedRight(List<String> lines) {
        if (feeding)
            lines.addAll(0, right);
    }

    /** What after has beyond before, in after's order (lines may repeat, and listeners may insert anywhere). */
    private static List<String> added(List<String> before, List<String> after) {
        Map<String, Integer> unmatched = new HashMap<>();
        for (String line : before)
            unmatched.merge(line, 1, Integer::sum);
        List<String> extra = new ArrayList<>();
        for (String line : after) {
            Integer n = unmatched.get(line);
            if (n != null && n > 0)
                unmatched.put(line, n - 1);
            else
                extra.add(line);
        }
        return extra;
    }
}
