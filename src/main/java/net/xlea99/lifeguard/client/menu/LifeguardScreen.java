package net.xlea99.lifeguard.client.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.xlea99.lifeguard.core.Entry;
import net.xlea99.lifeguard.core.Profile;
import net.xlea99.lifeguard.core.Side;
import net.xlea99.lifeguard.core.Switch;
import net.xlea99.lifeguard.core.Taste;
import org.lwjgl.glfw.GLFW;

/**
 * Lifeguard's menu: a profile bar across the top, the entries on the left, the selected entry's details on the right,
 * Done and Cancel at the bottom.
 *
 * <p>Everything edits a draft. Done (or Esc, as in vanilla's own option screens) writes it to the config files and
 * Cancel throws it away; either way nothing changes until the next restart, and the status line says when one would.
 */
public final class LifeguardScreen extends Screen {
    private static final int BAR_TOP = 22;
    private static final int STATUS_TOP = 48;
    private static final int BODY_TOP = 62;
    private static final int FOOTER = 36;
    private static final int GAP = 6;

    private final Screen parent;
    private final MenuModel model = new MenuModel();
    private final List<Button> profileButtons = new ArrayList<>();
    private final DetailsPane details = new DetailsPane(this);
    private final Filters filters = new Filters();
    private final FilterPane filterPane = new FilterPane(this, filters);
    private EntryList list;
    private Button resetButton;
    private Entry selected;
    private Component saveProblem;
    // "/" focuses the search box on key press; the character that follows must not land in it.
    private boolean swallowSlash;

    public LifeguardScreen(Screen parent) {
        super(Component.translatable("lifeguard.menu.title"));
        this.parent = parent;
    }

    MenuModel model() {
        return model;
    }

    Font font() {
        return font;
    }

    Entry selected() {
        return selected;
    }

