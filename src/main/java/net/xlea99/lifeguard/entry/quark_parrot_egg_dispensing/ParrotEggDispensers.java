package net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.entity.ParrotEgg;
import org.violetmoon.quark.content.tools.module.ParrotEggsModule;

/**
 * Dispensers throw Quark's parrot eggs again, as in Quark 4.0: a parrot egg in a dispenser flies out like a chicken egg
 * and can hatch a parrot of its color. Quark 4.1 dropped the dispenser behavior in the port (its eggs aren't vanilla
 * projectile items, so vanilla has none for them); the egg entity and the throw by hand are still Quark's. Only loaded
 * when the entry is active, so only with Quark installed.
 */
public final class ParrotEggDispensers {
    private ParrotEggDispensers() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(ParrotEggDispensers::registerBehaviors));
    }

    private static void registerBehaviors() {
        if (!Quark.ZETA.modules.isEnabled(ParrotEggsModule.class))
            return;
        // Quark makes one egg per parrot variant, in the variants' order.
        List<Item> eggs = ParrotEggsModule.parrotEggs;
        Parrot.Variant[] variants = Parrot.Variant.values();
        for (int i = 0; i < eggs.size() && i < variants.length; i++)
            DispenserBlock.registerBehavior(eggs.get(i), new Throw(variants[i]));
    }

    /** Vanilla's projectile dispensing (position, power, spread, sound), throwing Quark's egg of one variant. */
    private static final class Throw extends DefaultDispenseItemBehavior {
        private static final ProjectileItem.DispenseConfig CONFIG = ProjectileItem.DispenseConfig.DEFAULT;
        private final Parrot.Variant variant;

        Throw(Parrot.Variant variant) {
            this.variant = variant;
        }

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Level level = source.level();
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            Position position = CONFIG.positionFunction().getDispensePosition(source, direction);
            ParrotEgg egg = new ParrotEgg(level, position.x(), position.y(), position.z());
            egg.setItem(stack);
            egg.setVariant(variant);
            egg.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), CONFIG.power(), CONFIG.uncertainty());
            level.addFreshEntity(egg);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource source) {
            source.level().levelEvent(CONFIG.overrideDispenseEvent().orElse(1002), source.pos(), 0);
        }
    }
}
