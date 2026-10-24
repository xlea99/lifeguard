package net.xlea99.lifeguard.client.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.xlea99.lifeguard.core.EntryClass;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Taste;

/**
 * The left-hand column: search at the top, chips for taste, class, side, state and mod in the middle, and a footer
 * pinned to the bottom (the count, "include missing-mod entries", Clear filters) that stays put while the chips scroll.
 * Every change re-filters the list at once.
 */
final class FilterPane {
    private static final int PAD = 5;
    private static final int CHIP_HEIGHT = 12;
    private static final int CHIP_GAP = 3;
    private static final int SEARCH_HEIGHT = 16;
    private static final int BOX = 9;
    private static final int LINE = 10;

    private record Chip(int x, int y, int width, int height, Runnable action, Component tooltip, boolean scrolls) {
    }

    private final LifeguardScreen screen;
    private final Filters filters;
    private final List<Chip> chips = new ArrayList<>();
    private EditBox search;
    private int left;
    private int top;
    private int width;
    private int height;
    private int scroll;
    private int contentHeight;
    private int footerTop;

    // Where the flow layout is while rendering.
    private int cursorX;
    private int cursorY;

    FilterPane(LifeguardScreen screen, Filters filters) {
        this.screen = screen;
        this.filters = filters;
    }

    /** Places the column and its search box. Called on every (re)layout of the screen. */
    void layout(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        search = new EditBox(screen.font(), left + PAD, top + PAD, width - 2 * PAD, SEARCH_HEIGHT,
            Component.translatable("lifeguard.menu.filters.search"));
        search.setHint(Component.translatable("lifeguard.menu.filters.search_hint"));
        search.setValue(filters.search);
        search.setResponder(text -> {
            filters.search = text;
            screen.filtersChanged();
        });
        screen.addFilterWidget(search);
    }

    EditBox search() {
        return search;
    }

    private int chipsTop() {
        return top + PAD + SEARCH_HEIGHT + 4;
    }

    void renderBackdrop(GuiGraphics graphics) {
        graphics.fill(left, top, left + width, top + height, 0x60000000);
        graphics.renderOutline(left, top, width, height, 0x30FFFFFF);
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = screen.font();
        chips.clear();
        List<FormattedCharSequence> checkboxLines = font.split(checkboxLabel(), width - 2 * PAD - BOX - 4);
        // Footer, bottom up: Clear filters, the checkbox, the count.
        footerTop = top + height - PAD - CHIP_HEIGHT - 4 - checkboxLines.size() * LINE - 4 - LINE;

        renderChips(graphics, font, mouseX, mouseY);
        renderFooter(graphics, font, checkboxLines, mouseX, mouseY);
    }

    // --- the chips, which scroll --------------------------------------------------------------------------------------

    private void renderChips(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        graphics.enableScissor(left, chipsTop(), left + width, footerTop - 3);
        cursorY = chipsTop() - scroll;
        cursorX = left + PAD;

        section(graphics, font, "lifeguard.menu.filters.taste");
        for (Taste taste : Taste.values())
            tasteChip(graphics, font, taste, mouseX, mouseY);
        section(graphics, font, "lifeguard.menu.filters.class");
        for (EntryClass kind : EntryClass.values())
            chip(graphics, font, Component.translatable("lifeguard.class." + kind.id() + ".plural"),
                filters.classes.contains(kind), EntryList.classColor(kind), () -> toggle(filters.classes, kind), null,
                mouseX, mouseY);
        section(graphics, font, "lifeguard.menu.filters.side");
        for (Side side : Side.values())
            chip(graphics, font, Component.translatable("lifeguard.side." + side.id()), filters.sides.contains(side),
                0xFF555555, () -> toggle(filters.sides, side), null, mouseX, mouseY);
        section(graphics, font, "lifeguard.menu.filters.state");
        for (Filters.State state : Filters.State.values())
            chip(graphics, font, Component.translatable("lifeguard.menu.filters.state." + state.name().toLowerCase(Locale.ROOT)),
                filters.states.contains(state), 0xFF555555, () -> toggle(filters.states, state), null, mouseX, mouseY);
        section(graphics, font, "lifeguard.menu.filters.mod");
        for (String modId : screen.model().referencedMods()) {
            // A mod that isn't installed reads dimmer, and says so on hover.
            boolean installed = screen.model().modInstalled(modId);
            Component name = Component.literal(MenuModel.modName(modId))
                .withColor(installed ? 0xFFA8A8A8 : 0xFF6A6A6A)
                .append(Component.literal(" " + screen.model().entriesRequiring(modId)).withColor(0xFF707070));
            chip(graphics, font, name, filters.mods.contains(modId), 0xFF555555, () -> toggle(filters.mods, modId),
                installed ? null : Component.translatable("lifeguard.menu.filters.not_installed"), mouseX, mouseY);
        }
        newLine();
        contentHeight = cursorY + scroll - chipsTop();
        // Content that shrank (or a column that grew) mustn't leave the chips scrolled past their end.
        scroll = Math.max(0, Math.min(scroll, contentHeight - (footerTop - 3 - chipsTop())));
        graphics.disableScissor();
    }

