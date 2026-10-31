package net.xlea99.lifeguard.mixin.shared.cc_storage_ducts;

import com.teamabnormals.caverns_and_chasms.common.block.entity.StorageDuctBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.xlea99.lifeguard.duct.DuctNetworkHandler;
import net.xlea99.lifeguard.duct.StorageDucts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A Storage Duct hands out a {@link DuctNetworkHandler} instead of Caverns & Chasms' kept one ({@link StorageDucts}).
 * {@code invalidateCapabilities} is where every duct removal lands: NeoForge calls it from {@code setRemoved} (a break,
 * or its chunk unloading) and on being placed or loaded back.
 */
@Mixin(StorageDuctBlockEntity.class)
public abstract class StorageDuctBlockEntityMixin {
    @Unique
    private DuctNetworkHandler lifeguard$network;

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private void lifeguard$network(Direction side, CallbackInfoReturnable<IItemHandler> cir) {
        StorageDuctBlockEntity self = (StorageDuctBlockEntity) (Object) this;
        if (self.getLevel() == null || self.getLevel().isClientSide() || !StorageDucts.managed())
            return;
        if (lifeguard$network == null)
            lifeguard$network = new DuctNetworkHandler(self);
        cir.setReturnValue(lifeguard$network);
    }

    @Inject(method = "invalidateCapabilities", at = @At("HEAD"))
    private void lifeguard$changed(CallbackInfo ci) {
        StorageDucts.changed();
    }

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    private void lifeguard$sealedMenu(int id, Inventory inventory, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        StorageDuctBlockEntity self = (StorageDuctBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level != null && StorageDucts.sealed(level, self.getBlockPos(), self.getBlockState()))
            cir.setReturnValue(null);
    }
}
