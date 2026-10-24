package net.xlea99.lifeguard.mixin.faucet_pouring_recipes;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.List;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetTarget;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.PouringRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the faucet pouring recipes in front of Supplementaries' own faucet targets. A faucet uses the first target that
 * takes the block below; Supplementaries' registration hook only adds to the end, where its built-ins would win.
 */
@Mixin(FaucetBehaviorsManager.class)
public abstract class FaucetBehaviorsManagerMixin {

    @ModifyReturnValue(method = "getTargetBlockInteractions", at = @At("RETURN"))
    private Iterable<FaucetTarget.BlState> lifeguard$recipesFirst(Iterable<FaucetTarget.BlState> builtIn) {
        return Iterables.concat(List.of(PouringRecipes.TARGET), builtIn);
    }
}
