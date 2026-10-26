package net.xlea99.lifeguard.mixin.haunted_harvest_torch_lanterns;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns.TorchLanterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The unlocked lanterns drop through our loot tables: Haunted Harvest's own for them only load with Autumnity
 * ({@link TorchLanterns}).
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @ModifyReturnValue(method = "getLootTable", at = @At("RETURN"))
    private ResourceKey<LootTable> lifeguard$torchLanternDrops(ResourceKey<LootTable> table) {
        if (!((Object) this instanceof Block block))
            return table;
        ResourceKey<LootTable> ours = TorchLanterns.lootTable(block);
        return ours != null ? ours : table;
    }
}
