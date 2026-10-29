package net.xlea99.lifeguard;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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
        if (AcInfiniteSoda.ENTRY.active())
            InfiniteSoda.register();
        if (AmLavaVisionFallback.ENTRY.active())
            LavaVision.register();
        if (AmendmentsCakeSlicing.ENTRY.active())
            CakeSlicing.register();
        if (CauldronFourths.ENTRY.active())
            FourLevelInteractions.register(modEventBus);
        // Registered whenever Clayworks is there, never by the switch: placed pots keep their looks while it's off.
        if (ClayworksFlowerPots.ENTRY.installed())
            FlowerPots.register(modEventBus);
        if (SnowySpiritEdibleTreats.ENTRY.active())
            Treats.register(modEventBus);
        if (SnowySpiritEggnogNerf.ENTRY.active())
            EggnogNerf.register(modEventBus);
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
        modEventBus.addListener(Sync::registerTasks);
        NeoForge.EVENT_BUS.addListener(Sync::onServerAboutToStart);
    }
}
