package net.xlea99.lifeguard.mixin.etcetera_easy_anvils_label_costs;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The anvil's input slots, to see what Etcetera's label code is pricing. */
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
    @Accessor("inputSlots")
    Container lifeguard$inputSlots();
}
