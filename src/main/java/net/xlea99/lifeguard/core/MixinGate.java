package net.xlea99.lifeguard.core;

/**
 * Which of Lifeguard's mixins get applied. Every mixin lives in its entry's package, named after the entry's id:
 * {@code net.xlea99.lifeguard.mixin.jukebox.speakers.SpeakerBlockMixin} belongs to {@code jukebox.speakers}. The
 * package is the whole link, so nothing has to be declared twice and a mixin can't end up ungated by accident.
 *
 * <p>Kept free of the mixin library itself so the rule can be tested without a game; the mixin plugin only asks it.
 */
public final class MixinGate {
    public static final String MIXIN_ROOT = "net.xlea99.lifeguard.mixin.";
    /** Mixins in {@code mixin.core} are infrastructure, always applied. */
    public static final String CORE_PACKAGE = "core";

    private MixinGate() {
    }

    /**
     * Whether a mixin should be applied right now, at startup.
     *
     * <ul>
     *     <li>A client entry's mixins never apply on a dedicated server.</li>
     *     <li>A load-gated entry's apply when it's active: off means never loaded.</li>
     *     <li>A runtime-gated entry's apply whenever it's installed, and check {@link Entry#active()} as they run.</li>
     * </ul>
     *
     * @throws IllegalStateException for a mixin outside any entry's package: every mixin belongs to an entry
     */
    public static boolean shouldApply(String mixinClassName, Registry registry, boolean dedicatedServer) {
        String entryPackage = entryPackage(mixinClassName);
        if (entryPackage.equals(CORE_PACKAGE) || entryPackage.startsWith(CORE_PACKAGE + "."))
            return true;
        Entry entry = registry.get(entryPackage).orElseThrow(() -> new IllegalStateException(
            mixinClassName + " is in package '" + entryPackage + "', which isn't an entry. Every mixin lives directly"
                + " in its entry's package (named after the entry's id), or in mixin.core for always-on infrastructure."));
        if (dedicatedServer && entry.side() == Side.CLIENT)
            return false;
        return switch (entry.gating()) {
            case LOAD -> entry.active();
            case RUNTIME -> entry.installed();
        };
    }

    /** {@code net.xlea99.lifeguard.mixin.jukebox.speakers.SpeakerBlockMixin} to {@code jukebox.speakers}. */
    static String entryPackage(String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_ROOT))
            throw new IllegalStateException(mixinClassName + " isn't under " + MIXIN_ROOT);
        String rest = mixinClassName.substring(MIXIN_ROOT.length());
        int lastDot = rest.lastIndexOf('.');
        if (lastDot < 0)
            throw new IllegalStateException(mixinClassName + " sits directly in " + MIXIN_ROOT
                + " instead of in its entry's package");
        return rest.substring(0, lastDot);
    }
}
