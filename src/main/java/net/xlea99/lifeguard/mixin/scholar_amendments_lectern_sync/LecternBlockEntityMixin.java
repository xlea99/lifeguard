package net.xlea99.lifeguard.mixin.scholar_amendments_lectern_sync;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A book taken off a lectern disappears from it for everyone, with Scholar drawing lectern books and Amendments
 * installed.
 *
 * <p>An empty lectern saves nothing, and NeoForge ignores an empty update from the server, so clients never heard the
 * book was gone and Scholar kept drawing it. Scholar adds a marker to the empty lectern's update for exactly this, but
 * through the base block entity's update, which Amendments replaces for lecterns (without calling it). Amendments'
 * update gets Scholar's marker here. Applied after Amendments' own lectern mixin, whose method this is.
 */
@Mixin(value = LecternBlockEntity.class, priority = 1500)
public abstract class LecternBlockEntityMixin {

    @Dynamic(value = "Added by Amendments' LecternBlockEntityUpdateMixin",
        mixin = net.mehvahdjukaar.amendments.mixins.LecternBlockEntityUpdateMixin.class)
    @ModifyReturnValue(method = "getUpdateTag", at = @At("RETURN"))
    private CompoundTag lifeguard$sayTheBookIsGone(CompoundTag tag) {
        if (!tag.contains("Book"))
            tag.putBoolean("BookIsRemoved", true);
        return tag;
    }
}
