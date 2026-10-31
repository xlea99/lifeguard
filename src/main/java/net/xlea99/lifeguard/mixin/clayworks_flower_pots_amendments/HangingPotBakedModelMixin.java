package net.xlea99.lifeguard.mixin.clayworks_flower_pots_amendments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.mehvahdjukaar.amendments.client.model.HangingPotBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.HangingPots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Draws a hung pot with its looks. Amendments draws the pot it hangs with that pot's own model (our flower pot wrapper)
 * but asks for its quads and particle without model data; the looks now go along. Only the pot's call changes; the
 * rope's is the second one.
 */
@Mixin(HangingPotBakedModel.class)
public abstract class HangingPotBakedModelMixin {

    @WrapOperation(method = "getBlockQuads",
        at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> lifeguard$potWithLooks(BakedModel model, BlockState pot, Direction side, RandomSource rand,
                                                  Operation<List<BakedQuad>> original,
                                                  @Local(argsOnly = true) RenderType renderType,
                                                  @Local(argsOnly = true) ExtraModelData data) {
        ModelData looks = HangingPots.forPotModel(data);
        return looks == null ? original.call(model, pot, side, rand) : model.getQuads(pot, side, rand, looks, renderType);
    }

    @WrapOperation(method = "getBlockParticle",
        at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/resources/model/BakedModel;getParticleIcon()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite lifeguard$particleWithLooks(BakedModel model, Operation<TextureAtlasSprite> original,
                                                          @Local(argsOnly = true) ExtraModelData data) {
        ModelData looks = HangingPots.forPotModel(data);
        return looks == null ? original.call(model) : model.getParticleIcon(looks);
    }
}
