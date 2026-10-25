package net.xlea99.lifeguard.entry.whiter_labels;

/** OKLab to and from NativeImage's ABGR (alpha comes back opaque). */
final class OkLab {
    private OkLab() {
    }

    static double[] fromAbgr(int abgr) {
        double r = linear(abgr & 0xFF), g = linear(abgr >> 8 & 0xFF), b = linear(abgr >> 16 & 0xFF);
        double l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
        double m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
        double s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
        return new double[] {0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
            1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s, 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s};
    }

    static int toAbgr(double[] lab) {
        double l = Math.pow(lab[0] + 0.3963377774 * lab[1] + 0.2158037573 * lab[2], 3);
        double m = Math.pow(lab[0] - 0.1055613458 * lab[1] - 0.0638541728 * lab[2], 3);
        double s = Math.pow(lab[0] - 0.0894841775 * lab[1] - 1.2914855480 * lab[2], 3);
        int r = srgb(4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s);
        int g = srgb(-1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s);
        int b = srgb(-0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s);
        return 0xFF000000 | b << 16 | g << 8 | r;
    }

    private static double linear(int c) {
        double v = c / 255.0;
        return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static int srgb(double v) {
        v = Math.max(0, Math.min(1, v));
        v = v <= 0.0031308 ? v * 12.92 : 1.055 * Math.pow(v, 1 / 2.4) - 0.055;
        return (int) Math.round(v * 255);
    }
}
