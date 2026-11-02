package net.xlea99.lifeguard.mixin.quark_armor_swap_respects_locks;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// An armor stand's DisabledSlots lock, for Quark's armor swap to obey it.
@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {
    @Accessor("disabledSlots")
    int lifeguard$disabledSlots();
}
