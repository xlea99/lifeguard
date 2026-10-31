package net.xlea99.lifeguard;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.boids.Boids;
import net.xlea99.lifeguard.duct.StorageDucts;
import net.xlea99.lifeguard.entry.boidsify.Boidsify;
import net.xlea99.lifeguard.entry.cc_crafted_tether_potions.CcCraftedTetherPotions;
import net.xlea99.lifeguard.entry.cc_storage_duct_span_limit.CcStorageDuctSpanLimit;
import net.xlea99.lifeguard.entry.cc_flint_piston_striking.CcFlintPistonStriking;
import net.xlea99.lifeguard.entry.cc_flint_piston_striking.FlintStriking;
import net.xlea99.lifeguard.entry.species_bloodlust_all_food.BloodlustFood;
import net.xlea99.lifeguard.entry.species_bloodlust_all_food.SpeciesBloodlustAllFood;
import net.xlea99.lifeguard.entry.species_ghoul_forgets_prey.GhoulPrey;
import net.xlea99.lifeguard.entry.species_ghoul_forgets_prey.SpeciesGhoulForgetsPrey;
import net.xlea99.lifeguard.entry.species_stackatick_breeding.SpeciesStackatickBreeding;
import net.xlea99.lifeguard.entry.species_stackatick_breeding.StackatickBreeding;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.Chaindeliers;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.SpeciesDyeableChaindeliers;
import net.xlea99.lifeguard.entry.species_soap_washes_stackaticks.StackatickSoap;
import net.xlea99.lifeguard.entry.species_soap_washes_stackaticks.SpeciesSoapWashesStackaticks;
import net.xlea99.lifeguard.entry.cc_crafted_tether_potions.CraftedTetherPotions;
import net.xlea99.lifeguard.block.PlatedBricks;
import net.xlea99.lifeguard.entry.extended_metal_bars.Bars;
import net.xlea99.lifeguard.magnet.Magnets;
import net.xlea99.lifeguard.boot.Boot;
import net.xlea99.lifeguard.data.DataConditions;
import net.xlea99.lifeguard.entry.ac_infinite_soda.AcInfiniteSoda;
import net.xlea99.lifeguard.entry.ac_infinite_soda.InfiniteSoda;
import net.xlea99.lifeguard.entry.am_lava_vision_fallback.AmLavaVisionFallback;
import net.xlea99.lifeguard.entry.am_lava_vision_fallback.LavaVision;
import net.xlea99.lifeguard.entry.amendments_cake_slicing.AmendmentsCakeSlicing;
import net.xlea99.lifeguard.entry.amendments_cake_slicing.CakeSlicing;
import net.xlea99.lifeguard.entry.cauldron_fourths.CauldronFourths;
import net.xlea99.lifeguard.entry.cauldron_fourths.FourLevelInteractions;
import net.xlea99.lifeguard.entry.cc_cannon_backfire.CannonBackfire;
import net.xlea99.lifeguard.entry.cc_cannon_backfire.CcCannonBackfire;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPots;
import net.xlea99.lifeguard.entry.dye_depot_rock_candy.DyeDepotRockCandy;
import net.xlea99.lifeguard.entry.dye_depot_rock_candy.RockCandies;
import net.xlea99.lifeguard.entry.dye_depot_sparklers.DyeDepotSparklers;
import net.xlea99.lifeguard.entry.dye_depot_sparklers.Sparklers;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.FaucetPouringRecipes;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.HauntedHarvestPumpkinCompat;
import net.xlea99.lifeguard.entry.haunted_harvest_pumpkin_compat.Pumpkins;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.CarvingPools;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.HauntedHarvestUnifiedCarvedPumpkin;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.UnifiedPumpkin;
import net.xlea99.lifeguard.entry.snowy_spirit_edible_treats.SnowySpiritEdibleTreats;
import net.xlea99.lifeguard.entry.snowy_spirit_edible_treats.Treats;
import net.xlea99.lifeguard.entry.snowy_spirit_eggnog_nerf.EggnogNerf;
import net.xlea99.lifeguard.entry.snowy_spirit_eggnog_nerf.SnowySpiritEggnogNerf;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.PouringRecipesPayload;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSync;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSyncPayload;
import net.xlea99.lifeguard.entry.supps_jukebox_speakers.JukeboxSpeakers;
import net.xlea99.lifeguard.entry.supps_jukebox_speakers.SuppsJukeboxSpeakers;
import net.xlea99.lifeguard.entry.jukebox_synced_playback.JukeboxSyncedPlayback;
import net.xlea99.lifeguard.entry.faucet_pouring_recipes.PouringRecipes;
import net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing.ParrotEggDispensers;
import net.xlea99.lifeguard.entry.quark_parrot_egg_dispensing.QuarkParrotEggDispensing;
import net.xlea99.lifeguard.entry.twigs_pebble_dispensing.PebbleDispensers;
import net.xlea99.lifeguard.entry.twigs_pebble_dispensing.TwigsPebbleDispensing;
import net.xlea99.lifeguard.entry.supps_respects_mob_griefing.MobFiredCannonballs;
import net.xlea99.lifeguard.entry.supps_respects_mob_griefing.SuppsRespectsMobGriefing;
import net.xlea99.lifeguard.sync.Sync;
import org.slf4j.Logger;

