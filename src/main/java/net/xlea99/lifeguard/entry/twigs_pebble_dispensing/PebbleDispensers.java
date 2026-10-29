package net.xlea99.lifeguard.entry.twigs_pebble_dispensing;

import com.ninni.twigs.entity.Pebble;
import com.ninni.twigs.registry.TwigsItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Dispensers throw Twigs' pebbles again. Twigs 1.20.1 registered this behavior; 3.1.2's handler that did is empty, so a
 * dispenser just drops the pebble as an item.
 */
public final class PebbleDispensers {
    private PebbleDispensers() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(
            () -> DispenserBlock.registerBehavior(TwigsItems.PEBBLE.get(), new Throw())));
    }

    /** Vanilla's projectile dispensing (position, power, spread, sound), as Twigs 1.20.1 threw them. */
    private static final class Throw extends DefaultDispenseItemBehavior {
        private static final ProjectileItem.DispenseConfig CONFIG = ProjectileItem.DispenseConfig.DEFAULT;

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Level level = source.level();
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            Position position = CONFIG.positionFunction().getDispensePosition(source, direction);
            Pebble pebble = new Pebble(level, position.x(), position.y(), position.z());
            pebble.setItem(stack);
            pebble.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), CONFIG.power(), CONFIG.uncertainty());
            level.addFreshEntity(pebble);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource source) {
            source.level().levelEvent(CONFIG.overrideDispenseEvent().orElse(1002), source.pos(), 0);
        }
    }
}
