package net.xlea99.lifeguard.mixin.etcetera_shimmering_bismuth_trims;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims.ShimmerTrimSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Worn bismuth trims shimmer again on Etcetera 2.2.8+ ({@link ShimmerTrimSource}). Etcetera now gives every trim pattern
 * a still bismuth version in code, appending its source to the armor trims atlas after every pack's sources (its
 * {@code SpriteSourceListMixin}, at the return of {@code SpriteSourceList.load}); same sprite names, last one wins, so it
 * covered our shimmering ones. Through MixinSquared, right after Etcetera adds its source, ours goes on after it, so the
 * shimmering sprites win. Applied after Etcetera's mixin, whose handler this is.
 */
@Mixin(value = SpriteSourceList.class, priority = 1500)
public abstract class SpriteSourceListMixin {
    @Unique
    private static final ResourceLocation lifeguard$ARMOR_TRIMS = ResourceLocation.withDefaultNamespace("armor_trims");

    @TargetHandler(mixin = "com.ninni.etcetera.mixin.SpriteSourceListMixin", name = "etcetera$addBismuthToEveryPattern")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private static boolean lifeguard$shimmerAfterEtcetera(List<Object> sources, Object etceteraSource, Operation<Boolean> original) {
        boolean added = original.call(sources, etceteraSource);
        sources.add(new ShimmerTrimSource(lifeguard$ARMOR_TRIMS));
        return added;
    }
}
