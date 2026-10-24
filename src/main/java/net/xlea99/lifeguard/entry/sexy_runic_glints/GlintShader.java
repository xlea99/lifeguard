package net.xlea99.lifeguard.entry.sexy_runic_glints;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * The item glint shader ({@code assets/lifeguard/shaders/core/rendertype_glint_colorize.*}) for every rune color:
 * exactly vanilla's glint unless the color has a {@code colorize} setting, in which case it reads the item's own
 * pixel and recolors the item by brightness, lit like vanilla lights the item. Its vertices are full item vertices
 * ({@link DefaultVertexFormat#NEW_ENTITY}), so it can only draw render types built with that format; see
 * {@link GlintShards#shader}.
 */
public final class GlintShader {
    @Nullable
    private static ShaderInstance colorize;

    private GlintShader() {
    }

    /** The shader, or null before shaders first load. */
    @Nullable
    public static ShaderInstance colorize() {
        return colorize;
    }

    static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
            ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "rendertype_glint_colorize"), DefaultVertexFormat.NEW_ENTITY),
            shader -> colorize = shader);
    }
}