    private void section(GuiGraphics graphics, Font font, String key) {
        newLine();
        cursorY += 3;
        graphics.drawString(font, Component.translatable(key), left + PAD, cursorY, 0xFFE0C060, false);
        cursorY += 11;
        cursorX = left + PAD;
    }

    private void newLine() {
        if (cursorX != left + PAD) {
            cursorX = left + PAD;
            cursorY += CHIP_HEIGHT + CHIP_GAP;
        }
    }

    /** A taste chip looks like its glyph: filled in its color when ticked, the letter in its color when not. */
    private void tasteChip(GuiGraphics graphics, Font font, Taste taste, int mouseX, int mouseY) {
        int chipWidth = TasteGlyphs.width(font) + 2;
        place(chipWidth);
        boolean on = filters.tastes.contains(taste);
        int color = TasteGlyphs.color(taste);
        graphics.fill(cursorX, cursorY, cursorX + chipWidth, cursorY + CHIP_HEIGHT, on ? color : 0xFF262626);
        if (!on)
            graphics.renderOutline(cursorX, cursorY, chipWidth, CHIP_HEIGHT, color);
        String letter = TasteGlyphs.letter(taste);
        graphics.drawString(font, letter, cursorX + (chipWidth - font.width(letter) + 1) / 2, cursorY + 2,
            on ? 0xFFFFFFFF : color, false);
        record(chipWidth, () -> toggle(filters.tastes, taste),
            Component.translatable("lifeguard.taste." + taste.id() + ".name"), mouseX, mouseY);
    }

    private void chip(GuiGraphics graphics, Font font, Component label, boolean on, int onColor, Runnable toggle,
                      Component tooltip, int mouseX, int mouseY) {
        int chipWidth = Math.min(font.width(label) + 6, width - 2 * PAD);
        place(chipWidth);
        drawChip(graphics, font, label, cursorX, cursorY, chipWidth, on, onColor, true);
        record(chipWidth, toggle, tooltip, mouseX, mouseY);
    }

    private void drawChip(GuiGraphics graphics, Font font, Component label, int x, int y, int chipWidth, boolean on,
                          int onColor, boolean enabled) {
        graphics.fill(x, y, x + chipWidth, y + CHIP_HEIGHT, on ? onColor : 0xFF262626);
        graphics.renderOutline(x, y, chipWidth, CHIP_HEIGHT, on ? 0x60FFFFFF : 0xFF444444);
        // Unticked, the label keeps its own colors (a dimmed uninstalled mod, a grey count); ticked, it's plain white.
        if (on)
            graphics.drawString(font, font.substrByWidth(label, chipWidth - 6).getString(), x + 3, y + 2, 0xFFFFFFFF, false);
        else
            graphics.drawString(font, Language.getInstance().getVisualOrder(font.substrByWidth(label, chipWidth - 6)),
                x + 3, y + 2, enabled ? 0xFFA8A8A8 : 0xFF5A5A5A, false);
    }

    /** Moves to the next line when the chip doesn't fit on this one. */
    private void place(int chipWidth) {
        if (cursorX != left + PAD && cursorX + chipWidth > left + width - PAD) {
            cursorX = left + PAD;
            cursorY += CHIP_HEIGHT + CHIP_GAP;
        }
    }

