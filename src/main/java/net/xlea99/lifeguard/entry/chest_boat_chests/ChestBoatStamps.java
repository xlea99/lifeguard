package net.xlea99.lifeguard.entry.chest_boat_chests;

import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.client.ChestIcons;

/**
 * Chest boat and chest raft icons in their chest's colors ({@link ChestIcons}). Every chest boat icon in Deep End,
 * vanilla's and every mod's, draws its chest on the same pixels, and every chest raft on another, taller set (checked
 * 2026-10-31, icon_stamp.py).
 */
final class ChestBoatStamps {
    /** The chest on a chest boat icon (16x16, {@code #} = chest), painted by the user over vanilla's oak chest boat. */
    private static final String[] BOAT_MASK = {
        "................",
        "......##........",
        "....#####.......",
        "..########......",
        "...########.....",
        "..#.###..#......",
        "..##.#....#.....",
        "..###.....#.....",
        "...###.#........",
        "....##.##.......",
        ".....#..........",
        "................",
        "................",
        "................",
        "................",
        "................"};
    /** The same over vanilla's bamboo chest raft, whose chest sits taller. */
    private static final String[] RAFT_MASK = {
        "................",
        "......##........",
        "....#####.......",
        "..########......",
        "...########.....",
        "..#.######......",
        "..##.#....#.....",
        "..###....##.....",
        "..####....#.....",
        "...###.#........",
        "....##.##.......",
        ".....#.#........",
        "................",
        "................",
        "................",
        "................"};

    private ChestBoatStamps() {
    }

    static void add() {
        ChestIcons.add(new ChestIcons.Stamp("boat", ResourceLocation.withDefaultNamespace("textures/item/oak_chest_boat.png"),
            BOAT_MASK, id -> id.getPath().endsWith("_chest_boat"), BoatChest::of, ChestBoatChests.ENTRY));
        ChestIcons.add(new ChestIcons.Stamp("raft", ResourceLocation.withDefaultNamespace("textures/item/bamboo_chest_raft.png"),
            RAFT_MASK, id -> id.getPath().endsWith("_chest_raft"), BoatChest::of, ChestBoatChests.ENTRY));
    }
}
