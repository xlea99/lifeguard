package net.xlea99.lifeguard.client.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.SubValue;

/**
 * The right-hand pane: everything about the selected entry. Its description, what kind of thing it is, why it's on or
 * off right now, what it needs and what needs it (each one a link to that entry), and editors for its sub-values.
 *
 * <p>Laid out once per selection: text becomes a list of lines with positions, sub-values become widgets.
 */
final class DetailsPane {
    private static final int PAD = 6;
    private static final int LINE = 10;

    private record Line(int x, int y, FormattedCharSequence text, int color, Entry link, int width) {
    }

    private final LifeguardScreen screen;
    private final List<Line> lines = new ArrayList<>();
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private int left;
    private int top;
    private int width;
    private int height;
    private Entry entry;
    private int reasonX;
    private int reasonY;

    DetailsPane(LifeguardScreen screen) {
        this.screen = screen;
    }

    void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    Entry entry() {
        return entry;
    }

    /** Shows an entry (or nothing, for null), replacing whatever was there. */
    void show(Entry shown) {
        for (AbstractWidget widget : widgets)
            screen.removeDetailWidget(widget);
        widgets.clear();
        lines.clear();
        entry = shown;
        if (entry == null)
            return;

        MenuModel model = screen.model();
        Font font = screen.font();
        int x = left + PAD;
        int inner = width - 2 * PAD;
        int[] y = {top + PAD};

        text(font, Component.translatable(entry.nameKey()).withStyle(ChatFormatting.BOLD), x, y, inner, 0xFFFFFFFF, null);
        text(font, Component.translatable("lifeguard.menu.details.meta",
            Component.translatable("lifeguard.class." + entry.kind().id()),
            Component.translatable("lifeguard.taste." + entry.taste().id() + ".name"),
            Component.translatable("lifeguard.side." + entry.side().id())), x, y, inner, 0xFFA0A0A0, null);
        reserveReason(x, y);
        y[0] += 4;
        text(font, Component.translatable(entry.descKey()), x, y, inner, 0xFFD8D8D8, null);
        String broken = MenuModel.brokenReason(entry);
        if (broken != null)
            text(font, Component.translatable("lifeguard.menu.details.broken", broken), x, y, inner, 0xFFD07C7C, null);
        if (model.locked(entry))
            text(font, Component.translatable(model.brokenFile(entry.side()).isPresent()
                ? "lifeguard.menu.details.broken_note" : "lifeguard.menu.details.server_note"), x, y, inner, 0xFFE0A040, null);

        for (SubValue<?> value : entry.values())
            valueEditor(font, model, value, x, y, inner);

        y[0] += 4;
        mods(font, x, y, inner);
        links(font, "lifeguard.menu.details.requires", entry.requiredEntries(), x, y, inner);
        links(font, "lifeguard.menu.details.required_by", model.requiredBy(entry), x, y, inner);
    }

    /** Reserves the line for "why it's on or off". It's drawn live: it changes as switches flip, the layout doesn't. */
    private void reserveReason(int x, int[] y) {
        reasonX = x;
        reasonY = y[0];
        y[0] += LINE;
    }

    private Component reasonText(MenuModel.Reason reason) {
        return switch (reason.kind()) {
            case BROKEN -> Component.translatable("lifeguard.menu.details.why.broken");
            case BROKEN_HERE -> Component.translatable("lifeguard.menu.details.why.broken_here");
            case BROKEN_ON_SERVER -> Component.translatable("lifeguard.menu.details.why.broken_on_server");
            case REQUIRED_BROKEN -> Component.translatable("lifeguard.menu.details.why.required_broken",
                Component.translatable(reason.because().nameKey()));
            case MISSING_MODS -> Component.translatable("lifeguard.menu.details.why.missing_mods", modNames(entry.missingMods()));
            case REQUIRED_OFF -> Component.translatable("lifeguard.menu.details.why.required_off",
                Component.translatable(reason.because().nameKey()));
            case PROFILE -> Component.translatable(reason.active()
                ? "lifeguard.menu.details.why.profile_on" : "lifeguard.menu.details.why.profile_off");
            case OVERRIDE -> Component.translatable(reason.active()
                ? "lifeguard.menu.details.why.override_on" : "lifeguard.menu.details.why.override_off");
        };
    }

    private void valueEditor(Font font, MenuModel model, SubValue<?> value, int x, int[] y, int inner) {
        y[0] += 4;
        text(font, Component.translatable(value.nameKey())
            .append(Component.literal("  ").append(Component.translatable("lifeguard.menu.details.default",
                String.valueOf(value.defaultValue()))).withStyle(ChatFormatting.DARK_GRAY)), x, y, inner, 0xFFD8D8D8, null);
        int editorWidth = inner - 24;
        AbstractWidget editor;
        if (value instanceof SubValue.Bool flag) {
            Button toggle = Button.builder(boolLabel(flag), button -> {
                model.draft().setValue(flag, !model.draft().value(flag));
                button.setMessage(boolLabel(flag));
                screen.refresh();
            }).bounds(x, y[0], editorWidth, 20).build();
            editor = toggle;
        } else {
            editor = new ValueSlider(x, y[0], editorWidth, value, model.draft(), screen::refresh);
        }
        AbstractWidget editorFinal = editor;
        Button reset = Button.builder(Component.literal("↺"), button -> {
            resetValue(model, value);
            if (editorFinal instanceof ValueSlider slider)
                slider.sync();
            else if (value instanceof SubValue.Bool flag)
                editorFinal.setMessage(boolLabel(flag));
            screen.refresh();
        }).bounds(x + editorWidth + 4, y[0], 20, 20).build();
        reset.setTooltip(Tooltip.create(Component.translatable("lifeguard.menu.details.reset")));
        boolean editable = !model.locked(entry);
        editor.active = editable;
        reset.active = editable;
        add(editor);
        add(reset);
        y[0] += 22;
    }

