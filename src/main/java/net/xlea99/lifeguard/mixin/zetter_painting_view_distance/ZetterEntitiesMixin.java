package net.xlea99.lifeguard.mixin.zetter_painting_view_distance;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.dantaeusb.zetter.core.ZetterEntities;
import net.minecraft.world.entity.EntityType;
import net.xlea99.lifeguard.entry.zetter_painting_view_distance.ZetterPaintingViewDistance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The framed painting's type is built like vanilla's painting. {@code lambda$static$0} is the framed painting's supplier
 * (javap 0.21.7: {@code sized(1, 1)}; {@code lambda$static$1} is the easel's {@code sized(0.8, 1.8)}). See
 * {@link ZetterPaintingViewDistance}.
 */
@Mixin(ZetterEntities.class)
public abstract class ZetterEntitiesMixin {
    @WrapOperation(method = "lambda$static$0", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/EntityType$Builder;build(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;"))
    private static EntityType<?> lifeguard$likeVanillaPaintings(EntityType.Builder<?> builder, String id,
                                                                Operation<EntityType<?>> original) {
        return original.call(builder.clientTrackingRange(10).updateInterval(Integer.MAX_VALUE), id);
    }
}
