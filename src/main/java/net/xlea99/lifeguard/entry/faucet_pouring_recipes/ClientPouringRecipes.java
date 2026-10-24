package net.xlea99.lifeguard.entry.faucet_pouring_recipes;

import java.util.List;
import net.neoforged.fml.ModList;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.emi.FaucetPouringEmiPlugin;

/**
 * The faucet pouring recipes the server sent, as JSON, kept for recipe viewers. A viewer reloads when they arrive: they
 * can come in after it built its recipe list on joining.
 */
public final class ClientPouringRecipes {
    private static volatile List<String> json = List.of();

    private ClientPouringRecipes() {
    }

    public static List<String> json() {
        return json;
    }

    static void receive(List<String> received) {
        json = List.copyOf(received);
        if (ModList.get().isLoaded("emi") && FaucetPouringRecipes.EMI.active())
            FaucetPouringEmiPlugin.reload();
    }
}
