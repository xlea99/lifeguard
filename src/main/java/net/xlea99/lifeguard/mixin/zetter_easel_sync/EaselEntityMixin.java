package net.xlea99.lifeguard.mixin.zetter_easel_sync;

import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.entity.item.EaselEntity;
import me.dantaeusb.zetter.entity.item.state.EaselState;
import me.dantaeusb.zetter.item.CanvasItem;
import me.dantaeusb.zetter.network.packet.SEaselCanvasInitializationPacket;
import me.dantaeusb.zetter.storage.CanvasData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zetter_easel_sync.ZetterEaselSync;
import net.xlea99.lifeguard.sync.Payloads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A blank canvas becomes a real one right before the easel screen opens, instead of on the first stroke: Zetter's own
 * initializeCanvas, then its own init packet to the opener (who isn't "using" the easel yet, so initializeCanvas didn't
 * send it), so the screen opens on the real canvas with its data. See {@link ZetterEaselSync}.
 */
@Mixin(EaselEntity.class)
public abstract class EaselEntityMixin {
    @Inject(method = "openInventory", at = @At("HEAD"))
    private void lifeguard$initializeBeforeOpening(Player player, CallbackInfo ci) {
        EaselEntity easel = (EaselEntity) (Object) this;
        if (easel.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !ZetterEaselSync.ENTRY.active())
            return;
        ItemStack canvas = easel.getCanvasStack();
        if (canvas == null || canvas.isEmpty())
            return;
        EaselState state = easel.getStateHandler();
        if (state.isCanvasInitialized() || !state.initializeCanvas(System.currentTimeMillis()))
            return;
        String code = CanvasItem.getCanvasCode(canvas);
        CanvasData data = Helper.getLevelCanvasTracker(easel.level()).getCanvasData(code);
        if (code != null && data != null)
            Payloads.send(serverPlayer, new SEaselCanvasInitializationPacket(easel.getId(), code, data, System.currentTimeMillis()));
    }
}
