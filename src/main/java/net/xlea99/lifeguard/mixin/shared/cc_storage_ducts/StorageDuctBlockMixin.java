package net.xlea99.lifeguard.mixin.shared.cc_storage_ducts;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.teamabnormals.caverns_and_chasms.common.block.StorageDuctBlock;
import com.teamabnormals.caverns_and_chasms.common.block.entity.StorageDuctBlockEntity;
import com.teamabnormals.caverns_and_chasms.common.block.entity.StorageDuctHatchBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.duct.StorageDucts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Storage Duct networks, kept by {@link StorageDucts} instead of by each duct: placing, breaking or reshaping a duct
 * moves the stamp, and the walk Caverns & Chasms did there to tell every duct in the run to rebuild (through every
 * chunk the run crosses, however far) is skipped, the moved stamp already saying it. A network too wide to work doesn't
 * open (from a duct's end or a hatch, which both come here), and says why.
 */
@Mixin(StorageDuctBlock.class)
public abstract class StorageDuctBlockMixin {
    @Inject(method = {"onPlace", "onRemove"}, at = @At("HEAD"))
    private void lifeguard$changed(CallbackInfo ci) {
        StorageDucts.changed();
    }

    @WrapOperation(method = {"onPlace", "onRemove"}, at = @At(value = "INVOKE",
        target = "Lcom/teamabnormals/caverns_and_chasms/common/block/StorageDuctBlock;getConnectedStorageDucts(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/teamabnormals/caverns_and_chasms/common/block/StorageDuctBlock$DuctEnd;)Ljava/util/List;"))
    private List<StorageDuctBlockEntity> lifeguard$noResetWalk(Level level, BlockPos pos, BlockState state, StorageDuctBlock.DuctEnd end,
        Operation<List<StorageDuctBlockEntity>> original) {
        return StorageDucts.managed() ? List.of() : original.call(level, pos, state, end);
    }

    @Inject(method = "openMenu", at = @At("HEAD"), cancellable = true)
    private static void lifeguard$sealed(ServerPlayer player, Level level, BlockPos pos, StorageDuctBlock.DuctEnd end,
        StorageDuctHatchBlockEntity hatch, CallbackInfo ci) {
        if (!StorageDucts.sealed(level, pos, level.getBlockState(pos)))
            return;
        StorageDucts.tellSealed(player, level, pos, "lifeguard.message.storage_duct_sealed");
        ci.cancel();
    }
}
