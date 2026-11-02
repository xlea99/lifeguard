package net.xlea99.lifeguard.mixin.pack_animal_chests_alexs_mobs;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.xlea99.lifeguard.entry.pack_animal_chests.ElephantChests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An elephant remembers the chest put on it and gives it back. See {@link ElephantChests}. */
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityElephant")
public abstract class EntityElephantMixin {
    @WrapMethod(method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")
    private InteractionResult lifeguard$notesHeldChest(Player player, InteractionHand hand, Operation<InteractionResult> original) {
        Item previous = ElephantChests.startClick(player, hand);
        try {
            return original.call(player, hand);
        } finally {
            ElephantChests.endClick(previous);
        }
    }

    @Inject(method = "setChested(Z)V", at = @At("TAIL"))
    private void lifeguard$remembersChest(boolean chested, CallbackInfo ci) {
        ElephantChests.chested((Entity) (Object) this, chested);
    }

    @ModifyArg(method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
        at = @At(value = "INVOKE",
            target = "Lcom/github/alexthe666/alexsmobs/entity/EntityElephant;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemLike lifeguard$shearsGiveChestBack(ItemLike item) {
        return ElephantChests.dropped((Entity) (Object) this, item);
    }

    @ModifyArg(method = "dropEquipment()V", at = @At(value = "INVOKE",
        target = "Lcom/github/alexthe666/alexsmobs/entity/EntityElephant;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemLike lifeguard$deathGivesChestBack(ItemLike item) {
        return ElephantChests.dropped((Entity) (Object) this, item);
    }
}
