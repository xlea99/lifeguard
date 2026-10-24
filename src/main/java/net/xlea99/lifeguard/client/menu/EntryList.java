package net.xlea99.lifeguard.client.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.xlea99.lifeguard.core.EntryClass;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Switch;

/**
 * The entries as a compact table: one solid, striped 12-pixel row per entry under a pinned column header. Columns:
 * taste glyph, name (indented by tree depth, collapsible), class, side, switch. Mod headings sit between groups.
 *
 * <p>Rows hold no widgets: a click is read by where it lands. On the arrow it collapses; on the switch it steps the
 * switch (left click forward, right click back); anywhere else it selects the entry for the details pane.
 *
 * <p>Our {@code core.Entry} is written out in full here: inside a selection list, plain {@code Entry} means the list's
 * own row type.
 */
final class EntryList extends ContainerObjectSelectionList<EntryList.Row> {
    static final int ROW_HEIGHT = 12;
    static final int HEADER_HEIGHT = 12;
    private static final int INDENT = 8;
    private static final int SWITCH_WIDTH = 34;
    private static final int SIDE_WIDTH = 10;
    private static final int CLASS_WIDTH = 22;
    private static final int CELL_GAP = 3;

    private static final int PANEL = 0xFF151515;
    private static final int STRIPE_EVEN = 0xFF1E1E1E;
    private static final int STRIPE_ODD = 0xFF242424;
    private static final int HOVER = 0xFF2E2E2E;
    private static final int SELECTED = 0xFF34485C;
    private static final int HEADING = 0xFF2B2516;
    private static final int RULE = 0xFF3A3A3A;

    private final LifeguardScreen screen;

    EntryList(Minecraft minecraft, int width, int height, int top, LifeguardScreen screen) {
        super(minecraft, width, height, top, ROW_HEIGHT);
        this.screen = screen;
    }

    void show(List<MenuModel.Line> lines) {
        List<Row> rows = new ArrayList<>();
        int stripe = 0;
        for (MenuModel.Line line : lines) {
            if (line.isHeading()) {
                rows.add(new Heading(line));
                stripe = 0;
            } else {
                rows.add(new EntryRow(line.entry(), line.depth(), line.context(), stripe++ % 2 == 1));
            }
        }
        replaceEntries(rows);
        // A shorter list can leave the old scroll position past its end, showing nothing: clamp it back into range.
        setScrollAmount(getScrollAmount());
    }

    /**
     * A right click steps a switch backwards. Vanilla only passes left clicks into a list's rows, so the screen hands
     * right clicks here instead.
     */
    boolean rightClick(double mouseX, double mouseY) {
        if (getEntryAtPosition(mouseX, mouseY) instanceof EntryRow row && row.overSwitch(mouseX)) {
            screen.cycle(row.entry, false);
            return true;
        }
        return false;
    }

    /** Scrolls so the entry's row is in view. */
    void reveal(net.xlea99.lifeguard.core.Entry entry) {
        for (Row row : children())
            if (row instanceof EntryRow entryRow && entryRow.entry == entry) {
                ensureVisible(row);
                return;
            }
    }

    /**
     * Copper aging in reverse: fresh green features, sage tweaks, copper compat, brown fixes. Tweaks were verdigris,
     * pulled warmer so nothing in a class badge leans toward the blues taste uses.
     */
    static int classColor(EntryClass kind) {
        return switch (kind) {
            case FEATURE -> 0xFF2E9E3E;
            case TWEAK -> 0xFF78A05A;
            case COMPAT -> 0xFFC0703A;
            case FIX -> 0xFF7A5232;
        };
    }

    // --- geometry -----------------------------------------------------------------------------------------------------

    /** Rows stay centered (vanilla's hit-testing assumes it) and stop short of the scrollbar. */
    @Override
    public int getRowWidth() {
        return getWidth() - 16;
    }

    @Override
    protected int getScrollbarPosition() {
        return getX() + getWidth() - 6;
    }

    private int switchLeft(int rowLeft, int rowWidth) {
        return rowLeft + rowWidth - SWITCH_WIDTH;
    }

    private int sideLeft(int rowLeft, int rowWidth) {
        return switchLeft(rowLeft, rowWidth) - CELL_GAP - SIDE_WIDTH;
    }

    private int classLeft(int rowLeft, int rowWidth) {
        return sideLeft(rowLeft, rowWidth) - CELL_GAP - CLASS_WIDTH;
    }

    // --- the panel and the header -------------------------------------------------------------------------------------

    /** A solid panel instead of vanilla's translucent list backdrop: the table should feel fixed, not floating. */
    @Override
    protected void renderListBackground(GuiGraphics graphics) {
        graphics.fill(getX(), getY(), getRight(), getBottom(), PANEL);
    }

