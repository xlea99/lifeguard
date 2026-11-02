package net.xlea99.lifeguard.entry.zetter_emi_recipes.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.core.ZetterItems;
import me.dantaeusb.zetter.item.CanvasItem;
import me.dantaeusb.zetter.item.FrameItem;
import me.dantaeusb.zetter.item.PaintingItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.entry.zetter_copies_like_books.ZetterCopiesLikeBooks;
import net.xlea99.lifeguard.entry.zettify_all_paintings.VirtualPaintings;
import net.xlea99.lifeguard.entry.zetter_emi_recipes.ZetterEmiRecipes;

/**
 * Zetter's special crafting recipes in EMI, with an example painting (vanilla's Kebab, as a zettified painting; any
 * painting works the same). Found by EMI; does nothing unless the entry is active.
 */
@EmiEntrypoint
public final class ZetterEmiPlugin implements EmiPlugin {
    private static final ResourceLocation EXAMPLE = ResourceLocation.withDefaultNamespace("kebab");
    private static final TagKey<Item> PAINT_SOURCES = TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath("zetter", "paint_sources"));

    @Override
    public void register(EmiRegistry registry) {
        if (!ZetterEmiRecipes.ENTRY.active())
            return;
        ItemStack original = painting(new ItemStack(ZetterItems.PAINTING.get()), 0);

        for (var holder : ZetterItems.FRAMES.values()) {
            FrameItem frame = holder.get();
            String path = BuiltInRegistries.ITEM.getKey(frame).getPath();
            EmiStack empty = EmiStack.of(frame);
            EmiStack framed = EmiStack.of(painting(new ItemStack(frame), 0));
            registry.addRecipe(new EmiCraftingRecipe(List.of(empty, EmiStack.of(original)), framed, id("framing/" + path), true));
            registry.addRecipe(new EmiCraftingRecipe(List.of(framed.copy().setRemainder(empty)), EmiStack.of(original),
                id("unframing/" + path), true));
        }

        // Copying: painting + a blank canvas of the same size + a palette (it loses durability) -> the next generation.
        EmiStack canvas = EmiStack.of(ZetterItems.CANVAS.get());
        ItemStack worn = new ItemStack(ZetterItems.PALETTE.get());
        worn.setDamageValue(worn.getMaxDamage() / 4);
        EmiStack palette = EmiStack.of(ZetterItems.PALETTE.get()).setRemainder(EmiStack.of(worn));
        int lastCopyable = ZetterCopiesLikeBooks.ENTRY.active() ? 1 : 2;
        for (int generation = 0; generation <= lastCopyable; generation++) {
            ItemStack from = painting(new ItemStack(ZetterItems.PAINTING.get()), generation);
            ItemStack to = painting(new ItemStack(ZetterItems.PAINTING.get()), Math.min(2, generation + 1));
            registry.addRecipe(new EmiCraftingRecipe(List.of(EmiStack.of(from), canvas, palette), EmiStack.of(to),
                id("copying/generation_" + generation), true));
        }

        // Palette recharge: a used palette + paints (or another palette) -> recharged.
        ItemStack used = new ItemStack(ZetterItems.PALETTE.get());
        used.setDamageValue(used.getMaxDamage() - 1);
        registry.addRecipe(new EmiCraftingRecipe(List.of(EmiStack.of(used), EmiIngredient.of(PAINT_SOURCES)),
            EmiStack.of(ZetterItems.PALETTE.get()), id("palette_recharge"), true));
    }

    /** The example painting's data on a painting or frame item, at a generation. */
    private static ItemStack painting(ItemStack stack, int generation) {
        CompoundTag tag = Helper.getOrCreateTag(stack);
        tag.putString(PaintingItem.NBT_TAG_PAINTING_CODE, VirtualPaintings.code(EXAMPLE));
        tag.putIntArray(CanvasItem.NBT_TAG_CACHED_BLOCK_SIZE, new int[]{1, 1});
        tag.putInt(PaintingItem.NBT_TAG_GENERATION, generation);
        Helper.setTag(stack, tag);
        VirtualPaintings.storeCanonical(stack, EXAMPLE);
        return stack;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "/zetter/" + path);
    }
}
