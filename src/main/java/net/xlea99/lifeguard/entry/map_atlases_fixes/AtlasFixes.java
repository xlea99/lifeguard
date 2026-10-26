package net.xlea99.lifeguard.entry.map_atlases_fixes;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.config.MapAtlasesConfig;
import pepjebs.mapatlases.item.MapAtlasItem;
import pepjebs.mapatlases.map_collection.MapCollection;
import pepjebs.mapatlases.utils.AtlasMap;
import pepjebs.mapatlases.utils.ICraftingInputWithContext;
import pepjebs.mapatlases.utils.MapType;

/**
 * Map Atlases 7.0.1's leftover item bugs ({@link MapAtlasesFixes}, {@code mixin.map_atlases_fixes}).
 *
 * <ul>
 *   <li>The map limit ({@code max_map_count}) only stopped empty maps: a filled map or a second atlas in a cartography
 *   table could take an atlas past it. The limit is also what keeps opening an atlas to that many map packets.</li>
 *   <li>Merging two atlases pools their empty maps and gives each half, rounding down: an odd count lost one. It now
 *   comes back as an item.</li>
 *   <li>Shears in a cartography table cut the map out of the whole stack of atlases in the slot, for one map out.</li>
 *   <li>Atlas + shears in a crafting grid picks the map nearest the player twice: once for the result shown, once to
 *   take it out of the atlas. Moving onto another map in between handed out one and removed the other.</li>
 *   <li>Selecting a slice took any dimension, and saved it on the atlas: a cheating client could grow an atlas (or one
 *   on a lectern in reach) without end.</li>
 * </ul>
 */
public final class AtlasFixes {
    /** The map the crafting result shows, per crafting menu, so taking it removes that one. */
    private static final Map<AbstractContainerMenu, ChosenMap> SHOWN_CUTS = Collections.synchronizedMap(new WeakHashMap<>());

    private AtlasFixes() {
    }

    /** No room left for one more map. */
    public static boolean isFull(ItemStack atlas, Level level) {
        return MapAtlasItem.getFreeMapSlots(atlas, level) < 1;
    }

    /** Holds more maps (filled and empty) than the limit allows. */
    public static boolean isOverLimit(ItemStack atlas, Level level) {
        return MapAtlasItem.getMaps(atlas, level).getCount() + MapAtlasItem.getEmptyMaps(atlas).getTotalCount()
            > MapAtlasesConfig.maxMapCount.get();
    }

    /** The empty maps a merge of these two atlases leaves over: one of each type whose pooled count is odd. */
    public static Map<MapType, Integer> mergeLeftovers(ItemStack atlas, ItemStack other) {
        Map<MapType, Integer> pooled = new EnumMap<>(MapType.class);
        MapAtlasItem.getEmptyMaps(atlas).getAll().forEach((type, count) -> pooled.merge(type, count, Integer::sum));
        MapAtlasItem.getEmptyMaps(other).getAll().forEach((type, count) -> pooled.merge(type, count, Integer::sum));
        pooled.replaceAll((type, count) -> count % 2);
        pooled.values().removeIf(count -> count == 0);
        return pooled;
    }

    public static void rememberShownCut(CraftingInput input, @Nullable AtlasMap shown) {
        AbstractContainerMenu menu = menuOf(input);
        if (menu == null)
            return;
        if (shown == null)
            SHOWN_CUTS.remove(menu);
        else
            SHOWN_CUTS.put(menu, new ChosenMap(shown.id, shown.type));
    }

    /** The map the result showed, if it's still in the atlas. */
    @Nullable
    public static AtlasMap shownCut(CraftingInput input, MapCollection maps) {
        AbstractContainerMenu menu = menuOf(input);
        ChosenMap chosen = menu == null ? null : SHOWN_CUTS.get(menu);
        if (chosen == null)
            return null;
        for (AtlasMap map : maps.getAllFound())
            if (map.id.equals(chosen.id()) && map.type == chosen.type())
                return map;
        return null;
    }

    @Nullable
    private static AbstractContainerMenu menuOf(CraftingInput input) {
        return input instanceof ICraftingInputWithContext context ? context.mapAtlases$getMenu() : null;
    }

    private record ChosenMap(MapId id, MapType type) {
    }
}
