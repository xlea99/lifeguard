package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import com.teamabnormals.clayworks.common.DecoratedPotTrimPattern;
import com.teamabnormals.clayworks.core.registry.ClayworksDataComponents;
import com.teamabnormals.clayworks.core.registry.ClayworksRegistries;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Flower pots dyed and etched at Clayworks' pottery table. A flower pot item carries its color as vanilla's
 * {@code base_color} and its design as Clayworks' own {@code pot_trim}; a placed one keeps both in a block entity every
 * flower pot gets (vanilla's, every potted plant, every mod's).
 */
public final class FlowerPots {
    /** The designs drawn for flower pots: a flower pot lists only these at the table, a decorated pot never does. */
    public static final TagKey<DecoratedPotTrimPattern> DESIGNS = TagKey.create(ClayworksRegistries.DECORATED_POT_TRIM_PATTERN,
        ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "flower_pot"));

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Lifeguard.MODID);

    // Valid for any flower pot, decided by instanceof as a block is set rather than from a list built now: potted plants
    // from mods registered later work without being named. The set still holds vanilla's pot for anything reading it.
    public static final Supplier<BlockEntityType<FlowerPotBlockEntity>> BLOCK_ENTITY = BLOCK_ENTITIES.register("flower_pot",
        () -> new BlockEntityType<>(FlowerPotBlockEntity::new, Set.of(Blocks.FLOWER_POT), null) {
            @Override
            public boolean isValid(BlockState state) {
                return state.getBlock() instanceof FlowerPotBlock;
            }
        });

    private FlowerPots() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    public static boolean isFlowerPot(ItemStack stack) {
        return stack.is(Items.FLOWER_POT);
    }

    @Nullable
    public static DyeColor colorOf(ItemStack stack) {
        return stack.get(DataComponents.BASE_COLOR);
    }

    @Nullable
    public static DecoratedPotTrim designOf(ItemStack stack) {
        return stack.get(ClayworksDataComponents.POT_TRIM.get());
    }
}
