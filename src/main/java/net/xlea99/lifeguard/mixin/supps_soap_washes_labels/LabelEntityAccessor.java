package net.xlea99.lifeguard.mixin.supps_soap_washes_labels;

import net.mehvahdjukaar.labels.LabelEntity;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LabelEntity.class)
public interface LabelEntityAccessor {

    @Invoker("setColor")
    void lifeguard$setColor(@Nullable DyeColor color);
}
