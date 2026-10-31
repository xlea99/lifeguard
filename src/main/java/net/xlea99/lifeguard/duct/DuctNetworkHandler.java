package net.xlea99.lifeguard.duct;

import com.teamabnormals.caverns_and_chasms.common.block.StorageDuctBlock;
import com.teamabnormals.caverns_and_chasms.common.block.entity.StorageDuctBlockEntity;
import com.teamabnormals.caverns_and_chasms.common.inventory.StorageDuctContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/**
 * What one Storage Duct hands to hoppers and pipes ({@link StorageDucts}): its whole network, rebuilt before any use
 * once {@link StorageDucts#stamp()} has moved. It stays the same object for the duct's life, because pipes keep the
 * handler they were given (a {@code BlockCapabilityCache}) and never ask again. A discarded duct or a sealed network
 * hands over nothing.
 *
 * <p>Rebuilding mid-insert is expected (the insert's comparator updates can load a chunk, which moves the stamp), and
 * the network can come back smaller, so every slot is bounds-checked against the network as it is now.
 */
public final class DuctNetworkHandler implements IItemHandlerModifiable {
    private static final IItemHandlerModifiable NOTHING = (IItemHandlerModifiable) EmptyItemHandler.INSTANCE;

    private final StorageDuctBlockEntity duct;
    private IItemHandlerModifiable network;
    private long stamp;

    public DuctNetworkHandler(StorageDuctBlockEntity duct) {
        this.duct = duct;
    }

    private IItemHandlerModifiable current() {
        long now = StorageDucts.stamp();
        if (network == null || stamp != now) {
            // The stamp from before the walk: one that moved during it (the walk loading chunks) rebuilds once more.
            stamp = now;
            network = build();
        }
        return network;
    }

    private IItemHandlerModifiable build() {
        Level level = duct.getLevel();
        if (level == null || duct.isRemoved() || StorageDucts.sealed(level, duct.getBlockPos(), duct.getBlockState()))
            return NOTHING;
        StorageDuctContainer run = StorageDuctBlock.getContainer(level, duct.getBlockPos(), StorageDuctBlock.DuctEnd.FIRST);
        return new InvWrapper(run == null ? duct : run);
    }

    @Override
    public int getSlots() {
        return current().getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        IItemHandlerModifiable now = current();
        return slot < now.getSlots() ? now.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        IItemHandlerModifiable now = current();
        return slot < now.getSlots() ? now.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        IItemHandlerModifiable now = current();
        return slot < now.getSlots() ? now.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        IItemHandlerModifiable now = current();
        return slot < now.getSlots() ? now.getSlotLimit(slot) : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        IItemHandlerModifiable now = current();
        return slot < now.getSlots() && now.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        IItemHandlerModifiable now = current();
        if (slot < now.getSlots())
            now.setStackInSlot(slot, stack);
    }
}