    @Override
    protected void renderListSeparators(GuiGraphics graphics) {
        graphics.fill(getX(), getBottom() - 1, getRight(), getBottom(), RULE);
    }

    /** The pinned column header, drawn by the screen just above the rows. */
    void renderHeader(GuiGraphics graphics, int top, int mouseX, int mouseY) {
        Font font = minecraft.font;
        int left = getRowLeft();
        int width = getRowWidth();
        graphics.fill(getX(), top, getRight(), top + HEADER_HEIGHT, 0xFF101010);
        graphics.fill(getX(), top + HEADER_HEIGHT - 1, getRight(), top + HEADER_HEIGHT, RULE);
        int y = top + 2;
        int color = 0xFF8A8A8A;
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.entry"), left + TasteGlyphs.width(font) + 4, y, color, false);
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.class"), classLeft(left, width), y, color, false);
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.side"), sideLeft(left, width) + 2, y, color, false);
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.switch"), switchLeft(left, width) + 2, y, color, false);
    }

    // --- rows ---------------------------------------------------------------------------------------------------------

    abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    /** A mod's heading band: its name and how many entries under it match. Dimmed when the mod isn't installed. */
    final class Heading extends Row {
        private final Component text;
        private final int color;

        Heading(MenuModel.Line line) {
            String mod = line.headingMod();
            boolean installed = mod.equals(MenuModel.MINECRAFT) || screen.model().modInstalled(mod);
            MutableComponent name = Component.literal(MenuModel.modName(mod))
                .append(Component.literal("  " + line.count()).withColor(0xFF7A7A7A));
            if (!installed)
                name.append(Component.translatable("lifeguard.menu.group.not_installed").withColor(0xFF7A7A7A));
            text = name;
            color = installed ? 0xFFE0C060 : 0xFF8A7A50;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                           int mouseY, boolean hovering, float partialTick) {
            graphics.fill(getX(), top, getX() + getWidth() - 7, top + ROW_HEIGHT, HEADING);
            graphics.drawString(minecraft.font, text, left + 2, top + 2, color, false);
        }
    }

    final class EntryRow extends Row {
        private final net.xlea99.lifeguard.core.Entry entry;
        private final int depth;
        private final boolean context;
        private final boolean odd;
        private int arrowLeft;
        private int switchX;

        /** A context row is an ancestor shown only because something under it matches the filters; it's greyed. */
        EntryRow(net.xlea99.lifeguard.core.Entry entry, int depth, boolean context, boolean odd) {
            this.entry = entry;
            this.depth = depth;
            this.context = context;
            this.odd = odd;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                           int mouseY, boolean hovering, float partialTick) {
            MenuModel model = screen.model();
            Font font = minecraft.font;
            boolean selected = screen.selected() == entry;
            // Vanilla hands rows a height 4px short of their slot; the whole slot is painted so the stripes touch.
            height = ROW_HEIGHT;
            graphics.fill(getX(), top, getX() + getWidth() - 7, top + height,
                selected ? SELECTED : hovering ? HOVER : odd ? STRIPE_ODD : STRIPE_EVEN);
            int ripple = screen.rippleColor(entry);
            if (ripple != 0)
                graphics.fill(getX(), top, getX() + getWidth() - 7, top + height, ripple);
            int textY = top + 2;

            // Taste glyph, then the name column: tree indent, collapse arrow, the name, and an aside if it fits.
            TasteGlyphs.draw(graphics, font, entry.taste(), left, top + (height - TasteGlyphs.HEIGHT) / 2 + 1);
            int nameLeft = left + TasteGlyphs.width(font) + 4 + depth * INDENT;
            arrowLeft = nameLeft;
            if (!entry.children().isEmpty())
                graphics.drawString(font, model.collapsed(entry) ? "▸" : "▾", nameLeft, textY, 0xFF9A9A9A, false);
            int textX = nameLeft + 7;
            int classX = classLeft(left, width);
            int nameRight = classX - 4;

            boolean missing = !entry.missingMods().isEmpty();
            boolean active = entry.activeUnder(model.draft());
            int nameColor = context ? 0xFF555555 : missing ? 0xFF6A6A6A : active ? 0xFFEDEDED : 0xFF9A9A9A;
            Component name = Component.translatable(entry.nameKey());
            graphics.drawString(font, font.substrByWidth(name, nameRight - textX).getString(), textX, textY, nameColor, false);
            Component aside = aside(missing);
            int afterName = textX + font.width(name) + 6;
            if (aside != null && afterName + font.width(aside) < nameRight)
                graphics.drawString(font, aside, afterName, textY, 0xFF666666, false);

            // Class and side cells.
            String classCode = Component.translatable("lifeguard.class." + entry.kind().id() + ".code").getString();
            graphics.fill(classX, top + 1, classX + CLASS_WIDTH, top + height - 1, classColor(entry.kind()));
            graphics.drawString(font, classCode, classX + (CLASS_WIDTH - font.width(classCode) + 1) / 2, textY, 0xFFFFFFFF, false);
            int sideX = sideLeft(left, width);
            boolean serverLocked = entry.side() == Side.SERVER && model.following();
            String sideCode = entry.side() == Side.SERVER ? "S" : "C";
            graphics.fill(sideX, top + 1, sideX + SIDE_WIDTH, top + height - 1, serverLocked ? 0xFF5A4220 : 0xFF333333);
            graphics.drawString(font, sideCode, sideX + (SIDE_WIDTH - font.width(sideCode) + 1) / 2, textY, 0xFFC0C0C0, false);

            // The switch.
            switchX = switchLeft(left, width);
            renderSwitch(graphics, font, model, top, height, mouseX, mouseY);

            if (!hovering)
                return;
            if (mouseX >= left && mouseX < left + TasteGlyphs.width(font))
                screen.setTooltipForNextRenderPass(Component.translatable("lifeguard.taste." + entry.taste().id()));
            else if (mouseX >= classX && mouseX < classX + CLASS_WIDTH)
                screen.setTooltipForNextRenderPass(Component.translatable("lifeguard.class." + entry.kind().id()));
            else if (mouseX >= sideX && mouseX < sideX + SIDE_WIDTH)
                screen.setTooltipForNextRenderPass(Component.translatable(serverLocked
                    ? "lifeguard.menu.locked.server" : "lifeguard.side." + entry.side().id()));
            else if (mouseX >= switchX && model.locked(entry))
                screen.setTooltipForNextRenderPass(lockReason(model));
            else if (mouseX >= switchX)
                screen.setTooltipForNextRenderPass(Component.translatable("lifeguard.menu.switch.tooltip"));
        }

