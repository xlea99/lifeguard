package net.xlea99.lifeguard.mixin.no_supps_jar_fish;

import net.mehvahdjukaar.supplementaries.common.misc.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xlea99.lifeguard.entry.no_supps_jar_fish.NoSuppsJarFish;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fish buckets no longer empty into Supplementaries' jars. A fish poured into a jar shows as a little hand-drawn sprite,
 * but only for the handful of fish Supplementaries drew one for; any other fish poured in is invisible (the jar keeps
 * only the bucket, so there's no mob to draw either). This is where a fish bucket meets a jar, from a player and from a
 * dispenser alike, so it's turned away here. Fish already in jars stay, and an empty bucket still takes them out.
 */
@Mixin(MobContainer.class)
public abstract class MobContainerMixin {

    @Inject(method = "interactWithBucket", at = @At("HEAD"), cancellable = true)
    private void lifeguard$noFish(ItemStack stack, Level level, BlockPos pos, @Nullable Player player, InteractionHand hand,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (NoSuppsJarFish.ENTRY.active() && BucketHelper.isFishBucket(stack.getItem()))
            cir.setReturnValue(false);
    }
}
