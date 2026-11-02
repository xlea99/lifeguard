package net.xlea99.lifeguard.entry.zettify_all_paintings;

import java.util.Optional;
import javax.annotation.Nullable;
import me.dantaeusb.zetter.core.Helper;
import me.dantaeusb.zetter.item.CanvasItem;
import me.dantaeusb.zetter.item.PaintingItem;
import me.dantaeusb.zetter.menu.artisttable.CanvasCombinationAction;
import me.dantaeusb.zetter.storage.AbstractCanvasData;
import me.dantaeusb.zetter.storage.PaintingData;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Virtual Zetter paintings, the common half ({@link ZettifyAllPaintings}). A virtual painting is a Zetter painting code
 * that points at a registered painting variant: {@code lifeguard_painting_<namespace>/<path>} (a valid lowercase
 * texture path, as Zetter names each canvas texture {@code canvas/<code>}; a namespace has no '/', so the original id
 * reads back exactly). Clients build the pixels themselves ({@link ClientVirtualPaintings}); the server only ever
 * answers with a pixel-less stub (size, title and author keys), enough for breaking, pick-block, framing and copying.
 */
public final class VirtualPaintings {
    public static final String CODE_PREFIX = "lifeguard_painting_";

    private VirtualPaintings() {
    }

    public static boolean isVirtual(@Nullable String code) {
        return code != null && code.startsWith(CODE_PREFIX);
    }

    public static String code(ResourceLocation variant) {
        return CODE_PREFIX + variant.getNamespace() + "/" + variant.getPath();
    }

    /** The painting variant a virtual code points at, or null for any other code. */
    @Nullable
    public static ResourceLocation variant(@Nullable String code) {
        if (!isVirtual(code))
            return null;
        String rest = code.substring(CODE_PREFIX.length());
        int slash = rest.indexOf('/');
        return slash <= 0 ? null : ResourceLocation.tryBuild(rest.substring(0, slash), rest.substring(slash + 1));
    }

    /** One of the shapes Zetter's frames come in (CanvasCombinationAction.paintingShapes), in blocks. */
    public static boolean fitsZetter(int width, int height) {
        for (int[] shape : CanvasCombinationAction.paintingShapes)
            if (shape[0] == width && shape[1] == height)
                return true;
        return false;
    }

    /** Vanilla's keys: {@code painting.<namespace>.<path>.title} / {@code .author}. */
    public static String titleKey(ResourceLocation variant) {
        return "painting." + variant.getNamespace() + "." + variant.getPath() + ".title";
    }

    public static String authorKey(ResourceLocation variant) {
        return "painting." + variant.getNamespace() + "." + variant.getPath() + ".author";
    }

    /** "fall_forest" -> "Fall Forest", for a painting whose mod ships no title. */
    public static String humanize(ResourceLocation variant) {
        StringBuilder out = new StringBuilder();
        for (String part : variant.getPath().replace('/', '_').split("_")) {
            if (part.isEmpty())
                continue;
            if (!out.isEmpty())
                out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    /**
     * Gives a virtual painting's item its one canonical form, whichever side and path made it (the creative tab and
     * pick-block build from the client's real data, a drop from the server's stub): title and author as keys, x16, the
     * same as {@link #serverStub}. Otherwise one painting came in up to three spellings that never stack (user,
     * 2026-11-01: "Weathered Statue" vs its title key; a 32 px texture would add 32 vs 16). Names are translated where
     * they're shown (PaintingItemMixin, PaintingItemTooltipMixin).
     */
    public static void storeCanonical(ItemStack stack, ResourceLocation variant) {
        CompoundTag tag = Helper.getOrCreateTag(stack);
        tag.putString(PaintingItem.NBT_TAG_CACHED_PAINTING_TITLE, titleKey(variant));
        tag.putString(PaintingItem.NBT_TAG_CACHED_AUTHOR_NAME, authorKey(variant));
        tag.putInt(CanvasItem.NBT_TAG_CACHED_RESOLUTION, AbstractCanvasData.Resolution.x16.getNumeric());
        Helper.setTag(stack, tag);
    }

    /**
     * The server's answer for a virtual code: the variant's size at x16 with blank pixels, its title and author keys as
     * meta (the names shown are translated on the client). Null for a code whose variant isn't registered or doesn't
     * fit a Zetter frame. Built fresh each time (cheap; never stale if datapacks change a variant).
     */
    @Nullable
    public static PaintingData serverStub(Level level, String code) {
        ResourceLocation id = variant(code);
        if (id == null)
            return null;
        Optional<PaintingVariant> variant = level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getOptional(id);
        if (variant.isEmpty() || !fitsZetter(variant.get().width(), variant.get().height()))
            return null;
        int width = variant.get().width() * 16;
        int height = variant.get().height() * 16;
        PaintingData stub = PaintingData.BUILDER.createWrap(AbstractCanvasData.Resolution.x16, width, height,
            new byte[width * height * 4]);
        stub.setMetaProperties(PaintingData.FALLBACK_UUID, authorKey(id), titleKey(id));
        return stub;
    }
}
