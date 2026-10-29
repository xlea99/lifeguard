package net.xlea99.lifeguard.entry.cc_head_vision;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

/**
 * Wearing Caverns & Chasms' heads colors your view, like a creeper head: all assets, riding Supplementaries' mob head
 * effects ({@code mob_head_effects/*.json} -> a post shader; Supps' client option "mob_head_shaders" must be on). The
 * pack holds {@code assets/lifeguard/mob_head_effects} and the shaders they name.
 */
public final class CcHeadVision {
    /** Only what you see. */
    public static final Entry ENTRY = Entries.feature("cc_head_vision", Taste.STRONG)
        .requiresMods("caverns_and_chasms", "supplementaries")
        .side(Side.CLIENT).restart(Restart.GAME)
        .resourcePack();

    private CcHeadVision() {
    }
}
