package net.xlea99.lifeguard.boot;

import java.util.List;
import net.xlea99.lifeguard.entry.ac_infinite_soda.AcInfiniteSoda;
import net.xlea99.lifeguard.entry.am_lava_bottle_dupe.AmLavaBottleDupe;
import net.xlea99.lifeguard.entry.am_lava_vision_fallback.AmLavaVisionFallback;
import net.xlea99.lifeguard.entry.amendments_cake_slicing.AmendmentsCakeSlicing;
import net.xlea99.lifeguard.entry.amendments_cauldron_dispensing.AmendmentsCauldronDispensing;
import net.xlea99.lifeguard.entry.amendments_hanging_pot_names.AmendmentsHangingPotNames;
import net.xlea99.lifeguard.entry.amendments_lava_drip_layers.AmendmentsLavaDripLayers;
import net.xlea99.lifeguard.entry.advancement_frames_packet_checks.AdvancementFramesPacketChecks;
import net.xlea99.lifeguard.entry.amendments_lava_layers.AmendmentsLavaLayers;
import net.xlea99.lifeguard.entry.amendments_mixed_potion_brewing.AmendmentsMixedPotionBrewing;
import net.xlea99.lifeguard.entry.amendments_potion_dilution.AmendmentsPotionDilution;
import net.xlea99.lifeguard.entry.bucket_mob_flicker.BucketMobFlicker;
import net.xlea99.lifeguard.entry.cauldron_fourths.CauldronFourths;
import net.xlea99.lifeguard.entry.cc_tether_potion_compat.CcTetherPotionCompat;
import net.xlea99.lifeguard.entry.citadel_animation_smoothing.CitadelAnimationSmoothing;
import net.xlea99.lifeguard.entry.cc_cannon_backfire.CcCannonBackfire;
import net.xlea99.lifeguard.entry.cc_golden_bucket_compat.CcGoldenBucketCompat;
import net.xlea99.lifeguard.entry.cc_golden_milk_follows_neapolitan.CcGoldenMilkFollowsNeapolitan;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.dye_depot_runes.DyeDepotRunes;
import net.xlea99.lifeguard.entry.dye_depot_rock_candy.DyeDepotRockCandy;
import net.xlea99.lifeguard.entry.dye_depot_sparklers.DyeDepotSparklers;
import net.xlea99.lifeguard.entry.emf_haunted_harvest_masks.EmfHauntedHarvestMasks;
import net.xlea99.lifeguard.entry.haunted_harvest_mask_limit.HauntedHarvestMaskLimit;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.HauntedHarvestPumpkinCompat;
import net.xlea99.lifeguard.entry.haunted_harvest_torch_lanterns.HauntedHarvestTorchLanterns;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.HauntedHarvestUnifiedCarvedPumpkin;
import net.xlea99.lifeguard.entry.enderman_head_reach.EndermanHeadReach;
import net.xlea99.lifeguard.entry.etcetera_easy_anvils_label_costs.EtceteraEasyAnvilsLabelCosts;
import net.xlea99.lifeguard.entry.etcetera_label_wash_stacking.EtceteraLabelWashStacking;
import net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims.EtceteraShimmeringBismuthTrims;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.FaucetPouringRecipes;
import net.xlea99.lifeguard.entry.map_atlases_fixes.MapAtlasesFixes;
import net.xlea99.lifeguard.entry.moonlight_jade_contents.MoonlightJadeContents;
import net.xlea99.lifeguard.entry.moonlight_registration_races.MoonlightRegistrationRaces;
import net.xlea99.lifeguard.entry.neapolitan_amendments_milkshakes.NeapolitanAmendmentsMilkshakes;
import net.xlea99.lifeguard.entry.no_supps_jar_fish.NoSuppsJarFish;
import net.xlea99.lifeguard.entry.quark_amethyst_beacon_tint.QuarkAmethystBeaconTint;
import net.xlea99.lifeguard.entry.quark_donkey_chests.QuarkDonkeyChests;
import net.xlea99.lifeguard.entry.quark_elytra_duplication_crash.QuarkElytraDuplicationCrash;
import net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing.QuarkParrotEggDispensing;
import net.xlea99.lifeguard.entry.no_end_cave_sounds.NoEndCaveSounds;
import net.xlea99.lifeguard.entry.paper_labels.PaperLabels;
import net.xlea99.lifeguard.entry.quark_tool_restock_enchantments.QuarkToolRestockEnchantments;
import net.xlea99.lifeguard.entry.scholar_amendments_lectern_sync.ScholarAmendmentsLecternSync;
import net.xlea99.lifeguard.entry.scholar_lectern_atlases.ScholarLecternAtlases;
import net.xlea99.lifeguard.entry.sexy_runic_glints.SexyRunicGlints;
import net.xlea99.lifeguard.entry.sleep_tight_kill_stats.SleepTightKillStats;
import net.xlea99.lifeguard.entry.snowy_spirit_brighter_ginger.SnowySpiritBrighterGinger;
import net.xlea99.lifeguard.entry.snowy_spirit_edible_treats.SnowySpiritEdibleTreats;
import net.xlea99.lifeguard.entry.snowy_spirit_eggnog_nerf.SnowySpiritEggnogNerf;
import net.xlea99.lifeguard.entry.sleep_tight_spawn_events.SleepTightSpawnEvents;
import net.xlea99.lifeguard.entry.supplementaries_grouper_open_sides.SupplementariesGrouperOpenSides;
import net.xlea99.lifeguard.entry.supps_blue_bomb_dispensing.SuppsBlueBombDispensing;
import net.xlea99.lifeguard.entry.supps_soap_lore_first.SuppsSoapLoreFirst;
import net.xlea99.lifeguard.entry.supps_soap_washes_etcetera_labels.SuppsSoapWashesEtceteraLabels;
import net.xlea99.lifeguard.entry.supps_soap_washes_labels.SuppsSoapWashesLabels;
import net.xlea99.lifeguard.entry.twigs_bloodstone_obelisks.TwigsBloodstoneObelisks;
import net.xlea99.lifeguard.entry.twigs_gravel_pebbles.TwigsGravelPebbles;
import net.xlea99.lifeguard.entry.twigs_leaf_twigs.TwigsLeafTwigs;
import net.xlea99.lifeguard.entry.twigs_pebble_dispensing.TwigsPebbleDispensing;
import net.xlea99.lifeguard.entry.warden_roar_loop.WardenRoarLoop;
import net.xlea99.lifeguard.entry.whiter_labels.WhiterLabels;
import net.xlea99.lifeguard.entry.supps_cannon_comparator.SuppsCannonComparator;
import net.xlea99.lifeguard.entry.supps_continuous_hand_ropes.SuppsContinuousHandRopes;
import net.xlea99.lifeguard.entry.supps_drinkables_sip_pacing.SuppsDrinkablesSipPacing;
import net.xlea99.lifeguard.entry.supps_captured_mob_jitter.SuppsCapturedMobJitter;
import net.xlea99.lifeguard.entry.supps_faucet_cauldron_levels.SuppsFaucetCauldronLevels;
import net.xlea99.lifeguard.entry.supps_faucet_honey.SuppsFaucetHoney;
import net.xlea99.lifeguard.entry.supps_faucet_toggle.SuppsFaucetToggle;
import net.xlea99.lifeguard.entry.supps_gunpowder_lights_nuke.SuppsGunpowderLightsNuke;
import net.xlea99.lifeguard.entry.supps_lumisene_burn_time.SuppsLumiseneBurnTime;
import net.xlea99.lifeguard.entry.supps_moved_block_entities.SuppsMovedBlockEntities;
import net.xlea99.lifeguard.entry.supps_moved_cauldron_filling.SuppsMovedCauldronFilling;
import net.xlea99.lifeguard.entry.supps_moving_block_contents.SuppsMovingBlockContents;
import net.xlea99.lifeguard.entry.supps_notice_board_comparator.SuppsNoticeBoardComparator;
import net.xlea99.lifeguard.entry.supps_pedestal_compat.SuppsPedestalCompat;
import net.xlea99.lifeguard.entry.supps_plunderer_raid_weapon.SuppsPlundererRaidWeapon;
import net.xlea99.lifeguard.entry.supps_respects_mob_griefing.SuppsRespectsMobGriefing;
import net.xlea99.lifeguard.entry.supps_slice_maps_keep_the_altimeter.SuppsSliceMapsKeepTheAltimeter;
import net.xlea99.lifeguard.entry.themed_candle_holder_flames.ThemedCandleHolderFlames;
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
        AcInfiniteSoda.class,
        AmLavaBottleDupe.class,
        AmLavaVisionFallback.class,
        AmendmentsCakeSlicing.class,
        AmendmentsCauldronDispensing.class,
        AmendmentsHangingPotNames.class,
        AmendmentsLavaDripLayers.class,
        AmendmentsLavaLayers.class,
        AdvancementFramesPacketChecks.class,
        AmendmentsMixedPotionBrewing.class,
        AmendmentsPotionDilution.class,
        BucketMobFlicker.class,
        CauldronFourths.class,
        CcTetherPotionCompat.class,
        CitadelAnimationSmoothing.class,
        CcCannonBackfire.class,
        CcGoldenBucketCompat.class,
        CcGoldenMilkFollowsNeapolitan.class,
        ClayworksFlowerPots.class,
        DyeDepotRunes.class,
        DyeDepotRockCandy.class,
        DyeDepotSparklers.class,
        EmfHauntedHarvestMasks.class,
        HauntedHarvestMaskLimit.class,
        HauntedHarvestPumpkinCompat.class,
        HauntedHarvestTorchLanterns.class,
        HauntedHarvestUnifiedCarvedPumpkin.class,
        EndermanHeadReach.class,
        EtceteraEasyAnvilsLabelCosts.class,
        EtceteraLabelWashStacking.class,
        EtceteraShimmeringBismuthTrims.class,
        FaucetPouringRecipes.class,
        MapAtlasesFixes.class,
        MoonlightJadeContents.class,
        MoonlightRegistrationRaces.class,
        NeapolitanAmendmentsMilkshakes.class,
        NoSuppsJarFish.class,
        QuarkAmethystBeaconTint.class,
        QuarkDonkeyChests.class,
        QuarkElytraDuplicationCrash.class,
        QuarkParrotEggDispensing.class,
        NoEndCaveSounds.class,
        PaperLabels.class,
        QuarkToolRestockEnchantments.class,
        ScholarAmendmentsLecternSync.class,
        ScholarLecternAtlases.class,
        SexyRunicGlints.class,
        SleepTightKillStats.class,
        SleepTightSpawnEvents.class,
        SnowySpiritBrighterGinger.class,
        SnowySpiritEdibleTreats.class,
        SnowySpiritEggnogNerf.class,
        SupplementariesGrouperOpenSides.class,
        SuppsBlueBombDispensing.class,
        SuppsSoapLoreFirst.class,
        SuppsSoapWashesEtceteraLabels.class,
        SuppsSoapWashesLabels.class,
        WardenRoarLoop.class,
        WhiterLabels.class,
        SuppsCannonComparator.class,
        SuppsCapturedMobJitter.class,
        SuppsContinuousHandRopes.class,
        SuppsDrinkablesSipPacing.class,
        SuppsFaucetCauldronLevels.class,
        SuppsFaucetHoney.class,
        SuppsFaucetToggle.class,
        SuppsGunpowderLightsNuke.class,
        SuppsLumiseneBurnTime.class,
        SuppsMovedBlockEntities.class,
        SuppsMovedCauldronFilling.class,
        SuppsMovingBlockContents.class,
        SuppsNoticeBoardComparator.class,
        SuppsPedestalCompat.class,
        SuppsPlundererRaidWeapon.class,
        SuppsRespectsMobGriefing.class,
        SuppsSliceMapsKeepTheAltimeter.class,
        ThemedCandleHolderFlames.class,
        TwigsBloodstoneObelisks.class,
        TwigsGravelPebbles.class,
        TwigsLeafTwigs.class,
        TwigsPebbleDispensing.class,
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
