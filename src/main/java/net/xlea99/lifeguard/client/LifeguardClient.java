package net.xlea99.lifeguard.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.resources.ResourceLocation;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.client.menu.LifeguardScreen;
import net.xlea99.lifeguard.entry.ac_thornwood_very_slightly_lighter_planks.AcThornwoodVerySlightlyLighterPlanks;
import net.xlea99.lifeguard.entry.cauldron_fourths.CauldronFourths;
import net.xlea99.lifeguard.entry.cauldron_fourths.FourthsClient;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.FlowerPotsClient;
import net.xlea99.lifeguard.entry.dye_depot_sparklers.DyeDepotSparklers;
import net.xlea99.lifeguard.entry.dye_depot_sparklers.SparklersClient;
import net.xlea99.lifeguard.entry.sexy_runic_glints.GlintsClient;
import net.xlea99.lifeguard.entry.haunted_harvest_mask_limit.HauntedHarvestMaskLimit;
import net.xlea99.lifeguard.entry.haunted_harvest_mask_limit.HollowPumpkins;
import net.xlea99.lifeguard.entry.sexy_runic_glints.SexyRunicGlints;
import net.xlea99.lifeguard.entry.dye_the_world_glow_lights.DyeTheWorldGlowLights;
import net.xlea99.lifeguard.entry.dye_the_world_glow_lights.GlowLightsPack;
import net.xlea99.lifeguard.entry.dye_the_world_open_sacks.DyeTheWorldOpenSacks;
import net.xlea99.lifeguard.entry.dye_the_world_open_sacks.OpenSacks;
import net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims.EtceteraShimmeringBismuthTrims;
import net.xlea99.lifeguard.entry.etcetera_shimmering_bismuth_trims.ShimmerTrims;
import net.xlea99.lifeguard.entry.snowy_spirit_brighter_ginger.SnowySpiritBrighterGinger;
import net.xlea99.lifeguard.entry.snowy_spirit_edible_treats.GumdropCrumbs;
import net.xlea99.lifeguard.entry.snowy_spirit_edible_treats.SnowySpiritEdibleTreats;
import net.xlea99.lifeguard.entry.spawn_ru_pineify_rotten_wood.PineRotPack;
import net.xlea99.lifeguard.entry.spawn_ru_pineify_rotten_wood.SpawnRuPineifyRottenWood;
import net.xlea99.lifeguard.entry.whiter_labels.WhiterLabels;
import net.xlea99.lifeguard.entry.whiter_labels.WhiterPaper;

/**
 * Lifeguard's client-only entry point: the menu, opened from the Config button in NeoForge's Mods list, and client setup
 * for the active entries that need it.
 */
@Mod(value = Lifeguard.MODID, dist = Dist.CLIENT)
public final class LifeguardClient {
    public LifeguardClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new LifeguardScreen(parent));
        EntryPacks.register(modEventBus, container);
        ClientChecks.register(modEventBus);
        RecolorPack.register(modEventBus);
        if (CauldronFourths.ENTRY.active())
            FourthsClient.register();
        if (SexyRunicGlints.ENTRY.active())
            GlintsClient.register(modEventBus);
        if (DyeDepotSparklers.ENTRY.installed())
            SparklersClient.register(modEventBus);
        if (ClayworksFlowerPots.ENTRY.installed())
            FlowerPotsClient.register(modEventBus);
        if (WhiterLabels.ENTRY.active())
            WhiterPaper.register(modEventBus);
        if (SnowySpiritEdibleTreats.ENTRY.active())
            GumdropCrumbs.register(modEventBus);
        if (SnowySpiritBrighterGinger.ENTRY.active())
            SpecRecolors.register(SnowySpiritBrighterGinger.ENTRY.id(), recolors("snowy_spirit_ginger"), "snowyspirit");
        if (AcThornwoodVerySlightlyLighterPlanks.ENTRY.active())
            SpecRecolors.register(AcThornwoodVerySlightlyLighterPlanks.ENTRY.id(), recolors("ac_thornwood"), "alexscaves");
        if (SpawnRuPineifyRottenWood.ENTRY.active())
            PineRotPack.register(modEventBus);
        if (DyeTheWorldGlowLights.ENTRY.active())
            GlowLightsPack.register();
        if (DyeTheWorldOpenSacks.ENTRY.active())
            OpenSacks.register(modEventBus);
        if (EtceteraShimmeringBismuthTrims.ENTRY.active())
            ShimmerTrims.register(modEventBus);
        // The server owns this one: the tooltip checks the synced value each time.
        if (HauntedHarvestMaskLimit.ENTRY.installed())
            HollowPumpkins.registerTooltip();
    }

    /** A {@link SpecRecolors} spec: {@code assets/lifeguard/recolors/<name>.json}. */
    private static ResourceLocation recolors(String name) {
        return ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "recolors/" + name + ".json");
    }
}
