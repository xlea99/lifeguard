package net.xlea99.lifeguard.entry.chest_boats_take_any_chest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.data.RecipeEdits;

/**
 * The rule (a {@link RecipeEdits.Rule}): a standard chest boat recipe that asks for the vanilla chest takes any wooden,
 * untrapped chest instead, the ingredient NeoForge gives vanilla's own chest boats. Standard means exactly: shapeless,
 * two ingredients, {@code {"item": "minecraft:chest"}} and one {@code <namespace>:<wood>_boat} (or {@code _raft}),
 * making that wood's {@code _chest_boat} ({@code _chest_raft}). Item tags aren't bound yet while recipes load, so the
 * recipe is recognized by its shape; one a datapack changed isn't in it any more and is left alone.
 */
public final class StandardChestBoatRecipes {
    private static final JsonObject VANILLA_CHEST = JsonParser.parseString("{\"item\":\"minecraft:chest\"}").getAsJsonObject();
    private static final String ANY_WOODEN_CHEST =
        "{\"type\":\"neoforge:difference\",\"base\":{\"tag\":\"c:chests/wooden\"},\"subtracted\":{\"tag\":\"c:chests/trapped\"}}";

    private StandardChestBoatRecipes() {
    }

    public static JsonElement widen(ResourceLocation id, JsonElement recipe) {
        if (!recipe.isJsonObject())
            return null;
        JsonObject r = recipe.getAsJsonObject();
        if (!"minecraft:crafting_shapeless".equals(string(r, "type")) || !r.has("ingredients") || !r.get("ingredients").isJsonArray()
            || !r.has("result") || !r.get("result").isJsonObject())
            return null;
        JsonArray ingredients = r.getAsJsonArray("ingredients");
        if (ingredients.size() != 2)
            return null;
        int chest = ingredients.get(0).equals(VANILLA_CHEST) ? 0 : ingredients.get(1).equals(VANILLA_CHEST) ? 1 : -1;
        if (chest < 0 || !ingredients.get(1 - chest).isJsonObject())
            return null;
        String boat = string(ingredients.get(1 - chest).getAsJsonObject(), "item");
        String result = string(r.getAsJsonObject("result"), "id");
        if (boat == null || result == null || !result.equals(chestBoatOf(boat)))
            return null;
        JsonObject widened = r.deepCopy();
        widened.getAsJsonArray("ingredients").set(chest, JsonParser.parseString(ANY_WOODEN_CHEST));
        return widened;
    }

    /** {@code ns:pine_boat} -> {@code ns:pine_chest_boat}, {@code ns:date_raft} -> {@code ns:date_chest_raft}; else null. */
    private static String chestBoatOf(String boat) {
        for (String kind : new String[] {"_boat", "_raft"})
            if (boat.endsWith(kind) && !boat.endsWith("_chest" + kind))
                return boat.substring(0, boat.length() - kind.length()) + "_chest" + kind;
        return null;
    }

    private static String string(JsonObject o, String key) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
    }
}
