package net.xlea99.lifeguard.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

/**
 * The sounds of another block, looked up when first played: that block may be registered after ours, and may be another
 * mod's we don't compile against. The stone sounds handed to the constructor are never used.
 */
public final class BorrowedSoundType extends SoundType {
    private final ResourceLocation block;
    private SoundType borrowed;

    public BorrowedSoundType(ResourceLocation block) {
        super(1.0F, 1.0F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE, SoundEvents.STONE_HIT,
            SoundEvents.STONE_FALL);
        this.block = block;
    }

    private SoundType borrowed() {
        if (borrowed == null) {
            Block from = BuiltInRegistries.BLOCK.get(block);
            if (from == Blocks.AIR)
                return SoundType.METAL;
            borrowed = from.defaultBlockState().getSoundType();
        }
        return borrowed;
    }

    @Override
    public float getVolume() {
        return borrowed().getVolume();
    }

    @Override
    public float getPitch() {
        return borrowed().getPitch();
    }

    @Override
    public SoundEvent getBreakSound() {
        return borrowed().getBreakSound();
    }

    @Override
    public SoundEvent getStepSound() {
        return borrowed().getStepSound();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return borrowed().getPlaceSound();
    }

    @Override
    public SoundEvent getHitSound() {
        return borrowed().getHitSound();
    }

    @Override
    public SoundEvent getFallSound() {
        return borrowed().getFallSound();
    }
}
