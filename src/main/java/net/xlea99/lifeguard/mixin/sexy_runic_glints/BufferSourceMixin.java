package net.xlea99.lifeguard.mixin.sexy_runic_glints;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintShards;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintVariants;
import net.xlea99.lifeguard.entry.sexy_runic_glints.LenientVertices;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Buffers for glints that take full item vertices (see {@link GlintShards#shader}), made {@link LenientVertices}.
 *
 * <p>{@link GlintVariants} glints also get a buffer of their own. A glint is drawn into at the same time as the thing
 * under it (the foil buffer helpers hand out both buffers at once), and a render type without a buffer of its own shares
 * the buffer source's one shared buffer, where handing out the second ends the first one's batch before its vertices
 * arrive. Vanilla's own per-type buffers live in a map some modpacks make read-only, so the variants' are kept apart from
 * it; their builders still sit with vanilla's started ones, so ending a batch works the same for them.
 */
@Mixin(MultiBufferSource.BufferSource.class)
public abstract class BufferSourceMixin {

    @Shadow
    @Final
    protected Map<RenderType, BufferBuilder> startedBuilders;

    @Shadow
    public abstract void endBatch(RenderType renderType);

    /** Variant glint render type -> its buffer memory, for this buffer source. Render thread only. */
    @Unique
    private final Map<RenderType, ByteBufferBuilder> lifeguard$variantBuffers = new IdentityHashMap<>();

    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
    private void lifeguard$variantBuffer(RenderType type, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!GlintVariants.isVariant(type))
            return;
        BufferBuilder builder = startedBuilders.get(type);
        if (builder == null) {
            ByteBufferBuilder memory = lifeguard$variantBuffers.computeIfAbsent(type, key -> new ByteBufferBuilder(key.bufferSize()));
            builder = new BufferBuilder(memory, type.mode(), type.format());
            startedBuilders.put(type, builder);
        }
        if (GlintShards.isFullVertexType(type))
            ((LenientVertices) builder).lifeguard$setLenient(true);
        cir.setReturnValue(builder);
    }

    @Inject(method = "getBuffer", at = @At("RETURN"))
    private void lifeguard$lenientGlintVertices(RenderType type, CallbackInfoReturnable<VertexConsumer> cir) {
        if (GlintShards.isFullVertexType(type) && cir.getReturnValue() instanceof LenientVertices lenient)
            lenient.lifeguard$setLenient(true);
    }

    /** Flushing everything: draw the variant glints too (after the rest, so what they lie over is already drawn). */
    @Inject(method = "endBatch()V", at = @At("RETURN"))
    private void lifeguard$endVariants(CallbackInfo ci) {
        for (RenderType type : lifeguard$variantBuffers.keySet())
            endBatch(type);
    }
}