    @Override
    protected void init() {
        profileButtons.clear();
        Profile[] profiles = Profile.values();
        int gap = 4;
        int buttonWidth = Math.min(110, (width - 40 - gap * (profiles.length - 1)) / profiles.length);
        int barLeft = (width - (buttonWidth * profiles.length + gap * (profiles.length - 1))) / 2;
        for (int i = 0; i < profiles.length; i++) {
            Profile profile = profiles[i];
            profileButtons.add(addRenderableWidget(Button.builder(profileName(profile), button -> pickProfile(profile))
                .bounds(barLeft + i * (buttonWidth + gap), BAR_TOP, buttonWidth, 20).build()));
        }

        // Three panes: filters, entries, details. The entries get whatever the other two leave.
        int bodyHeight = height - BODY_TOP - FOOTER;
        int filtersWidth = Math.max(96, Math.min(140, (int) (width * 0.2)));
        int detailsWidth = Math.max(150, Math.min(280, (int) (width * 0.3)));
        int listWidth = width - filtersWidth - detailsWidth - GAP * 4;
        filterPane.layout(GAP, BODY_TOP, filtersWidth, bodyHeight);
        // The column header is pinned above the rows; the list itself starts below it and scrolls under it.
        list = addRenderableWidget(new EntryList(minecraft, listWidth, bodyHeight - EntryList.HEADER_HEIGHT,
            BODY_TOP + EntryList.HEADER_HEIGHT, this));
        list.setX(GAP * 2 + filtersWidth);
        details.setBounds(width - detailsWidth - GAP, BODY_TOP, detailsWidth, bodyHeight);
        rebuildRows();
        details.show(selected);

        int footerWidth = 104;
        int footerLeft = width / 2 - (footerWidth * 3 + 8) / 2;
        resetButton = addRenderableWidget(Button.builder(Component.translatable("lifeguard.menu.reset"),
            button -> confirmReset()).bounds(footerLeft, height - 28, footerWidth, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
            .bounds(footerLeft + footerWidth + 4, height - 28, footerWidth, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> minecraft.setScreen(parent))
            .bounds(footerLeft + (footerWidth + 4) * 2, height - 28, footerWidth, 20).build());
        refresh();
    }

    // --- choices that ask first ---------------------------------------------------------------------------------------

    /** Off the Deep End names what it would turn on before it does; every other profile applies at once. */
    private void pickProfile(Profile profile) {
        List<Entry> turningOn = model.wouldTurnOn(profile);
        if (profile != Profile.OFF_THE_DEEP_END || turningOn.isEmpty()) {
            applyProfile(profile);
            return;
        }
        MutableComponent names = Component.empty();
        int shown = Math.min(turningOn.size(), 8);
        for (int i = 0; i < shown; i++)
            names.append(i == 0 ? "" : ", ").append(Component.translatable(turningOn.get(i).nameKey()));
        if (turningOn.size() > shown)
            names.append(Component.translatable("lifeguard.menu.confirm.and_more", turningOn.size() - shown));
        minecraft.setScreen(new ConfirmScreen(yes -> {
            if (yes)
                applyProfile(profile);
            minecraft.setScreen(this);
        }, Component.translatable("lifeguard.menu.confirm.deep_end.title"),
            Component.translatable("lifeguard.menu.confirm.deep_end.message", turningOn.size(), names)));
    }

    private void applyProfile(Profile profile) {
        Map<Entry, Boolean> before = activeNow();
        model.pickProfile(profile);
        startRipple(before);
        refresh();
    }

    /** One step of an entry's switch: forward from a left click or the space bar, backward from a right click. */
    void cycle(Entry entry, boolean forward) {
        setSwitch(entry, MenuModel.step(model.draft().setting(entry), forward));
    }

    /**
     * Every change to a switch comes through here. Landing a dangerous entry on On asks first, quoting its warning,
     * whichever way the player got there: it's the only way such an entry is ever on.
     */
    void setSwitch(Entry entry, Switch target) {
        if (model.locked(entry))
            return;
        if (entry.taste() == Taste.DANGEROUS && target == Switch.ON && model.draft().setting(entry) != Switch.ON) {
            minecraft.setScreen(new ConfirmScreen(yes -> {
                if (yes) {
                    model.set(entry, target);
                    refresh();
                }
                minecraft.setScreen(this);
            }, Component.translatable("lifeguard.menu.confirm.dangerous.title", Component.translatable(entry.nameKey())),
                Component.translatable(entry.descKey())));
            return;
        }
        model.set(entry, target);
        refresh();
    }

    private void confirmReset() {
        int count = model.resettableOverrides();
        if (count == 0)
            return;
        minecraft.setScreen(new ConfirmScreen(yes -> {
            if (yes) {
                Map<Entry, Boolean> before = activeNow();
                model.resetOverrides();
                startRipple(before);
                refresh();
            }
            minecraft.setScreen(this);
        }, Component.translatable("lifeguard.menu.confirm.reset.title"),
            Component.translatable("lifeguard.menu.confirm.reset.message", count)));
    }

    // --- the ripple ---------------------------------------------------------------------------------------------------

    private record Ripple(long start, boolean on) {
    }

    private static final long RIPPLE_STEP = 35;
    private static final long RIPPLE_MAX_DELAY = 600;
    static final long RIPPLE_LENGTH = 350;

    private final Map<Entry, Ripple> ripples = new HashMap<>();

    private Map<Entry, Boolean> activeNow() {
        Map<Entry, Boolean> active = new HashMap<>();
        for (MenuModel.Line line : model.lines(filters))
            if (!line.isHeading())
                active.put(line.entry(), line.entry().activeUnder(model.draft()));
        return active;
    }

    /** Rows that flipped flash green (now on) or red (now off), one after another from the top. */
    private void startRipple(Map<Entry, Boolean> before) {
        ripples.clear();
        long now = Util.getMillis();
        int index = 0;
        for (MenuModel.Line line : model.lines(filters)) {
            if (line.isHeading())
                continue;
            Entry entry = line.entry();
            Boolean was = before.get(entry);
            boolean isOn = entry.activeUnder(model.draft());
            if (was != null && was != isOn)
                ripples.put(entry, new Ripple(now + Math.min(index * RIPPLE_STEP, RIPPLE_MAX_DELAY), isOn));
            index++;
        }
    }

    /** The ripple's overlay color for a row right now (alpha included), or 0 for none. */
    int rippleColor(Entry entry) {
        Ripple ripple = ripples.get(entry);
        if (ripple == null)
            return 0;
        long age = Util.getMillis() - ripple.start();
        if (age < 0)
            return 0;
        if (age >= RIPPLE_LENGTH) {
            ripples.remove(entry);
            return 0;
        }
        int alpha = (int) (0x70 * (1 - age / (double) RIPPLE_LENGTH));
        return (alpha << 24) | (ripple.on() ? 0x40C060 : 0xD04040);
    }

    /** While any filter is on, collapsing does nothing (the model shows every match): arrows show open and don't click. */
    boolean collapseIgnored() {
        return filters.any();
    }

    void rebuildRows() {
        list.show(model.lines(filters));
    }

    void filtersChanged() {
        rebuildRows();
    }

    void addFilterWidget(AbstractWidget widget) {
        addRenderableWidget(widget);
    }

    /** Shows an entry in the details pane; with reveal, also opens its collapsed parents and scrolls the list to it. */
    void select(Entry entry, boolean reveal) {
        selected = entry;
        if (reveal) {
            model.expandTo(entry);
            rebuildRows();
            list.reveal(entry);
        }
        details.show(entry);
    }

    void addDetailWidget(AbstractWidget widget) {
        addRenderableWidget(widget);
    }

    void removeDetailWidget(AbstractWidget widget) {
        removeWidget(widget);
    }

    /** Re-labels what depends on the draft: the profile buttons (the picked one highlighted). Rows redraw themselves. */
    void refresh() {
        Optional<Profile> shown = model.shownProfile();
        Profile[] profiles = Profile.values();
        for (int i = 0; i < profiles.length; i++) {
            MutableComponent name = profileName(profiles[i]);
            if (shown.isPresent() && shown.get() == profiles[i])
                name = Component.literal("▶ ").append(name).append(" ◀")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            profileButtons.get(i).setMessage(name);
        }
        resetButton.active = model.resettableOverrides() > 0;
        saveProblem = null;
    }

    private static MutableComponent profileName(Profile profile) {
        return Component.translatable("lifeguard.profile." + profile.id());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFFFF);
        graphics.drawCenteredString(font, status(), width / 2, STATUS_TOP, 0xFFB0B0B0);
        list.renderHeader(graphics, BODY_TOP, mouseX, mouseY);
        filterPane.render(graphics, mouseX, mouseY);
        details.render(graphics, mouseX, mouseY);
    }

