package net.xlea99.lifeguard.core;

/**
 * The state entries resolve against: a profile per side, a switch per entry, a value per sub-value. The live
 * {@link Registry} is one view (what this session runs with); a {@link Settings} draft is another (what the menu is
 * editing, or what's in a file waiting for the next restart). {@link Entry#activeUnder} is the one rule for both.
 */
public interface SettingsView {
    Profile profile(Side side);

    Switch setting(Entry entry);

    <T> T value(SubValue<T> value);
}