    private void record(int chipWidth, Runnable toggle, Component tooltip, int mouseX, int mouseY) {
        Chip chip = new Chip(cursorX, cursorY, chipWidth, CHIP_HEIGHT, toggle, tooltip, true);
        chips.add(chip);
        if (tooltip != null && over(chip, mouseX, mouseY))
            screen.setTooltipForNextRenderPass(tooltip);
        cursorX += chipWidth + CHIP_GAP;
    }

    private <T> void toggle(Set<T> set, T value) {
        Filters.toggle(set, value);
        screen.filtersChanged();
    }

    // --- the footer, which stays put ----------------------------------------------------------------------------------

    private Component checkboxLabel() {
        int hidden = filters.includeMissing ? 0 : screen.model().hiddenByMissingMods(filters);
        Component label = Component.translatable("lifeguard.menu.filters.include_missing");
        return hidden == 0 ? label : Component.empty().append(label)
            .append(Component.literal(" (+" + hidden + ")").withColor(0xFF808080));
    }

    private void renderFooter(GuiGraphics graphics, Font font, List<FormattedCharSequence> checkboxLines, int mouseX,
                              int mouseY) {
        graphics.fill(left + PAD, footerTop - 3, left + width - PAD, footerTop - 2, 0x30FFFFFF);
        int x = left + PAD;
        int y = footerTop;

        graphics.drawString(font, Component.translatable("lifeguard.menu.filters.showing",
            screen.model().matching(filters).size(), screen.model().totalEntries()), x, y, 0xFF909090, false);
        y += LINE + 4;

        // The checkbox: a box, ticked or not, and its label wrapped beside it. The whole thing is one click target.
        graphics.fill(x, y, x + BOX, y + BOX, 0xFF262626);
        graphics.renderOutline(x, y, BOX, BOX, 0xFF8A8A8A);
        if (filters.includeMissing)
            graphics.fill(x + 2, y + 2, x + BOX - 2, y + BOX - 2, 0xFFE0E0E0);
        for (int i = 0; i < checkboxLines.size(); i++)
            graphics.drawString(font, checkboxLines.get(i), x + BOX + 4, y + 1 + i * LINE, 0xFFC8C8C8, false);
        int boxHeight = Math.max(BOX, checkboxLines.size() * LINE);
        chips.add(new Chip(x, y, width - 2 * PAD, boxHeight, () -> {
            filters.includeMissing = !filters.includeMissing;
            screen.filtersChanged();
        }, null, false));
        y += boxHeight + 4;

        // Clear filters: always there, inactive with nothing to clear.
        Component clear = Component.translatable("lifeguard.menu.filters.clear");
        int clearWidth = Math.min(font.width(clear) + 6, width - 2 * PAD);
        boolean active = filters.any();
        drawChip(graphics, font, clear, x, y, clearWidth, false, 0, active);
        if (active)
            chips.add(new Chip(x, y, clearWidth, CHIP_HEIGHT, () -> {
                filters.clear();
                search.setValue("");
                screen.filtersChanged();
            }, null, false));
    }

    // --- input -------------------------------------------------------------------------------------------------------

    private boolean over(Chip chip, double mouseX, double mouseY) {
        boolean inside = mouseX >= chip.x() && mouseX < chip.x() + chip.width() && mouseY >= chip.y()
            && mouseY < chip.y() + chip.height();
        // A scrolling chip only counts inside the scroll area, where it's visible.
        return inside && (!chip.scrolls() || mouseY >= chipsTop() && mouseY < footerTop - 3);
    }

    boolean click(double mouseX, double mouseY) {
        for (Chip chip : chips) {
            if (over(chip, mouseX, mouseY)) {
                chip.action().run();
                return true;
            }
        }
        return false;
    }

    /** Scrolls the chips when the column is too short for them. */
    boolean scroll(double mouseX, double mouseY, double amount) {
        if (mouseX < left || mouseX >= left + width || mouseY < top || mouseY >= top + height)
            return false;
        int visible = footerTop - 3 - chipsTop();
        int max = Math.max(0, contentHeight - visible);
        scroll = Math.max(0, Math.min(max, scroll - (int) (amount * 12)));
        return true;
    }
}
