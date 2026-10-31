package net.xlea99.lifeguard.mixin.vanilla_backport_lightning_rods;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lightning finds every lightning rod again next to Vanilla Backport. VB's weathering rods sit in their own point of
 * interest ({@code minecraft:lightning_rods}: its seven weathered and waxed rods, not the plain one, which vanilla's
 * {@code lightning_rod} holds), and VB's ServerLevelMixin took over {@code findLightningRod} at its head, searched only
 * those, and returned. So the plain lightning rod drew no lightning, and neither did anything another mod adds at the
 * method's return, like Caverns & Chasms' rods and its copper helmets (proven on VB 1.2.2 + C&C 3.0.1,
 * {@code claude-tools/lightningprobe}). VB's handler is switched off through MixinSquared, so vanilla's search and every
 * other mod's hook run again, and the whole method is wrapped to add VB's rods in: the nearest of all of them wins.
 */
@Mixin(value = ServerLevel.class, priority = 1500)
public abstract class ServerLevelMixin {
    @Unique
    private static final ResourceKey<PoiType> lifeguard$BACKPORT_RODS = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
        ResourceLocation.withDefaultNamespace("lightning_rods"));

    @TargetHandler(mixin = "com.blackgear.vanillabackport.core.mixin.common.weathering_lightning_rods.ServerLevelMixin", name = "findLightningRods")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void lifeguard$letEveryRodBeSearched(BlockPos center, CallbackInfoReturnable<Optional<BlockPos>> backportCallback, CallbackInfo ci) {
        ci.cancel();
    }

    @WrapMethod(method = "findLightningRod")
    private Optional<BlockPos> lifeguard$backportRodsToo(BlockPos center, Operation<Optional<BlockPos>> original) {
        Optional<BlockPos> found = original.call(center);
        ServerLevel level = (ServerLevel) (Object) this;
        Optional<BlockPos> backport = level.getPoiManager().findClosest(type -> type.is(lifeguard$BACKPORT_RODS),
                pos -> pos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1,
                center, 128, PoiManager.Occupancy.ANY)
            .map(pos -> pos.above(1));
        if (found.isEmpty())
            return backport;
        if (backport.isEmpty())
            return found;
        return backport.get().distSqr(center) < found.get().distSqr(center) ? backport : found;
    }
}
