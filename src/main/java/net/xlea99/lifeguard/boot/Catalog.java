package net.xlea99.lifeguard.boot;

import java.util.List;
import net.xlea99.lifeguard.entry.planned.Planned;
import net.xlea99.lifeguard.entry.quark_donkey_chests.QuarkDonkeyChests;
import net.xlea99.lifeguard.entry.quark_elytra_duplication_crash.QuarkElytraDuplicationCrash;
import net.xlea99.lifeguard.entry.zeta_instance_race.ZetaInstanceRace;

/**
 * Every class that declares entries. Startup loads each one so its constants register before the freeze.
 *
 * <p>These classes are read before Minecraft's classes are safe to load, so a declaration class holds only its entry
 * and sub-value constants: no Minecraft, NeoForge or other mods' imports. Its gameplay code lives beside it in other
 * classes. The workspace tests check every class listed here.
 */
public final class Catalog {
    private static final List<Class<?>> DECLARATIONS = List.of(
        QuarkDonkeyChests.class,
        QuarkElytraDuplicationCrash.class,
        ZetaInstanceRace.class,
        Planned.class
    );

    private Catalog() {
    }

    public static List<Class<?>> declarations() {
        return DECLARATIONS;
    }

    /** Runs each declaration class's static initializer, which is where its entries register. */
    static void load() {
        for (Class<?> declarations : DECLARATIONS) {
            try {
                Class.forName(declarations.getName(), true, declarations.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Lifeguard couldn't load its declarations in " + declarations.getName(), e);
            }
        }
    }
}
