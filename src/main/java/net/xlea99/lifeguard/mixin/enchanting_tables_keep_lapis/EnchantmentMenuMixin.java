package net.xlea99.lifeguard.mixin.enchanting_tables_keep_lapis;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.xlea99.lifeguard.entry.enchanting_tables_keep_lapis.LapisMenu;
import net.xlea99.lifeguard.entry.enchanting_tables_keep_lapis.TableLapis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the enchanting menu where its lapis slot is filled (built), changed (any slot change, including lapis spent on
 * an enchant: vanilla calls slotsChanged after), and emptied (removed, BEFORE vanilla hands the slots back to the player).
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin implements LapisMenu {
    @Shadow
    @Final
    private Container enchantSlots;

    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Unique
    private boolean lifeguard$lapisLinked;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
        at = @At("TAIL"))
    private void lifeguard$fillKeptLapis(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        TableLapis.onOpen(this);
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void lifeguard$keepLapis(Container container, CallbackInfo ci) {
        TableLapis.onSlotsChanged(this);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void lifeguard$returnLapisToTable(Player player, CallbackInfo ci) {
        TableLapis.onClose(this);
    }

    @Override
    public ContainerLevelAccess lifeguard$access() {
        return this.access;
    }

    @Override
    public Container lifeguard$enchantSlots() {
        return this.enchantSlots;
    }

    @Override
    public boolean lifeguard$linked() {
        return this.lifeguard$lapisLinked;
    }

    @Override
    public void lifeguard$setLinked(boolean linked) {
        this.lifeguard$lapisLinked = linked;
    }
}
