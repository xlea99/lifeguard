package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import java.util.Optional;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Reads which texture a texture state binds (see {@code GlintVariants#textureOf}). */
@Mixin(RenderStateShard.EmptyTextureStateShard.class)
public interface EmptyTextureStateShardAccessor {
    @Invoker("cutoutTexture")
    Optional<ResourceLocation> lifeguard$cutoutTexture();
}
