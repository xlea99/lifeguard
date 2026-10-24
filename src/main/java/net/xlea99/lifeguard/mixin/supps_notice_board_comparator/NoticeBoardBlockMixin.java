package net.xlea99.lifeguard.mixin.supps_notice_board_comparator;

import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A comparator reads a notice board holding something again. 4.0.2 meant to read how far through a book the board is,
 * but the board only counts its pages on the client (the server always thinks a book has one page) and rounds the
 * progress down to 0 or 1, so the comparator reads 1 for anything. This reads 1 on a book's first page up to 15 on its
 * last, and 15 for anything else on the board (maps, banner patterns, one-page books), as Supplementaries 3.1 did for
 * every item.
 */
@Mixin(NoticeBoardBlock.class)
public abstract class NoticeBoardBlockMixin {

    @Inject(method = "getAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void lifeguard$pageProgress(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!(level.getBlockEntity(pos) instanceof NoticeBoardBlockTile board) || board.isEmpty()) {
            cir.setReturnValue(0);
            return;
        }
        int pages = pageCount(board.getDisplayedItem());
        if (pages <= 1) {
            cir.setReturnValue(15);
            return;
        }
        int page = board.getPageIndex() % pages;
        cir.setReturnValue(1 + Math.round(14.0F * page / (pages - 1)));
    }

    private static int pageCount(ItemStack stack) {
        WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written != null)
            return written.pages().size();
        WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        return writable != null ? writable.pages().size() : 0;
    }
}
