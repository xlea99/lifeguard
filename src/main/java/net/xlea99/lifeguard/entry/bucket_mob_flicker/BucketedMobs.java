package net.xlea99.lifeguard.entry.bucket_mob_flicker;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * A mob let out of a bucket arrives looking the way it was caught ({@link BucketMobFlicker}).
 *
 * <p>Vanilla's bucket spawns the mob (its spawn code rolls a look: tropical fish pick a random pattern even from a
 * bucket), adds it to the world, which sends it to nearby players at once, and only then loads what the bucket saved
 * (its variant, size, health...). So the mob flashes a random look for a tick before turning into the one caught.
 * Here the bucket's data loads as part of spawning, after the spawn code's roll and before the mob is added: the order
 * every mob loaded from a save already gets. Vanilla then loads it again, which sets the same values. Only buckets that
 * let mobs out vanilla's way are touched; mods with their own bucket code are untouched.
 */
public final class BucketedMobs {
    private BucketedMobs() {
    }

    /** {@link EntityType#spawn} as the bucket calls it, with the bucket's data loaded before the mob is added. */
    @Nullable
    public static <T extends Entity> T spawnLoaded(EntityType<T> type, ServerLevel level, ItemStack bucket, @Nullable Player player,
                                                   BlockPos pos, MobSpawnType spawnType, boolean offsetY, boolean offsetYMore) {
        Consumer<T> config = EntityType.<T>createDefaultStackConfig(level, bucket, player).andThen(mob -> {
            if (mob instanceof Bucketable bucketable) {
                bucketable.loadFromBucketTag(bucket.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag());
                bucketable.setFromBucket(true);
            }
        });
        return type.spawn(level, config, pos, spawnType, offsetY, offsetYMore);
    }
}
