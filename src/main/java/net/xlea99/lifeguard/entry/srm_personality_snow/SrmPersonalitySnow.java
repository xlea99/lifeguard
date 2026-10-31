package net.xlea99.lifeguard.entry.srm_personality_snow;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class SrmPersonalitySnow {
    /**
     * Personality's falling snow adds {@code onPlace} and {@code tick} to vanilla's snow layer, and at its higher mixin
     * priority they silently replace Snow! Real Magic's own, whatever either config says: SRM's gravity and landing
     * rules are gone, and with Personality's option off, unsupported snow just floats. Its three snow mixins are
     * switched off unless its option is on and SRM's gravity is off (see {@link FallingSnowOwner}).
     */
    public static final Entry ENTRY = Entries.compat("srm_personality_snow", Taste.NONE)
        .requiresMods("snowrealmagic", "personality")
        .side(Side.SERVER).restart(Restart.GAME)
        .cancelsMixins(FallingSnowOwner::personalityYields,
            "com.teamabnormals.personality.core.mixin.SnowLayerBlockMixin",
            "com.teamabnormals.personality.core.mixin.FallingBlockMixin",
            "com.teamabnormals.personality.core.mixin.BlueprintFallingBlockEntityMixin");

    private SrmPersonalitySnow() {
    }
}
