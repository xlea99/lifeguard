package net.xlea99.lifeguard.mixin.bucket_mob_flicker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.xlea99.lifeguard.entry.bucket_mob_flicker.BucketedMobs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A bucket's mob gets the bucket's data before it's added to the world ({@link BucketedMobs}). */
@Mixin(MobBucketItem.class)
public abstract class MobBucketItemMixin {

    @WrapOperation(method = "spawn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;"))
    private Entity lifeguard$loadBeforeAdding(EntityType<?> type, ServerLevel level, ItemStack bucket, Player player, BlockPos pos,
                                              MobSpawnType spawnType, boolean offsetY, boolean offsetYMore, Operation<Entity> original) {
        return BucketedMobs.spawnLoaded(type, level, bucket, player, pos, spawnType, offsetY, offsetYMore);
    }
}
