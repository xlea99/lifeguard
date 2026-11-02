package net.xlea99.lifeguard.entry.enchanting_tables_keep_lapis;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The lapis an enchanting table keeps, and the one rule that makes it dupe-proof: the table's stored stack and the lapis
 * slot of every menu open on that table are always the same. Every change to a linked menu's slot is saved to the table
 * and copied to the other viewers in the same moment; a menu is linked when it is filled from the table, and stays linked
 * (saving on close) even if the switch flips while it's open. Server only: a client menu's access is NULL.
 */
public final class TableLapis {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Lifeguard.MODID);

    /** Saved with the table. Never holds an empty stack: empty means the attachment is removed. */
    private static final Supplier<AttachmentType<ItemStack>> LAPIS = ATTACHMENTS.register("table_lapis",
        () -> AttachmentType.builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).build());

    /** True while this class writes menu slots itself, so those writes don't save or mirror again. */
    private static boolean writing;

    private TableLapis() {
    }

    static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    /** A menu was just built: fill its lapis slot from its table and link it. */
    public static void onOpen(LapisMenu menu) {
        if (!EnchantingTablesKeepLapis.ENTRY.active())
            return;
        menu.lifeguard$access().execute((level, pos) -> {
            BlockEntity table = table(level, pos);
            if (table == null)
                return;
            menu.lifeguard$setLinked(true);
            ItemStack lapis = stored(table);
            if (!lapis.isEmpty())
                write(menu.lifeguard$enchantSlots(), lapis);
        });
    }

    /** A linked menu's slots changed (placed, taken, spent on an enchant): save the lapis and copy it to the others. */
    public static void onSlotsChanged(LapisMenu menu) {
        if (writing || !menu.lifeguard$linked())
            return;
        menu.lifeguard$access().execute((level, pos) -> {
            BlockEntity table = table(level, pos);
            if (table == null)
                return;
            ItemStack lapis = menu.lifeguard$enchantSlots().getItem(1);
            if (ItemStack.matches(lapis, stored(table)))
                return;
            store(table, lapis);
            mirror(level, pos, menu, lapis);
        });
    }

    /** A linked menu is closing: its lapis goes back into the table, never to the player. */
    public static void onClose(LapisMenu menu) {
        if (!menu.lifeguard$linked())
            return;
        menu.lifeguard$setLinked(false);
        menu.lifeguard$access().execute((level, pos) -> {
            Container slots = menu.lifeguard$enchantSlots();
            BlockEntity table = table(level, pos);
            // No table: it was broken, and dropStored already dropped the lapis this slot only mirrored.
            if (table != null)
                store(table, slots.getItem(1));
            write(slots, ItemStack.EMPTY);
        });
    }

    /** The table is being removed: drop what it keeps and empty every viewer's slot, so no close can hand back a copy. */
    public static void dropStored(Level level, BlockPos pos) {
        if (level.isClientSide())
            return;
        BlockEntity table = table(level, pos);
        if (table == null)
            return;
        ItemStack lapis = stored(table);
        if (lapis.isEmpty())
            return;
        store(table, ItemStack.EMPTY);
        mirror(level, pos, null, ItemStack.EMPTY);
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), lapis);
    }

    private static void mirror(Level level, BlockPos pos, @Nullable LapisMenu except, ItemStack lapis) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof LapisMenu other && other != except && other.lifeguard$linked()
                && other.lifeguard$access().evaluate((l, p) -> l == level && p.equals(pos)).orElse(false))
                write(other.lifeguard$enchantSlots(), lapis);
        }
    }

    private static void write(Container slots, ItemStack lapis) {
        writing = true;
        try {
            slots.setItem(1, lapis.copy());
        } finally {
            writing = false;
        }
    }

    @Nullable
    private static BlockEntity table(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table ? table : null;
    }

    private static ItemStack stored(BlockEntity table) {
        return table.hasData(LAPIS) ? table.getData(LAPIS) : ItemStack.EMPTY;
    }

    private static void store(BlockEntity table, ItemStack lapis) {
        if (lapis.isEmpty()) {
            if (table.hasData(LAPIS))
                table.removeData(LAPIS);
        } else {
            table.setData(LAPIS, lapis.copy());
        }
        table.setChanged();
    }
}
