package net.xlea99.lifeguard.mixin.zetter_easel_sync;

import java.util.ArrayList;
import me.dantaeusb.zetter.entity.item.state.EaselState;
import me.dantaeusb.zetter.entity.item.state.representation.CanvasAction;
import me.dantaeusb.zetter.entity.item.state.representation.CanvasSnapshot;
import net.xlea99.lifeguard.entry.zetter_easel_sync.ZetterEaselSync;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The client's merge of the server's history, done right: Zetter's own, with its action merge replaced by "already have
 * this id: mark it synced; otherwise insert it at its start time". The snapshot handling and everything after the merge
 * are Zetter's, line for line. A history for another canvas falls through to Zetter (which logs and ignores it).
 * See {@link ZetterEaselSync}.
 */
@Mixin(EaselState.class)
public abstract class EaselStateMixin {
    @Shadow
    @Final
    private ArrayList<CanvasAction> actions;

    @Shadow
    @Final
    private ArrayList<CanvasSnapshot> snapshots;

    @Shadow
    private boolean sync;

    @Shadow
    private String getCanvasCode() {
        throw new AssertionError();
    }

    @Shadow
    private CanvasSnapshot getLastSnapshot() {
        throw new AssertionError();
    }

    @Shadow
    private void insertSnapshot(CanvasSnapshot snapshot) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract void unfreeze();

    @Shadow
    private void wipeCanceledActionsAndDiscardSnapshots() {
        throw new AssertionError();
    }

    @Shadow
    private void cleanupActionHistory() {
        throw new AssertionError();
    }

    @Shadow
    public abstract void recollectPaintingData();

    @Shadow
    protected abstract void onStateChanged();

    @Inject(method = "processHistorySyncClient", at = @At("HEAD"), cancellable = true)
    private void lifeguard$mergeInTimeOrder(String canvasCode, boolean sync, CanvasSnapshot snapshot,
                                            ArrayList<CanvasAction> incoming, CallbackInfo ci) {
        if (!ZetterEaselSync.ENTRY.active() || !canvasCode.equals(this.getCanvasCode()))
            return;
        ci.cancel();

        if (snapshot != null) {
            CanvasSnapshot last = this.getLastSnapshot();
            if (last == null) {
                this.insertSnapshot(snapshot);
            } else if (last.id != snapshot.id) {
                if (this.snapshots.size() >= EaselState.SNAPSHOT_HISTORY_SIZE)
                    this.snapshots.remove(0);
                this.insertSnapshot(snapshot);
            }
        }

        if (incoming == null || incoming.isEmpty()) {
            this.sync = sync;
            this.recollectPaintingData();
            return;
        }

        int added = 0;
        for (CanvasAction action : incoming) {
            CanvasAction existing = lifeguard$find(action.id);
            if (existing != null) {
                existing.setSync();
                continue;
            }
            int at = this.actions.size();
            while (at > 0 && this.actions.get(at - 1).getStartTime() > action.getStartTime())
                at--;
            this.actions.add(at, action);
            added++;
        }

        this.unfreeze();
        if (added > 0) {
            this.wipeCanceledActionsAndDiscardSnapshots();
            this.cleanupActionHistory();
        }
        this.sync = sync;
        this.recollectPaintingData();
        this.onStateChanged();
    }

    @org.spongepowered.asm.mixin.Unique
    private CanvasAction lifeguard$find(int id) {
        for (CanvasAction action : this.actions)
            if (action.id == id)
                return action;
        return null;
    }
}
