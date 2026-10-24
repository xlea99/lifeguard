package net.xlea99.lifeguard.client.menu;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.xlea99.lifeguard.core.Taste;

/**
 * Taste as a small colored box with a letter in it, cooler to hotter: gray N, steel-blue L, bright-blue S, muted red DE,
 * red !. Every box is the same width so the names after them line up. Taste keeps to grays, blues and reds; class
 * badges run green to brown, so the two read apart.
 */
final class TasteGlyphs {
    static final int HEIGHT = 11;

    private TasteGlyphs() {
    }

    static int width(Font font) {
        return font.width("DE") + 4;
    }

    static void draw(GuiGraphics graphics, Font font, Taste taste, int x, int y) {
        int width = width(font);
        graphics.fill(x, y, x + width, y + HEIGHT, color(taste));
        String letter = letter(taste);
        graphics.drawString(font, letter, x + (width - font.width(letter) + 1) / 2, y + 2, 0xFFFFFFFF, false);
    }

    static String letter(Taste taste) {
        return switch (taste) {
            case NONE -> "N";
            case LIGHT -> "L";
            case STRONG -> "S";
            case DEEP_END -> "DE";
            case DANGEROUS -> "!";
        };
    }

    static int color(Taste taste) {
        return switch (taste) {
            case NONE -> 0xFF5E5E5E;
            case LIGHT -> 0xFF5A7896;
            case STRONG -> 0xFF2F80FF;
            case DEEP_END -> 0xFF9A5A5A;
            case DANGEROUS -> 0xFFC8302E;
        };
    }
}
