package net.xlea99.lifeguard.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.core.Entries;
import net.xlea99.lifeguard.core.Entry;

/**
 * Entries' edits to other mods' recipes ({@link Entry#editsRecipe}), made to the recipe files as the game loads them:
 * the merged set, every mod's and every datapack's, topmost file per id. An edit replaces a recipe only while it is
 * still exactly the original file. One a datapack (a world's, a paxi pack's) or a mod update changed is left alone, and
 * one removed stays removed: datapacks keep the last word, and an entry that's off touches nothing.
 */
public final class RecipeEdits {
    private static List<Edit> edits;

    private RecipeEdits() {
    }

    /** The recipe files about to load, with every active entry's edits made where the original is still there. */
    public static Map<ResourceLocation, JsonElement> apply(Map<ResourceLocation, JsonElement> recipes) {
        Map<ResourceLocation, JsonElement> edited = null;
        for (Edit edit : edits()) {
            if (!edit.entry().active())
                continue;
            JsonElement loaded = recipes.get(edit.recipe());
            if (loaded == null)
                continue;
            if (!loaded.equals(edit.original())) {
                Lifeguard.LOGGER.info("Lifeguard: {} left {} alone: it isn't the original any more (a datapack or a mod update"
                    + " changed it)", edit.entry().id(), edit.recipe());
                continue;
            }
            if (edited == null)
                edited = new HashMap<>(recipes);
            edited.put(edit.recipe(), edit.replacement().deepCopy());
            Lifeguard.LOGGER.debug("Lifeguard: {} edited {}", edit.entry().id(), edit.recipe());
        }
        return edited == null ? recipes : edited;
    }

    private static synchronized List<Edit> edits() {
        if (edits == null) {
            List<Edit> loaded = new ArrayList<>();
            for (Entry entry : Entries.REGISTRY.entries())
                for (String recipeId : entry.recipeEdits())
                    loaded.add(read(entry, recipeId));
            edits = List.copyOf(loaded);
        }
        return edits;
    }

    private static Edit read(Entry entry, String recipeId) {
        String path = "/" + entry.recipeEditPath(recipeId);
        try (InputStream in = RecipeEdits.class.getResourceAsStream(path)) {
            if (in == null)
                throw new IllegalStateException("Lifeguard's " + path + " is missing from the jar");
            JsonObject file = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            return new Edit(entry, ResourceLocation.parse(recipeId), file.get("original"), file.get("replacement"));
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Lifeguard couldn't read the recipe edit " + path, e);
        }
    }

    private record Edit(Entry entry, ResourceLocation recipe, JsonElement original, JsonElement replacement) {
    }
}
