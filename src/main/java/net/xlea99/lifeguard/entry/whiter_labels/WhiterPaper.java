package net.xlea99.lifeguard.entry.whiter_labels;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Placed labels in whiter paper ({@link WhiterLabels}).
 *
 * <p>The paper: Labels draws it with its own model, {@code labels:block/label}; ours is that model with a whiter copy
 * of its texture, made from Labels' own on the player's machine each resource reload (Labels' art can't be shipped,
 * only recolored), and drawn instead by {@code mixin.whiter_labels.LabelEntityRendererMixin}. Every paper color keeps
 * its lightness plus a touch and a little over half its color, so the tan reads as plain paper.
 *
 * <p>The drawing: an undyed label draws its item in shades from a light to a dark color (cream to brown in every one of
 * Labels' presets), which reads sepia on whiter paper. The light one gets the paper's own shift, the dark one only loses
 * the same share of its color, so the ink stays as dark ({@code mixin.whiter_labels.ColorManagerMixin}).
 */
public final class WhiterPaper {
    public static final ModelResourceLocation MODEL =
        ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "block/label_white"));
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "block/label_white");
    private static final ResourceLocation LABELS_PAPER = ResourceLocation.fromNamespaceAndPath("labels", "block/label");

    /** Lightness up this much (OKLab L), this share of color kept. */
    private static final double LIFT = 0.03;
    private static final double CHROMA = 0.55;

    private WhiterPaper() {
    }

    public static void register(IEventBus modBus) {
        RegHelper.registerDynamicResourceProvider(new Pack());
        modBus.addListener((ModelEvent.RegisterAdditional event) -> event.register(MODEL));
    }

    /** An undyed drawing's light or dark color (NativeImage ABGR), given Labels' own. */
    public static int undyed(int abgr, boolean light) {
        return whiter(abgr, light);
    }

    private static int whiter(int abgr, boolean lift) {
        double[] lab = OkLab.fromAbgr(abgr);
        double[] out = {lift ? Math.min(1, lab[0] + LIFT) : lab[0], lab[1] * CHROMA, lab[2] * CHROMA};
        return OkLab.toAbgr(out) & 0x00FFFFFF | abgr & 0xFF000000;
    }

    private static final class Pack extends DynamicClientResourceProvider {
        Pack() {
            super(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "whiter_labels"), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
        }

        @Override
        protected Collection<String> gatherSupportedNamespaces() {
            return List.of(Lifeguard.MODID);
        }

        @Override
        protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
            executor.accept((manager, sink) -> {
                try (TextureImage paper = TextureImage.open(manager, LABELS_PAPER)) {
                    NativeImage image = paper.getImage();
                    for (int y = 0; y < image.getHeight(); y++)
                        for (int x = 0; x < image.getWidth(); x++) {
                            int abgr = image.getPixelRGBA(x, y);
                            if (abgr >>> 24 != 0)
                                image.setPixelRGBA(x, y, whiter(abgr, true));
                        }
                    sink.addTexture(TEXTURE, paper);
                } catch (IOException | RuntimeException e) {
                    Lifeguard.LOGGER.warn("[lifeguard] whiter_labels: couldn't recolor Labels' paper, labels stay tan", e);
                    return;
                }
                sink.addBlockModel(ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "label_white"), JsonParser.parseString(
                    "{\"parent\": \"labels:block/label\", \"textures\": {\"particle\": \"" + TEXTURE + "\", \"back\": \"" + TEXTURE + "\"}}"));
            });
        }
    }
}
