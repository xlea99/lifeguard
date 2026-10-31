package net.xlea99.lifeguard.mixin.cc_crafted_tether_potions;

import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Vanilla's brewing mix record is package-private; its ends, for {@link PotionBrewingBuilderMixin}. */
@Mixin(targets = "net.minecraft.world.item.alchemy.PotionBrewing$Mix")
public interface PotionBrewingMixAccessor {
    @Accessor("from")
    Holder<?> lifeguard$from();

    @Accessor("to")
    Holder<?> lifeguard$to();
}
