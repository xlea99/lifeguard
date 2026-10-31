package net.xlea99.lifeguard.mixin.species_soap_washes_stackaticks;

import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Soap's washing sound and bubbles, and using the soap up, which Supplementaries keeps private (for StackatickSoap). */
@Mixin(SoapItem.class)
public interface SoapItemAccessor {
    @Invoker("playEffectsAndConsume")
    static void lifeguard$playEffectsAndConsume(ItemStack stack, Player player, Entity entity) {
        throw new AssertionError();
    }
}
