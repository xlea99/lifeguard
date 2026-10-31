package net.xlea99.lifeguard.mixin.species_dyeable_chaindeliers;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.Chaindeliers;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.DyeableChaindelier;
import org.spongepowered.asm.mixin.Mixin;

/**
 * A dyed Chaindelier drops with its color, and picking it gives a dyed one ({@link Chaindeliers}). Its drop is Species'
 * own loot table's, with the color put on (the Hopelight's loot table copies its own; the Chaindelier's is a plain
 * drop, and another mod's file isn't ours to override); the color goes on whatever the setting, so a dyed one broken
 * while it's off isn't washed by it. Written against our build of Species' 1.21.1 port (PR #88), by class name.
 */
@Mixin(targets = "com.ninni.species.server.block.ChaindelierBlock")
public abstract class ChaindelierBlockMixin extends BaseEntityBlock {
    protected ChaindelierBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DyeableChaindelier dyed
            && dyed.lifeguard$getColor() != Chaindeliers.DEFAULT_COLOR)
            for (ItemStack drop : drops)
                if (Chaindeliers.is(drop))
                    Chaindeliers.setColor(drop, dyed.lifeguard$getColor());
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (Chaindeliers.enabled() && level.getBlockEntity(pos) instanceof DyeableChaindelier dyed)
            Chaindeliers.setColor(stack, dyed.lifeguard$getColor());
        return stack;
    }
}
