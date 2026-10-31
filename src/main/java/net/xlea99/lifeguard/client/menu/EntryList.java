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
 * taste glyph and name (indented together by tree depth under drawn guides, collapsible), class, side, switch. Mod
 * headings sit between groups and collapse them.
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
    private static final int INDENT = 10;
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
    private static final int HEADING_HOVER = 0xFF372F1C;
    private static final int RULE = 0xFF3A3A3A;
    private static final int GUIDE = 0xFF505050;

    private final LifeguardScreen screen;

    EntryList(Minecraft minecraft, int width, int height, int top, LifeguardScreen screen) {
        super(minecraft, width, height, top, ROW_HEIGHT);
        this.screen = screen;
    }

    void show(List<MenuModel.Line> lines) {
        List<Row> rows = new ArrayList<>();
        int stripe = 0;
        for (int i = 0; i < lines.size(); i++) {
            MenuModel.Line line = lines.get(i);
            if (line.isHeading()) {
                rows.add(new Heading(line));
                stripe = 0;
            } else {
                rows.add(new EntryRow(line.entry(), line.depth(), line.context(), stripe++ % 2 == 1, guides(lines, i)));
            }
        }
        replaceEntries(rows);
        // A shorter list can leave the old scroll position past its end, showing nothing: clamp it back into range.
        setScrollAmount(getScrollAmount());
    }

    /**
     * Which tree guides run on below a row: for each level from 1 to its depth, whether a later row sits at that level
     * before the tree climbs back above it. A guide that runs on is a full-height line; one that doesn't ends here.
     */
    private static boolean[] guides(List<MenuModel.Line> lines, int at) {
        int depth = lines.get(at).depth();
        boolean[] runsOn = new boolean[depth + 1];
        for (int level = 1; level <= depth; level++)
            for (int j = at + 1; j < lines.size(); j++) {
                MenuModel.Line next = lines.get(j);
                if (next.isHeading() || next.depth() < level)
                    break;
                if (next.depth() == level) {
                    runsOn[level] = true;
                    break;
                }
            }
        return runsOn;
    }

    /** Vanilla's hit area starts a little right of the glyph column; a click left of it still counts for its row. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= getX() && mouseX < getRowLeft())
            mouseX = getRowLeft();
        return super.mouseClicked(mouseX, mouseY, button);
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

    /**
     * Where a root's taste glyph sits, hugging the panel's left edge instead of vanilla's centered row inset: the room
     * goes to the tree indent, which moves glyph and name together. Rows themselves stay centered, so only drawing moves.
     */
    private int glyphLeft() {
        return getX() + 3;
    }

    private boolean over(int top, int mouseX, int mouseY) {
        return isMouseOver(mouseX, mouseY) && mouseX >= getX() && mouseX < getX() + getWidth() - 7 && mouseY >= top
            && mouseY < top + ROW_HEIGHT;
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
        Component entryTitle = Component.translatable("lifeguard.menu.col.entry");
        graphics.drawString(font, entryTitle, glyphLeft(), y, color, false);
        // Expand all and collapse all, after the title: they act on the mod groups below.
        boolean usable = !screen.collapseIgnored();
        expandAllLeft = glyphLeft() + font.width(entryTitle) + 8;
        collapseAllLeft = expandAllLeft + font.width(EXPAND_ALL) + 6;
        headerTop = top;
        drawHeaderButton(graphics, EXPAND_ALL, expandAllLeft, y, usable, mouseX, mouseY, "lifeguard.menu.expand_all");
        drawHeaderButton(graphics, COLLAPSE_ALL, collapseAllLeft, y, usable, mouseX, mouseY, "lifeguard.menu.collapse_all");
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.class"), classLeft(left, width), y, color, false);
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.side"), sideLeft(left, width) + 2, y, color, false);
        graphics.drawString(font, Component.translatable("lifeguard.menu.col.switch"), switchLeft(left, width) + 2, y, color, false);
    }

    private static final String EXPAND_ALL = "▾▾";
    private static final String COLLAPSE_ALL = "▸▸";
    private int expandAllLeft = -1;
    private int collapseAllLeft = -1;
    private int headerTop = -1;

    private void drawHeaderButton(GuiGraphics graphics, String glyph, int x, int y, boolean usable, int mouseX, int mouseY,
                                  String tooltipKey) {
        boolean hover = usable && overHeaderButton(glyph, x, mouseX, mouseY);
        graphics.drawString(minecraft.font, glyph, x, y, !usable ? 0xFF4A4A4A : hover ? 0xFFE0C060 : 0xFFB09A60, false);
        if (hover)
            screen.setTooltipForNextRenderPass(Component.translatable(tooltipKey));
    }

    private boolean overHeaderButton(String glyph, int x, double mouseX, double mouseY) {
        return x >= 0 && mouseX >= x - 2 && mouseX < x + minecraft.font.width(glyph) + 2 && mouseY >= headerTop
            && mouseY < headerTop + HEADER_HEIGHT;
    }

    /** A click on the pinned header's expand all / collapse all; true if it was one. */
    boolean headerClick(double mouseX, double mouseY) {
        if (screen.collapseIgnored())
            return false;
        if (overHeaderButton(EXPAND_ALL, expandAllLeft, mouseX, mouseY))
            screen.model().expandAllMods();
        else if (overHeaderButton(COLLAPSE_ALL, collapseAllLeft, mouseX, mouseY))
            screen.model().collapseAllMods();
        else
            return false;
        screen.rebuildRows();
        return true;
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

    /**
     * A mod's heading band: its name and how many entries under it match. Dimmed when the mod isn't installed. A click
     * anywhere on it collapses or opens the whole group.
     */
    final class Heading extends Row {
        private final String mod;
        private final Component text;
        private final int color;

        Heading(MenuModel.Line line) {
            mod = line.headingMod();
            boolean installed = mod.equals(net.xlea99.lifeguard.core.Entry.MINECRAFT)
                || mod.equals(net.xlea99.lifeguard.core.Entry.LIFEGUARD)
                || screen.model().modInstalled(mod);
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
            boolean collapsible = !screen.collapseIgnored();
            graphics.fill(getX(), top, getX() + getWidth() - 7, top + ROW_HEIGHT,
                collapsible && over(top, mouseX, mouseY) ? HEADING_HOVER : HEADING);
            boolean closed = collapsible && screen.model().modCollapsed(mod);
            graphics.drawString(minecraft.font, closed ? "▸" : "▾", glyphLeft() + 1, top + 2,
                collapsible ? 0xFFB09A60 : 0xFF6A6050, false);
            graphics.drawString(minecraft.font, text, glyphLeft() + 9, top + 2, color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || screen.collapseIgnored())
                return false;
            screen.model().toggleModCollapsed(mod);
            screen.rebuildRows();
            return true;
        }
    }

    final class EntryRow extends Row {
        private final net.xlea99.lifeguard.core.Entry entry;
        private final int depth;
        private final boolean context;
        private final boolean odd;
        private final boolean[] guides;
        private int arrowLeft;
        private int switchX;

        /**
         * A context row is an ancestor shown only because something under it matches the filters; it's greyed. Guides
         * say which tree lines run on below this row, by level (see {@link EntryList#guides}).
         */
        EntryRow(net.xlea99.lifeguard.core.Entry entry, int depth, boolean context, boolean odd, boolean[] guides) {
            this.entry = entry;
            this.depth = depth;
            this.context = context;
            this.odd = odd;
            this.guides = guides;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                           int mouseY, boolean hovering, float partialTick) {
            MenuModel model = screen.model();
            Font font = minecraft.font;
            boolean selected = screen.selected() == entry;
            // Vanilla's hover starts a little right of the glyphs; ours covers the whole painted row.
            hovering = over(top, mouseX, mouseY);
            // Vanilla hands rows a height 4px short of their slot; the whole slot is painted so the stripes touch.
            height = ROW_HEIGHT;
            graphics.fill(getX(), top, getX() + getWidth() - 7, top + height,
                selected ? SELECTED : hovering ? HOVER : odd ? STRIPE_ODD : STRIPE_EVEN);
            int ripple = screen.rippleColor(entry);
            if (ripple != 0)
                graphics.fill(getX(), top, getX() + getWidth() - 7, top + height, ripple);
            int textY = top + 2;

            // Tree guides, then the taste glyph indented by depth, the collapse arrow, the name, and an aside if it fits.
            int base = glyphLeft();
            int glyphX = base + depth * INDENT;
            renderGuides(graphics, base, glyphX, top);
            TasteGlyphs.draw(graphics, font, entry.taste(), glyphX, top + (height - TasteGlyphs.HEIGHT) / 2 + 1);
            arrowLeft = glyphX + TasteGlyphs.width(font) + 3;
            if (!entry.shownBelow().isEmpty())
                graphics.drawString(font, model.collapsed(entry) && !screen.collapseIgnored() ? "▸" : "▾", arrowLeft,
                    textY, 0xFF9A9A9A, false);
            int textX = arrowLeft + 7;
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
            if (mouseX >= glyphX && mouseX < glyphX + TasteGlyphs.width(font))
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

        /**
         * Tree lines hanging from each ancestor's glyph: full height where that ancestor has more children below, and
         * for this row an elbow into its own glyph, stopping at the elbow when it's the last child.
         */
        private void renderGuides(GuiGraphics graphics, int base, int glyphX, int top) {
            for (int level = 1; level <= depth; level++) {
                int x = base + (level - 1) * INDENT + 3;
                if (level == depth) {
                    graphics.fill(x, top, x + 1, guides[level] ? top + ROW_HEIGHT : top + 7, GUIDE);
                    graphics.fill(x + 1, top + 6, glyphX - 1, top + 7, GUIDE);
                } else if (guides[level]) {
                    graphics.fill(x, top, x + 1, top + ROW_HEIGHT, GUIDE);
                }
            }
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
            return null;
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
            if (button == 0 && !entry.shownBelow().isEmpty() && !screen.collapseIgnored() && mouseX >= arrowLeft
                && mouseX < arrowLeft + 7) {
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
