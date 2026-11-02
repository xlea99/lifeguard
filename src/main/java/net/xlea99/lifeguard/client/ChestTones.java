package net.xlea99.lifeguard.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Recoloring a drawing into a chest's colors, the way chest boat icons and pack animal saddlebags are: a chest's colors
 * as {@link Tone}s (OKLab, with how many pixels have each), and a {@link Transfer} that moves a pixel's lightness from
 * one set of tones to another (mean and spread, so every shading step stays) and gives it the color of the target's
 * tones near that lightness.
 */
public final class ChestTones {
    /** How far (OKLab lightness) a tone still lends its color to a pixel of another lightness. */
    public static final double SIGMA = 0.05;

    private ChestTones() {
    }

    /** A color of a chest, how many pixels have it, and its OKLab. */
    public record Tone(int abgr, int count, double[] lab) {
        public double chroma() {
            return Math.hypot(lab[1], lab[2]);
        }
    }

    public static List<Tone> tones(Map<Integer, Integer> colors) {
        List<Tone> tones = new ArrayList<>();
        colors.forEach((abgr, count) -> tones.add(new Tone(abgr, count, OkLab.fromAbgr(abgr))));
        return tones;
    }

    public static double mean(List<Tone> tones) {
        double sum = 0, n = 0;
        for (Tone t : tones) {
            sum += t.lab()[0] * t.count();
            n += t.count();
        }
        return sum / n;
    }

    public static double spread(List<Tone> tones) {
        double mean = mean(tones), sum = 0, n = 0;
        for (Tone t : tones) {
            sum += (t.lab()[0] - mean) * (t.lab()[0] - mean) * t.count();
            n += t.count();
        }
        return Math.max(1e-4, Math.sqrt(sum / n));
    }

    /** The color at lightness {@code l} from {@code palette}: its colors near that lightness, weighted by count and closeness. */
    public static int at(double l, List<Tone> palette) {
        double a = 0, b = 0, w = 0;
        for (Tone t : palette) {
            double d = t.lab()[0] - l, weight = t.count() * Math.exp(-d * d / (2 * SIGMA * SIGMA)) + 1e-12;
            a += t.lab()[1] * weight;
            b += t.lab()[2] * weight;
            w += weight;
        }
        return OkLab.toAbgr(new double[] {l, a / w, b / w});
    }

    /**
     * A chest's wood without its accents (gray trim, gold studs, iron bands): colors under half the palette's weighted
     * median chroma, or over 45 degrees off its weighted mean hue (unless the wood itself is near gray).
     */
    public static List<Tone> withoutAccents(List<Tone> all) {
        List<Tone> byChroma = new ArrayList<>(all);
        byChroma.sort(Comparator.comparingDouble(Tone::chroma));
        int total = all.stream().mapToInt(Tone::count).sum(), seen = 0;
        double median = 0, a = 0, b = 0;
        for (Tone t : byChroma) {
            seen += t.count();
            if (seen * 2 >= total) {
                median = t.chroma();
                break;
            }
        }
        for (Tone t : all) {
            a += t.lab()[1] * t.count();
            b += t.lab()[2] * t.count();
        }
        double hue = Math.toDegrees(Math.atan2(b, a));
        List<Tone> kept = new ArrayList<>();
        for (Tone t : all) {
            double off = Math.abs(((Math.toDegrees(Math.atan2(t.lab()[2], t.lab()[1])) - hue + 540) % 360) - 180);
            if (t.chroma() >= 0.5 * median && (off <= 45 || median < 0.03))
                kept.add(t);
        }
        return kept.isEmpty() ? all : kept;
    }

    /**
     * How a chest recolors a pixel: its lightness moved from one set of tones to the chest's (mean and spread, so every
     * shading step stays), its color the chest's tones near that lightness ({@link #at}).
     */
    public record Transfer(double fromMean, double fromSpread, double toMean, double toSpread, List<Tone> palette) {
        public static Transfer of(List<Tone> from, List<Tone> to) {
            return new Transfer(mean(from), spread(from), mean(to), spread(to), to);
        }

        public double lightness(double l) {
            return (l - fromMean) * toSpread / fromSpread + toMean;
        }

        public int apply(int abgr) {
            return at(lightness(OkLab.fromAbgr(abgr)[0]), palette);
        }
    }
}
