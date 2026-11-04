package net.xlea99.lifeguard.mixin.ac_radiation_glow_batching;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Keeps one radiation glow render type per texture, as vanilla does for its own entity types.
 *
 * <p>Alex's Caves builds a new render type every time an irradiated mob is drawn, and render types are matched by identity, so
 * nothing batches: each mob gets its own setup and draw every frame. Setting up this type also copies the screen's depth into
 * the glow's target, so hundreds of glowing mobs mean hundreds of full-screen copies a frame. The type holds no per-mob state and
 * looks its target up when it's set up, so one per texture draws the same picture. Targeted by name: Lifeguard isn't built
 * against Alex's Caves.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.render.ACRenderTypes")
public abstract class ACRenderTypesMixin {
    @Unique
    private static final Map<ResourceLocation, RenderType> lifeguard$glows = new HashMap<>();
    @Unique
    private static final Map<ResourceLocation, RenderType> lifeguard$blueGlows = new HashMap<>();

    @WrapMethod(method = "getRadiationGlow")
    private static RenderType lifeguard$oneGlowPerTexture(ResourceLocation texture, Operation<RenderType> original) {
        RenderType glow = lifeguard$glows.get(texture);
        if (glow == null) {
            glow = original.call(texture);
            lifeguard$glows.put(texture, glow);
        }
        return glow;
    }

    @WrapMethod(method = "getBlueRadiationGlow")
    private static RenderType lifeguard$oneBlueGlowPerTexture(ResourceLocation texture, Operation<RenderType> original) {
        RenderType glow = lifeguard$blueGlows.get(texture);
        if (glow == null) {
            glow = original.call(texture);
            lifeguard$blueGlows.put(texture, glow);
        }
        return glow;
    }
}
