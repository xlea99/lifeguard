package net.xlea99.lifeguard.client.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Side;

/**
 * Shown when leaving the menu with changes saved: exactly what will be different once the game restarts (profiles,
 * entries switching on and off, changed values), with Quit game, Restart later and Keep editing.
 *
 * <p>Everything here needs a full restart: Lifeguard reads its config files once, at startup, so nothing half-applies.
 */
final class RestartSummaryScreen extends Screen {
    private static final int LIST_TOP = 44;
    private static final int LINE = 11;

    private final LifeguardScreen menu;
    private final Screen parent;
    // The summary as whole lines; wrapped into `lines` in init, once the screen's width is known.
    private final List<Component> parts = new ArrayList<>();
    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private int scroll;

    RestartSummaryScreen(LifeguardScreen menu, Screen parent, MenuModel.Summary summary) {
        super(Component.translatable("lifeguard.menu.summary.title"));
        this.menu = menu;
        this.parent = parent;
        build(summary);
    }

    private void build(MenuModel.Summary summary) {
        for (Side side : summary.profileSides())
            parts.add(Component.translatable("lifeguard.menu.summary.profile",
                Component.translatable("lifeguard.side." + side.id()),
                Component.translatable("lifeguard.profile." + menu.model().liveProfile(side).id()),
                Component.translatable("lifeguard.profile." + menu.model().draft().profile(side).id())
                    .withStyle(ChatFormatting.YELLOW)));
        if (!summary.turningOn().isEmpty()) {
            parts.add(Component.translatable("lifeguard.menu.summary.on", summary.turningOn().size())
                .withStyle(ChatFormatting.GREEN));
            for (Entry entry : summary.turningOn())
                parts.add(Component.literal("  + ").append(Component.translatable(entry.nameKey())).withStyle(ChatFormatting.GREEN));
        }
        if (!summary.turningOff().isEmpty()) {
            parts.add(Component.translatable("lifeguard.menu.summary.off", summary.turningOff().size())
                .withStyle(ChatFormatting.RED));
            for (Entry entry : summary.turningOff())
                parts.add(Component.literal("  − ").append(Component.translatable(entry.nameKey())).withStyle(ChatFormatting.RED));
        }
        if (!summary.values().isEmpty()) {
            parts.add(Component.translatable("lifeguard.menu.summary.values").withStyle(ChatFormatting.AQUA));
            for (MenuModel.ValueChange change : summary.values()) {
                MutableComponent line = Component.literal("  ").append(Component.translatable(change.entry().nameKey()))
                    .append(" › ").append(Component.translatable(change.value().nameKey()))
                    .append(": " + change.before() + " → ")
                    .append(Component.literal(String.valueOf(change.after())).withStyle(ChatFormatting.YELLOW));
                parts.add(line);
            }
        }
    }

    @Override
    protected void init() {
        lines.clear();
        int wrap = Math.min(width - 40, 420);
        for (Component part : parts)
            lines.addAll(font.split(part, wrap));

        int buttonWidth = 110;
        int y = height - 28;
        int x = width / 2 - (buttonWidth * 3 + 8) / 2;
        addRenderableWidget(Button.builder(Component.translatable("lifeguard.menu.summary.quit"), button -> minecraft.stop())
            .bounds(x, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("lifeguard.menu.summary.later"),
            button -> minecraft.setScreen(parent)).bounds(x + buttonWidth + 4, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("lifeguard.menu.summary.keep_editing"),
            button -> minecraft.setScreen(menu)).bounds(x + (buttonWidth + 4) * 2, y, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("lifeguard.menu.summary.subtitle"), width / 2, 26, 0xFFA0A0A0);
        int left = width / 2 - Math.min(width - 40, 420) / 2;
        int bottom = height - 36;
        graphics.enableScissor(0, LIST_TOP, width, bottom);
        int y = LIST_TOP - scroll;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, left, y, 0xFFE0E0E0, false);
            y += LINE;
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = Math.max(0, lines.size() * LINE - (height - 36 - LIST_TOP));
        scroll = Math.max(0, Math.min(max, scroll - (int) (scrollY * LINE * 2)));
        return true;
    }

    /** Esc is "restart later": the changes are already saved. */
    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
