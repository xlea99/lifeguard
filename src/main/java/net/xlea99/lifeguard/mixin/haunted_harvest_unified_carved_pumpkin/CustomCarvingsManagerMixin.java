package net.xlea99.lifeguard.mixin.haunted_harvest_unified_carved_pumpkin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.mehvahdjukaar.hauntedharvest.CustomCarvingsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.CarvingPools;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.UnifiedPumpkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Haunted Harvest's carvings, with vanilla's face as one of them ({@link UnifiedPumpkin}):
 * <ul>
 * <li>its own picks of a vanilla pumpkin (a share of the kids' carvings, the farm fields, Halloween mobs' heads) give its
 * own pumpkin instead: vanilla's face, or from the pack's pools for fields (worldgen) and mobs' heads;</li>
 * <li>what it reaches for as "the vanilla face" (lighting a vanilla carved pumpkin with a modded torch) is vanilla's
 * real one, not its own {@code classic};</li>
 * <li>the {@code lifeguard:vanilla} preset only loads while the entry is on (NeoForge doesn't apply load conditions to
 * this kind of file, so it's taken out here).</li>
 * </ul>
 */
@Mixin(CustomCarvingsManager.class)
public abstract class CustomCarvingsManagerMixin {

    @WrapOperation(method = "placeRandomPumpkin", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean lifeguard$placeHauntedPumpkin(LevelAccessor level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        return UnifiedPumpkin.setBlock(level, pos, state, flags, (l, p, s, f) -> original.call(l, p, s, f));
    }

    @ModifyReturnValue(method = "getRandomPumpkinItem", at = @At("RETURN"))
    private static ItemStack lifeguard$giveHauntedPumpkin(ItemStack pumpkin, @Local(argsOnly = true) LevelAccessor level) {
        if (UnifiedPumpkin.isVanilla(pumpkin) && UnifiedPumpkin.active())
            return UnifiedPumpkin.hauntedItem(pumpkin, CarvingPools.forMobHead(level.getRandom()));
        return pumpkin;
    }

    @ModifyReturnValue(method = "getVanillaFace", at = @At("RETURN"))
    private static long[] lifeguard$theRealVanillaFace(long[] face) {
        return UnifiedPumpkin.active() ? UnifiedPumpkin.VANILLA_FACE.clone() : face;
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD"))
    private void lifeguard$vanillaPresetOnlyWhileOn(Map<ResourceLocation, JsonElement> presets, ResourceManager manager, ProfilerFiller profiler,
                                                    CallbackInfo ci) {
        if (!UnifiedPumpkin.active())
            presets.remove(UnifiedPumpkin.VANILLA_PRESET);
    }
}
