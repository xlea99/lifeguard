package net.xlea99.lifeguard.mixin.core;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.xlea99.lifeguard.data.RecipeEdits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Where entries' recipe edits are made ({@link RecipeEdits}): on the recipe files the game is about to read, after
 * every mod's and datapack's are merged. Infrastructure, always applied; with no active entry editing recipes it hands
 * the files straight through.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, JsonElement> lifeguard$recipeEdits(Map<ResourceLocation, JsonElement> recipes) {
        return RecipeEdits.apply(recipes);
    }
}
