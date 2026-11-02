package net.xlea99.lifeguard.entry.zetter_easel_sync;

import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.item.CanvasItem;
import me.dantaeusb.zetter.storage.AbstractCanvasData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A canvas item that got a code (opened on an easel) but was never painted goes back to exactly what a fresh blank
 * canvas carries: nothing for a 1x1 (crafted), only its block size for a combined one (the artist table). Its canvas
 * data stays in the world as an orphan, as Zetter leaves any lost canvas: deleting it could break a creative copy
 * sharing the code. See {@link ZetterEaselSync}.
 */
public final class BlankCanvases {
    /** Zetter's fresh canvas color (CanvasData.createFresh fills -2041138). */
    private static final int FRESH = 0xFFE0DACE;

    private BlankCanvases() {
    }

    public static void wipeIfBlank(ItemStack stack, Level level) {
        String code = CanvasItem.getCanvasCode(stack);
        if (code == null)
            return;
        AbstractCanvasData data = Helper.getLevelCanvasTracker(level).getCanvasData(code);
        if (data == null || !isBlank(data))
            return;
        CompoundTag tag = Helper.getOrCreateTag(stack);
        tag.remove(CanvasItem.NBT_TAG_CANVAS_CODE);
        tag.remove(CanvasItem.NBT_TAG_CACHED_RESOLUTION);
        int[] size = tag.getIntArray(CanvasItem.NBT_TAG_CACHED_BLOCK_SIZE);
        if (size.length == 2 && size[0] == 1 && size[1] == 1)
            tag.remove(CanvasItem.NBT_TAG_CACHED_BLOCK_SIZE);
        Helper.setTag(stack, tag);
    }

    private static boolean isBlank(AbstractCanvasData data) {
        int pixels = data.getWidth() * data.getHeight();
        for (int i = 0; i < pixels; i++)
            if (data.getColorAt(i) != FRESH)
                return false;
        return true;
    }
}