        /** Auto in dim gray (saying what the profile makes it), On in green, Off in red; dimmer still when locked. */
        private void renderSwitch(GuiGraphics graphics, Font font, MenuModel model, int top, int height, int mouseX,
                                  int mouseY) {
            Switch setting = model.draft().setting(entry);
            boolean locked = model.locked(entry);
            boolean hover = !locked && mouseX >= switchX && mouseX < switchX + SWITCH_WIDTH && mouseY >= top
                && mouseY < top + height;
            int fill = switch (setting) {
                case AUTO -> hover ? 0xFF4A4A4A : 0xFF383838;
                case ON -> hover ? 0xFF3E9E4E : 0xFF2E7E3E;
                case OFF -> hover ? 0xFFB04848 : 0xFF8E3434;
            };
            String label = switch (setting) {
                case AUTO -> Component.translatable(entry.switchedOnUnder(model.draft())
                    ? "lifeguard.menu.switch.auto_on" : "lifeguard.menu.switch.auto_off").getString();
                case ON -> Component.translatable("lifeguard.menu.switch.on").getString();
                case OFF -> Component.translatable("lifeguard.menu.switch.off").getString();
            };
            graphics.fill(switchX, top + 1, switchX + SWITCH_WIDTH, top + height - 1, locked ? 0xFF2A2A2A : fill);
            int textColor = locked ? 0xFF6A6A6A : setting == Switch.AUTO ? 0xFFB8B8B8 : 0xFFFFFFFF;
            graphics.drawString(font, label, switchX + (SWITCH_WIDTH - font.width(label) + 1) / 2, top + 2, textColor, false);
        }

        private Component aside(boolean missing) {
            if (missing) {
                MutableComponent names = Component.empty();
                List<String> mods = entry.missingMods();
                for (int i = 0; i < mods.size(); i++) {
                    if (i > 0)
                        names.append(", ");
                    names.append(Component.literal(MenuModel.modName(mods.get(i))));
                }
                return Component.translatable("lifeguard.menu.needs", names);
            }
            return entry.isPlanned() ? Component.translatable("lifeguard.menu.planned") : null;
        }

        boolean overSwitch(double mouseX) {
            return mouseX >= switchX && mouseX < switchX + SWITCH_WIDTH;
        }

        private Component lockReason(MenuModel model) {
            return model.brokenFile(entry.side())
                .<Component>map(file -> Component.translatable("lifeguard.menu.locked.broken", file))
                .orElseGet(() -> Component.translatable(entry.side() == Side.SERVER
                    ? "lifeguard.menu.locked.server" : "lifeguard.menu.locked.client"));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && !entry.children().isEmpty() && mouseX >= arrowLeft && mouseX < arrowLeft + 7) {
                screen.model().toggleCollapsed(entry);
                screen.rebuildRows();
                return true;
            }
            if (overSwitch(mouseX)) {
                screen.cycle(entry, true);
                return true;
            }
            if (button == 0) {
                screen.select(entry, false);
                return true;
            }
            return false;
        }
    }
}
