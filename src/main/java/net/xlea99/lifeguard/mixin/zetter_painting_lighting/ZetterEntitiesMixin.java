package net.xlea99.lifeguard.mixin.zetter_painting_lighting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.dantaeusb.zetter.core.ZetterEntities;
import net.minecraft.world.entity.EntityType;
import net.xlea99.lifeguard.entry.zetter_painting_lighting.ZetterPaintingLighting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The framed painting's type gets eye height 0, so its light probe is its own center. {@code lambda$static$0} is the
 * framed painting's supplier (javap 0.21.7). See {@link ZetterPaintingLighting}.
 */
@Mixin(ZetterEntities.class)
public abstract class ZetterEntitiesMixin {
    @WrapOperation(method = "lambda$static$0", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/EntityType$Builder;build(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;"))
    private static EntityType<?> lifeguard$eyeAtCenter(EntityType.Builder<?> builder, String id,
                                                       Operation<EntityType<?>> original) {
        return original.call(builder.eyeHeight(0.0F), id);
    }
}
