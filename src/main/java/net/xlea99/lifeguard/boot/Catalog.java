package net.xlea99.lifeguard.boot;

import java.util.List;
import net.xlea99.lifeguard.entry.cc_cannon_backfire.CcCannonBackfire;
import net.xlea99.lifeguard.entry.dye_depot_runes.DyeDepotRunes;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.FaucetPouringRecipes;
import net.xlea99.lifeguard.entry.no_supps_jar_fish.NoSuppsJarFish;
import net.xlea99.lifeguard.entry.quark_amethyst_beacon_tint.QuarkAmethystBeaconTint;
import net.xlea99.lifeguard.entry.quark_donkey_chests.QuarkDonkeyChests;
import net.xlea99.lifeguard.entry.quark_elytra_duplication_crash.QuarkElytraDuplicationCrash;
import net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing.QuarkParrotEggDispensing;
import net.xlea99.lifeguard.entry.quark_tool_restock_enchantments.QuarkToolRestockEnchantments;
import net.xlea99.lifeguard.entry.sexy_runic_glints.SexyRunicGlints;
import net.xlea99.lifeguard.entry.supplementaries_grouper_open_sides.SupplementariesGrouperOpenSides;
import net.xlea99.lifeguard.entry.supps_blue_bomb_dispensing.SuppsBlueBombDispensing;
import net.xlea99.lifeguard.entry.supps_cannon_comparator.SuppsCannonComparator;
import net.xlea99.lifeguard.entry.supps_captured_mob_jitter.SuppsCapturedMobJitter;
import net.xlea99.lifeguard.entry.supps_faucet_cauldron_levels.SuppsFaucetCauldronLevels;
import net.xlea99.lifeguard.entry.supps_faucet_honey.SuppsFaucetHoney;
import net.xlea99.lifeguard.entry.supps_lumisene_burn_time.SuppsLumiseneBurnTime;
import net.xlea99.lifeguard.entry.supps_notice_board_comparator.SuppsNoticeBoardComparator;
import net.xlea99.lifeguard.entry.supps_plunderer_raid_weapon.SuppsPlundererRaidWeapon;
import net.xlea99.lifeguard.entry.supps_respects_mob_griefing.SuppsRespectsMobGriefing;
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
        CcCannonBackfire.class,
        DyeDepotRunes.class,
        FaucetPouringRecipes.class,
        NoSuppsJarFish.class,
        QuarkAmethystBeaconTint.class,
        QuarkDonkeyChests.class,
        QuarkElytraDuplicationCrash.class,
        QuarkParrotEggDispensing.class,
        QuarkToolRestockEnchantments.class,
        SexyRunicGlints.class,
        SupplementariesGrouperOpenSides.class,
        SuppsBlueBombDispensing.class,
        SuppsCannonComparator.class,
        SuppsCapturedMobJitter.class,
        SuppsFaucetCauldronLevels.class,
        SuppsFaucetHoney.class,
        SuppsLumiseneBurnTime.class,
        SuppsNoticeBoardComparator.class,
        SuppsPlundererRaidWeapon.class,
        SuppsRespectsMobGriefing.class,
        ZetaInstanceRace.class
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
