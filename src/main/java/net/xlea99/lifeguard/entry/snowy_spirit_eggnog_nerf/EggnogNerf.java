package net.xlea99.lifeguard.entry.snowy_spirit_eggnog_nerf;

import java.util.List;
import java.util.Optional;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Snowy Spirit's eggnog is a treat, not a staple ({@link SnowySpiritEggnogNerf}): it feeds 4 hunger and 14 saturation
 * (more than a golden carrot) and stacks to 64, from ginger, an egg and milk. Nerfed it's 2 hunger and 6 saturation,
 * drunk as fast as before; it still cures nausea (that's the item's own code, not its food).
 */
public final class EggnogNerf {
    private static final FoodProperties FOOD = new FoodProperties(2, 6.0F, false, 1.5F, Optional.empty(), List.of());

    private EggnogNerf() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((ModifyDefaultComponentsEvent event) ->
            event.modify(ModRegistry.EGGNOG.get(), components -> components.set(DataComponents.FOOD, FOOD)));
    }
}
