package net.xlea99.lifeguard.entry.snowy_spirit_edible_treats;

import java.util.Map;
import net.mehvahdjukaar.snowyspirit.common.block.GumdropButton;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * Snowy Spirit's sweets are food ({@link SnowySpiritEdibleTreats}), as Alex's Caves makes every candy block, however
 * absurd: gumdrops (every color, Dye the World's too: they're all Snowy Spirit's gumdrop block), gingerbread (plain and
 * frosted, and its stairs, slab, door and trapdoor) and the candy cane block. Right-clicking a block still places one;
 * right-clicking the air eats it (a block item with food eats when it isn't placed). Gumdrops crumble in their own
 * color ({@link GumdropCrumbs}).
 *
 * <p>Fed as Alex's Caves feeds its own: a whole gingerbread block 4 hunger, a stair, slab, door or trapdoor (Alex's Caves
 * has none) 2, the candy cane block 2, a gumdrop 2 and eaten quickly; all with little saturation and, with Alex's Caves,
 * its candies' 1% chance of Sugar Rush.
 */
public final class Treats {
    private static final String SNOWY_SPIRIT = "snowyspirit";
    /** Hunger by Snowy Spirit's id, as Alex's Caves' parallel blocks feed. */
    private static final Map<String, Integer> HUNGER = Map.of("gingerbread", 4, "gingerbread_frosted", 4, "gingerbread_stairs", 2,
        "gingerbread_slab", 2, "gingerbread_door", 2, "gingerbread_trapdoor", 2, "candy_cane_block", 2);
    private static final ResourceKey<MobEffect> SUGAR_RUSH =
        ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath("alexscaves", "sugar_rush"));

    private Treats() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(Treats::addFood);
    }

    /** True if {@code item} places one of Snowy Spirit's gumdrops (any color, any mod's). */
    public static boolean isGumdrop(Item item) {
        return item instanceof BlockItem block && block.getBlock() instanceof GumdropButton;
    }

    private static void addFood(ModifyDefaultComponentsEvent event) {
        FoodProperties whole = food(4, false);
        FoodProperties half = food(2, false);
        FoodProperties gumdrop = food(2, true);
        int[] treats = {0};
        event.getAllItems().forEach(item -> {
            FoodProperties food = isGumdrop(item) ? gumdrop : byId(item, whole, half);
            if (food != null) {
                event.modify(item, components -> components.set(DataComponents.FOOD, food));
                treats[0]++;
            }
        });
        Lifeguard.LOGGER.info("[lifeguard] snowy_spirit_edible_treats: {} Snowy Spirit treats are food", treats[0]);
    }

    @Nullable
    private static FoodProperties byId(Item item, FoodProperties whole, FoodProperties half) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        Integer hunger = SNOWY_SPIRIT.equals(id.getNamespace()) ? HUNGER.get(id.getPath()) : null;
        if (hunger == null)
            return null;
        return hunger == 4 ? whole : half;
    }

    private static FoodProperties food(int hunger, boolean fast) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationModifier(0.1F);
        if (fast)
            builder.fast();
        if (ModList.get().isLoaded("alexscaves"))
            builder.effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(SUGAR_RUSH), 200), 0.01F);
        return builder.build();
    }
}
