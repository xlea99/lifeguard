package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Reads a render type's states, to find which texture it draws (see {@code GlintVariants#textureOf}). */
@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface CompositeRenderTypeAccessor {
    @Invoker("state")
    RenderType.CompositeState lifeguard$state();
}