    private static <T> void resetValue(MenuModel model, SubValue<T> value) {
        model.draft().setValue(value, value.defaultValue());
    }

    private Component boolLabel(SubValue.Bool flag) {
        return Component.translatable(screen.model().draft().value(flag)
            ? "lifeguard.menu.details.yes" : "lifeguard.menu.details.no");
    }

    private void mods(Font font, int x, int[] y, int inner) {
        if (entry.requiredMods().isEmpty()) {
            text(font, Component.translatable("lifeguard.menu.details.needs_nothing"), x, y, inner, 0xFF909090, null);
            return;
        }
        text(font, Component.translatable("lifeguard.menu.details.needs"), x, y, inner, 0xFFA0A0A0, null);
        List<String> missing = entry.missingMods();
        for (String modId : entry.requiredMods()) {
            boolean installed = !missing.contains(modId);
            MutableComponent line = Component.literal(installed ? "✔ " : "✘ ")
                .append(Component.translatableWithFallback("lifeguard.mod." + modId, modId));
            text(font, line, x + 6, y, inner - 6, installed ? 0xFF7CD07C : 0xFFD07C7C, null);
        }
    }

    private void links(Font font, String headingKey, List<Entry> targets, int x, int[] y, int inner) {
        if (targets.isEmpty())
            return;
        text(font, Component.translatable(headingKey), x, y, inner, 0xFFA0A0A0, null);
        for (Entry target : targets)
            text(font, Component.translatable(target.nameKey()).withStyle(ChatFormatting.UNDERLINE), x + 6, y, inner - 6,
                0xFF80B0FF, target);
    }

    private static Component modNames(List<String> modIds) {
        MutableComponent names = Component.empty();
        for (int i = 0; i < modIds.size(); i++) {
            if (i > 0)
                names.append(", ");
            names.append(Component.translatableWithFallback("lifeguard.mod." + modIds.get(i), modIds.get(i)));
        }
        return names;
    }

    /** Adds wrapped text, one Line per visual line; a link makes every line of it clickable. */
    private void text(Font font, Component text, int x, int[] y, int inner, int color, Entry link) {
        for (FormattedCharSequence part : font.split(text, inner)) {
            lines.add(new Line(x, y[0], part, color, link, font.width(part)));
            y[0] += LINE;
        }
    }

    private void add(AbstractWidget widget) {
        widgets.add(widget);
        screen.addDetailWidget(widget);
    }

    void renderBackdrop(GuiGraphics graphics) {
        graphics.fill(left, top, left + width, top + height, 0x60000000);
        graphics.renderOutline(left, top, width, height, 0x30FFFFFF);
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = screen.font();
        if (entry == null) {
            graphics.drawCenteredString(font, Component.translatable("lifeguard.menu.details.empty"), left + width / 2,
                top + height / 2 - 4, 0xFF808080);
            return;
        }
        graphics.enableScissor(left, top, left + width, top + height);
        for (Line line : lines) {
            boolean hovered = line.link() != null && over(line, mouseX, mouseY);
            graphics.drawString(font, line.text(), line.x(), line.y(), hovered ? 0xFFFFFFFF : line.color(), false);
        }
        Line reason = reasonLine();
        boolean hovered = reason.link() != null && over(reason, mouseX, mouseY);
        graphics.drawString(font, reason.text(), reason.x(), reason.y(), hovered ? 0xFFFFFFFF : reason.color(), false);
        graphics.disableScissor();
    }

    /** The reason as it stands this frame, cut to one line. Links to the entry responsible, when there is one. */
    private Line reasonLine() {
        MenuModel.Reason reason = screen.model().why(entry);
        Font font = screen.font();
        FormattedCharSequence text = Language.getInstance()
            .getVisualOrder(font.substrByWidth(reasonText(reason), width - 2 * PAD));
        return new Line(reasonX, reasonY, text, reason.active() ? 0xFF7CD07C : 0xFFD07C7C, reason.because(), font.width(text));
    }

    /** A click on a link selects that entry. */
    boolean click(double mouseX, double mouseY) {
        if (entry != null) {
            Line reason = reasonLine();
            if (reason.link() != null && over(reason, mouseX, mouseY)) {
                screen.select(reason.link(), true);
                return true;
            }
        }
        for (Line line : lines) {
            if (line.link() != null && over(line, mouseX, mouseY)) {
                screen.select(line.link(), true);
                return true;
            }
        }
        return false;
    }

    private boolean over(Line line, double mouseX, double mouseY) {
        return mouseX >= line.x() && mouseX < line.x() + line.width() && mouseY >= line.y() - 1 && mouseY < line.y() + 9
            && mouseY < top + height;
    }
}
