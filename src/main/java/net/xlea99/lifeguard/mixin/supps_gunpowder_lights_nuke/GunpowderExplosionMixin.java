package net.xlea99.lifeguard.mixin.supps_gunpowder_lights_nuke;

import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.TntBehavior;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.GunpowderExplosion;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A burning gunpowder trail lights Alex's Caves' nuclear bomb, as Supplementaries' own tags say it should.
 *
 * <p>When a trail burns out it sets off a tiny explosion that lights the blocks around it, but only blocks with a blast
 * resistance of exactly 0. Supplementaries puts the nuclear bomb in {@code #supplementaries:lightable_by_gunpowder} and
 * {@code cannon_tnts}, so the trail connects to it, but the bomb resists explosions (1001), so the trail fizzles next
 * to it; the lightable tag's other path only flips a {@code lit} property, which the bomb doesn't have. Here a block in
 * both tags is lit the way Supplementaries lights its TNT-likes (a burning arrow), whatever its resistance. Blocks with
 * no resistance are left to Supplementaries.
 */
@Mixin(GunpowderExplosion.class)
public abstract class GunpowderExplosionMixin {

    @Inject(method = "explodeSingleBlock", at = @At("HEAD"))
    private void lifeguard$lightResistant(int x, int y, int z, CallbackInfo ci) {
        Level level = ((ExplosionAccessor) this).lifeguard$level();
        BlockPos pos = new BlockPos(x, y, z);
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.isEmpty() && !fluid.is(ModFluids.LUMISENE_FLUID.get()))
            return;
        BlockState state = level.getBlockState(pos);
        if (state.is(ModTags.LIGHTABLE_BY_GUNPOWDER) && TntBehavior.explodesWhenHitByFlamingArrow(state)
            && state.getExplosionResistance(level, pos, (GunpowderExplosion) (Object) this) != 0)
            TntBehavior.igniteTntHack(state, level, pos);
    }
}
