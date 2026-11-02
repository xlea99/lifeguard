package net.xlea99.lifeguard.entry.better_tridents_loyal_drops_last;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class BetterTridentsLoyalDropsLast {
    /**
     * None: Better Tridents' loyalty drops (a loyalty kill sends its drops flying to the player) take the drops the
     * death event holds and CANCEL it, at NeoForge's normal priority. Every listener after it never runs on a loyalty
     * kill, so what it would add is lost (Name Tag Upgrade's name tag, Alex's Mobs' vine lasso, Quark's Monster Box
     * loot). Which listeners come after is registration order, set while mods are constructed in parallel: a race, read
     * in the code 2026-11-01 (both register in their mod constructors, Puzzles Lib adds the listener at once), not yet
     * seen to lose anything in game (user: build it anyway). Now its drops listener runs last (LOWEST), after every
     * other mod has added or removed drops. Its XP magnet is unaffected (vanilla hands out XP before the drops event).
     * Load-gated: Better Tridents registers while it's constructed, before any runtime switch could be read.
     * (Better Tridents 21.1.0, BetterTridents.registerEventHandlers.)
     */
    public static final Entry ENTRY = Entries.fix("better_tridents_loyal_drops_last", Taste.NONE).home("bettertridents")
        .requiresMods("bettertridents")
        .side(Side.SERVER).restart(Restart.GAME);

    private BetterTridentsLoyalDropsLast() {
    }
}
