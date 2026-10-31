package net.xlea99.lifeguard.mixin.species_soap_washes_stackaticks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** A Stackatick's dye, by class name (Species isn't compiled against; for StackatickSoap). */
@Mixin(targets = "com.ninni.species.server.entity.mob.update_1.Stackatick")
public interface StackatickAccessor {
    @Invoker("isDyed")
    boolean lifeguard$isDyed();

    @Invoker("setDyed")
    void lifeguard$setDyed(boolean dyed);
}
