package net.xlea99.lifeguard.entry.faucet_pouring_recipes;

import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Restart;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

public final class FaucetPouringRecipes {
    /** Faucets pour on the server only; the recipes are server data, sent to clients for viewing. */
    public static final Entry ENTRY = Entries.tweak("faucet_pouring_recipes", Taste.LIGHT)
        .requiresMods("supplementaries")
        .side(Side.SERVER).restart(Restart.GAME);

    /** The recipes, and Supplementaries' concrete powder hardening, as an EMI category. */
    public static final Entry EMI = Entries.compat("emi", Taste.NONE).parent(ENTRY)
        .requiresMods("emi")
        .side(Side.CLIENT).restart(Restart.GAME);

    private FaucetPouringRecipes() {
    }
}