    /** The panes' backdrops go under every widget, so they don't dim the sliders and search box drawn on them. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        filterPane.renderBackdrop(graphics);
        details.renderBackdrop(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && (filterPane.click(mouseX, mouseY) || details.click(mouseX, mouseY) || list.headerClick(mouseX, mouseY)))
            return true;
        if (button == 1 && list.rightClick(mouseX, mouseY))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return filterPane.scroll(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * The keyboard: "/" jumps to search; up and down move the selection through the visible entries; left and right
     * collapse and expand it; space cycles its switch. While typing in search, keys go to the search box.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        EditBox search = filterPane.search();
        if (search != null && search.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_ENTER) {
                search.setFocused(false);
                setFocused(null);
                moveSelection(selected == null ? 0 : 1);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_SLASH -> {
                setFocused(search);
                swallowSlash = true;
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                moveSelection(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                moveSelection(1);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> {
                if (selected != null && !selected.children().isEmpty() && !collapseIgnored()
                    && model.collapsed(selected) == (keyCode == GLFW.GLFW_KEY_RIGHT)) {
                    model.toggleCollapsed(selected);
                    rebuildRows();
                }
                return true;
            }
            case GLFW.GLFW_KEY_SPACE -> {
                if (selected != null) {
                    cycle(selected, true);
                    return true;
                }
            }
            default -> {
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (swallowSlash && character == '/') {
            swallowSlash = false;
            return true;
        }
        swallowSlash = false;
        return super.charTyped(character, modifiers);
    }

    /** Moves the selection by one visible entry (headings skipped), or to the first one when nothing is selected. */
    private void moveSelection(int step) {
        List<Entry> visible = new ArrayList<>();
        for (MenuModel.Line line : model.lines(filters))
            if (!line.isHeading())
                visible.add(line.entry());
        if (visible.isEmpty())
            return;
        int at = visible.indexOf(selected);
        int next = at < 0 ? 0 : Math.max(0, Math.min(visible.size() - 1, at + step));
        selected = visible.get(next);
        details.show(selected);
        list.reveal(selected);
    }

    private Component status() {
        if (saveProblem != null)
            return saveProblem;
        MutableComponent line = model.shownProfile()
            .<MutableComponent>map(LifeguardScreen::profileName)
            .orElseGet(() -> Component.translatable("lifeguard.menu.status.mixed",
                profileName(model.draft().profile(Side.CLIENT)), profileName(model.draft().profile(Side.SERVER))));
        line = Component.translatable("lifeguard.menu.status", line, model.activeCount(), model.overrides());
        if (model.following())
            line.append(" · ").append(Component.translatable("lifeguard.menu.following"));
        Optional<String> broken = model.brokenFile(Side.SERVER).or(() -> model.brokenFile(Side.CLIENT));
        if (broken.isPresent())
            line.append(" · ").append(Component.translatable("lifeguard.menu.broken", broken.get()).withStyle(ChatFormatting.RED));
        else if (model.restartPending())
            line.append(" · ").append(Component.translatable("lifeguard.menu.restart").withStyle(ChatFormatting.YELLOW));
        return line;
    }

    /**
     * Esc and Done both save, as vanilla's option screens do. A failed save keeps the menu open and says why. When the
     * saved state differs from what's running, the restart summary comes next; otherwise straight back.
     */
    @Override
    public void onClose() {
        Optional<String> problem = model.save();
        if (problem.isPresent()) {
            saveProblem = Component.translatable("lifeguard.menu.save_failed", problem.get()).withStyle(ChatFormatting.RED);
            return;
        }
        MenuModel.Summary summary = model.summary();
        minecraft.setScreen(summary.empty() ? parent : new RestartSummaryScreen(this, parent, summary));
    }
}