@Mod(Lifeguard.MODID)
public class Lifeguard {
    public static final String MODID = "lifeguard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lifeguard(IEventBus modEventBus, ModContainer modContainer) {
        // The mixin plugin normally got here first; this only matters if it didn't.
        Boot.start();

        DataConditions.register(modEventBus);
        if (Boidsify.ENTRY.active())
            Boids.register();
        if (QuarkParrotEggDispensing.ENTRY.active())
            ParrotEggDispensers.register(modEventBus);
        if (TwigsPebbleDispensing.ENTRY.active())
            PebbleDispensers.register(modEventBus);
        if (SuppsRespectsMobGriefing.ENTRY.active())
            MobFiredCannonballs.register();
        if (CcCannonBackfire.ENTRY.active())
            CannonBackfire.register();
        if (FaucetPouringRecipes.ENTRY.active())
            PouringRecipes.register();
        // Registered whenever both mods are there, never by the switch: a world may hold the candies.
        if (DyeDepotRockCandy.ENTRY.installed())
            RockCandies.register(modEventBus);
        if (DyeDepotRockCandy.ENTRY.active())
            RockCandies.registerTab(modEventBus);
        if (AcInfiniteSoda.ENTRY.active())
            InfiniteSoda.register();
        if (AmLavaVisionFallback.ENTRY.active())
            LavaVision.register();
        if (AmendmentsCakeSlicing.ENTRY.active())
            CakeSlicing.register();
        if (CauldronFourths.ENTRY.active())
            FourLevelInteractions.register(modEventBus);
        // Runtime-gated: registered whenever both mods are there (a world may hold the sparklers); the listeners check the switch.
        if (DyeDepotSparklers.ENTRY.installed())
            Sparklers.register(modEventBus);
        // Registered whenever Clayworks is there, never by the switch: placed pots keep their looks while it's off.
        if (ClayworksFlowerPots.ENTRY.installed())
            FlowerPots.register(modEventBus);
        // Registered whenever each metal's entry (or, for bars, its mod) is there, never by the switch: a world may hold
        // them. The magnets join Alex's Caves' after every block that is one has said so.
        // Runtime-gated: the recipe type whenever C&C is there; the recipe file and the brewing cut ask the switch.
        if (CcCraftedTetherPotions.ENTRY.installed())
            CraftedTetherPotions.register(modEventBus);
        PlatedBricks.register(modEventBus);
        Bars.register(modEventBus);
        Magnets.register(modEventBus);
        if (CcStorageDuctSpanLimit.ENTRY.active())
            StorageDucts.registerPlaceWarning();
        if (CcFlintPistonStriking.ENTRY.active())
            FlintStriking.register();
        if (SnowySpiritEdibleTreats.ENTRY.active())
            Treats.register(modEventBus);
        if (SnowySpiritEggnogNerf.ENTRY.active())
            EggnogNerf.register(modEventBus);
        if (SpeciesGhoulForgetsPrey.ENTRY.active())
            GhoulPrey.register();
        // Runtime-gated: registered whenever Species is there; the listeners ask the switch (both sides handle these).
        if (SpeciesBloodlustAllFood.ENTRY.installed())
            BloodlustFood.register();
        if (SpeciesStackatickBreeding.ENTRY.installed())
            StackatickBreeding.register();
        if (SpeciesSoapWashesStackaticks.ENTRY.installed())
            StackatickSoap.register();
        if (SpeciesDyeableChaindeliers.ENTRY.installed())
            Chaindeliers.register(modEventBus);
        modEventBus.addListener(Sync::registerPayloads);
        if (HauntedHarvestPumpkinCompat.ENTRY.active())
            Pumpkins.register(modEventBus);
        // Runtime-gated: the listeners check the switch, so a server can turn it on or off for its world.
        if (HauntedHarvestUnifiedCarvedPumpkin.ENTRY.installed()) {
            UnifiedPumpkin.register();
            CarvingPools.register();
        }
        if (FaucetPouringRecipes.ENTRY.installed())
            modEventBus.addListener(PouringRecipesPayload::register);
        if (JukeboxSyncedPlayback.ENTRY.installed())
            modEventBus.addListener(JukeboxSyncPayload::register);
        if (SuppsJukeboxSpeakers.ENTRY.installed())
            JukeboxSync.addCarrier(JukeboxSpeakers::carry);
        modEventBus.addListener(Sync::registerTasks);
        NeoForge.EVENT_BUS.addListener(Sync::onServerAboutToStart);
    }
}
