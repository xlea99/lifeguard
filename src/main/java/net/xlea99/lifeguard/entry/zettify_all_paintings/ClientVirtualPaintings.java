package net.xlea99.lifeguard.entry.zettify_all_paintings;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import me.dantaeusb.zetter.capability.canvastracker.CanvasClientTracker;
import me.dantaeusb.zetter.core.ZetterItems;
import me.dantaeusb.zetter.item.PaintingItem;
import me.dantaeusb.zetter.storage.AbstractCanvasData;
import me.dantaeusb.zetter.storage.PaintingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Virtual Zetter paintings on the client ({@link ZettifyAllPaintings}): each one's pixels come from this client's own
 * resource packs, read from the variant's texture ({@code <namespace>:textures/painting/<path>.png}), as vanilla's
 * painting atlas does. Registered into Zetter's client tracker as unmanaged canvases (the way Zetter registers its own
 * blank default canvases), which Zetter never times out and never asks the server for. Skipped: animated textures (the
 * user: don't mangle them with a first frame), shapes Zetter can't frame, textures that aren't 16/32/64 px per block or
 * aren't the variant's proportions, missing textures.
 */
public final class ClientVirtualPaintings {
    private ClientVirtualPaintings() {
    }

    /** A painting variant that becomes a virtual painting here, with its pixels. */
    private record Built(ResourceLocation id, String code, PaintingData data) {
    }

    /** Every painting variant this client can turn into a virtual painting, sorted by id. */
    private static List<Built> build(HolderLookup.RegistryLookup<PaintingVariant> variants) {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        List<Built> built = new ArrayList<>();
        variants.listElements().sorted(Comparator.comparing(holder -> holder.key().location())).forEach(holder -> {
            ResourceLocation id = holder.key().location();
            PaintingVariant variant = holder.value();
            if (!VirtualPaintings.fitsZetter(variant.width(), variant.height()))
                return;
            ResourceLocation asset = variant.assetId();
            ResourceLocation texture = asset.withPath(path -> "textures/painting/" + path + ".png");
            Optional<Resource> resource = resources.getResource(texture);
            if (resource.isEmpty())
                return;
            try {
                if (resource.get().metadata().getSection(AnimationMetadataSection.SERIALIZER).isPresent())
                    return;
                PaintingData data = read(resource.get(), variant);
                if (data == null)
                    return;
                data.setMetaProperties(PaintingData.FALLBACK_UUID, translated(VirtualPaintings.authorKey(id), ""),
                    translated(VirtualPaintings.titleKey(id), VirtualPaintings.humanize(id)));
                data.setManaged(false);
                built.add(new Built(id, VirtualPaintings.code(id), data));
            } catch (IOException | RuntimeException e) {
                Lifeguard.LOGGER.warn("zettify_all_paintings: couldn't read {} ({})", texture, e.toString());
            }
        });
        return built;
    }

    /** The texture as Zetter painting data (ARGB, row by row), or null if its size doesn't suit the variant. */
    private static PaintingData read(Resource resource, PaintingVariant variant) throws IOException {
        try (InputStream in = resource.open(); NativeImage image = NativeImage.read(in)) {
            int width = image.getWidth();
            int height = image.getHeight();
            if (width % variant.width() != 0 || height % variant.height() != 0)
                return null;
            int perBlock = width / variant.width();
            AbstractCanvasData.Resolution resolution = AbstractCanvasData.Resolution.get(perBlock);
            if (resolution == null || height / variant.height() != perBlock)
                return null;
            byte[] color = new byte[width * height * 4];
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++) {
                    // NativeImage gives ABGR; Zetter stores big-endian ARGB ints, pixel index y * width + x.
                    int abgr = image.getPixelRGBA(x, y);
                    int i = (y * width + x) * 4;
                    color[i] = (byte) (abgr >>> 24);
                    color[i + 1] = (byte) abgr;
                    color[i + 2] = (byte) (abgr >>> 8);
                    color[i + 3] = (byte) (abgr >>> 16);
                }
            return PaintingData.BUILDER.createWrap(resolution, width, height, color);
        }
    }

    private static String translated(String key, String fallback) {
        return Language.getInstance().getOrDefault(key, fallback);
    }

    /** Zetter's client tracker just got its level: register every virtual painting into it. */
    public static void registerAll(CanvasClientTracker tracker, Level level) {
        if (!ZettifyAllPaintings.ENTRY.active())
            return;
        Optional<HolderLookup.RegistryLookup<PaintingVariant>> variants = level.registryAccess()
            .lookup(Registries.PAINTING_VARIANT);
        if (variants.isEmpty())
            return;
        long now = System.currentTimeMillis();
        List<Built> built = build(variants.get());
        for (Built painting : built)
            tracker.registerCanvasData(painting.code(), painting.data(), now);
        Lifeguard.LOGGER.info("zettify_all_paintings: {} virtual paintings registered", built.size());
    }

    /** For now (testing): every virtual painting as a Zetter painting item in the Functional Blocks tab. */
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FUNCTIONAL_BLOCKS || !ZettifyAllPaintings.ENTRY.active())
            return;
        Optional<HolderLookup.RegistryLookup<PaintingVariant>> variants = event.getParameters().holders()
            .lookup(Registries.PAINTING_VARIANT);
        if (variants.isEmpty())
            return;
        for (Built painting : build(variants.get())) {
            ItemStack stack = new ItemStack(ZetterItems.PAINTING.get());
            PaintingItem.storePaintingData(stack, painting.code(), painting.data(), PaintingItem.GENERATION_ORIGINAL);
            event.accept(stack);
        }
    }
}
