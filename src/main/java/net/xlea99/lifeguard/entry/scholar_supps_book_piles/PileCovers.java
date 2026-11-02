package net.xlea99.lifeguard.entry.scholar_supps_book_piles;

import io.github.mortuusars.scholar.Scholar;
import io.github.mortuusars.scholar.book.BookColor;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.SpecRecolors;
import org.jetbrains.annotations.Nullable;

/**
 * Colored covers for Scholar's books in Supplementaries' book piles ({@link ScholarSuppsBookPiles}).
 *
 * <p>A pile draws each book as one of Supplementaries' book models, picked by item (its {@code placeable_books_visuals}
 * data, which can't carry a color): a written book or a book and quill always got the brown one. A colored one now also
 * gets a cover overlay drawn over it, tinted with the book's color the way grass tints its gray overlay: our models, the
 * same shape with only the cover, in gray, made from Supplementaries' own textures on the player's machine
 * ({@code recolors/scholar_supps_book_piles.json}; its art is never shipped). Piles are baked into the chunk like
 * blocks, so the color rides on the overlay's tint index: {@link #TINTED} plus the color itself, read back by the block
 * color handler registered here for the two pile blocks. The drawing is
 * {@code mixin.scholar_supps_book_piles.BookPileBlockTileRendererMixin}.
 * (Scholar 1.2.10, Supplementaries 4.0.2, Moonlight 3.7.2, read 2026-10-31.)
 */
public final class PileCovers {
    /** Set on a tint index that carries a color (in its low 24 bits) rather than an index. */
    public static final int TINTED = 0x1000000;

    private static final ModelResourceLocation WRITTEN_COVER = cover("book_written_cover");
    private static final ModelResourceLocation WRITABLE_COVER = cover("book_and_quill_cover");

    private PileCovers() {
    }

    public static void register(IEventBus modBus) {
        SpecRecolors.register(ScholarSuppsBookPiles.ENTRY.id(), SpecRecolors.spec("scholar_supps_book_piles"), Lifeguard.MODID);
        modBus.addListener((ModelEvent.RegisterAdditional event) -> {
            event.register(WRITTEN_COVER);
            event.register(WRITABLE_COVER);
        });
        modBus.addListener(PileCovers::onBlockColors);
    }

    /**
     * The cover overlay for a book Scholar colored, or null when it has no color of its own. Golden books count, in
     * Scholar's gold. Scholar dyes book and quills only; a signed book keeps its color.
     */
    @Nullable
    public static ModelResourceLocation coverModel(ItemStack book) {
        if (!book.has(DataComponents.DYED_COLOR) && !book.has(Scholar.DataComponents.BOOK_GOLDEN))
            return null;
        if (book.is(Items.WRITABLE_BOOK))
            return WRITABLE_COVER;
        if (book.is(Items.WRITTEN_BOOK))
            return WRITTEN_COVER;
        return null;
    }

    /** The tint index carrying a colored book's color, as Scholar reads it (gold for a golden book). */
    public static int tint(ItemStack book) {
        return TINTED | BookColor.of(book) & 0xFFFFFF;
    }

    private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        for (String pile : new String[] {"book_pile", "book_pile_horizontal"}) {
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("supplementaries", pile));
            if (block != Blocks.AIR)
                event.register((state, level, pos, tint) -> (tint & ~0xFFFFFF) == TINTED ? tint & 0xFFFFFF : -1, block);
        }
    }

    private static ModelResourceLocation cover(String name) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "block/books/" + name));
    }
}
