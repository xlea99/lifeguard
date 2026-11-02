package net.xlea99.lifeguard.mixin.scholar_supps_book_piles;

import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The book a pile's drawn book stands for, which Supplementaries keeps private (for PileCovers). */
@Mixin(BookPileBlockTile.BookVisualData.class)
public interface BookVisualDataAccessor {
    @Accessor("stack")
    ItemStack lifeguard$getStack();
}
