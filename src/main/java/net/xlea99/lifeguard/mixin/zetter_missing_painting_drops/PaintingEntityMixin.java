package net.xlea99.lifeguard.mixin.zetter_missing_painting_drops;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.entity.item.PaintingEntity;
import me.dantaeusb.zetter.item.CanvasItem;
import me.dantaeusb.zetter.item.PaintingItem;
import me.dantaeusb.zetter.storage.PaintingData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.entry.zetter_missing_painting_drops.ZetterMissingPaintingDrops;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A painting with no picture data drops its frame carrying the code, the entity's own size and its generation, instead
 * of storePaintingData dereferencing null. See {@link ZetterMissingPaintingDrops}.
 */
@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin {
    @WrapOperation(method = "dropItem", at = @At(value = "INVOKE",
        target = "Lme/dantaeusb/zetter/item/FrameItem;storePaintingData(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Lme/dantaeusb/zetter/storage/PaintingData;I)V"))
    private void lifeguard$keepWhatsKnown(ItemStack stack, String code, PaintingData data, int generation,
                                          Operation<Void> original) {
        if (data != null) {
            original.call(stack, code, data, generation);
            return;
        }
        PaintingEntity self = (PaintingEntity) (Object) this;
        CompoundTag tag = Helper.getOrCreateTag(stack);
        tag.putString(PaintingItem.NBT_TAG_PAINTING_CODE, code);
        tag.putIntArray(CanvasItem.NBT_TAG_CACHED_BLOCK_SIZE, new int[]{self.getBlockWidth(), self.getBlockHeight()});
        tag.putInt(PaintingItem.NBT_TAG_GENERATION, generation);
        Helper.setTag(stack, tag);
    }
}
