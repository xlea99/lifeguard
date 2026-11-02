package net.xlea99.lifeguard.entry.scholar_default_packs;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Saves the options once when Scholar has just turned its packs on. Scholar enables its built-in packs itself, once: as
 * the options load (its OptionsMixin.onLoad) it adds them to the enabled packs and records in
 * {@code data/scholar_loaded_built_in_resource_packs.dat} that it has. Nothing saves {@code options.txt} then (the game
 * only writes it when an option changes), so unless the player changed one that first session, the packs were gone the
 * next launch, and Scholar, having done its once, never added them again: dyed books lost their colors. Proven on
 * Scholar 1.2.10 with a fresh tracker: on, then gone (2026-10-31). Here, once loading is done, if a Scholar pack is
 * enabled but missing from {@code options.txt}, the options are saved. That only keeps what Scholar just added: a pack
 * the player turned off is in neither, and isn't touched.
 */
public final class UnsavedPacks {
    private static final String PACK_PREFIX = "scholar:";

    private UnsavedPacks() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((FMLLoadCompleteEvent event) -> event.enqueueWork(UnsavedPacks::saveIfScholarAdded));
    }

    private static void saveIfScholarAdded() {
        Minecraft minecraft = Minecraft.getInstance();
        Options options = minecraft.options;
        List<String> saved = savedResourcePacks(minecraft.gameDirectory.toPath().resolve("options.txt"));
        if (options.resourcePacks.stream().anyMatch(pack -> pack.startsWith(PACK_PREFIX) && !saved.contains(pack))) {
            Lifeguard.LOGGER.info("Lifeguard: saving options so Scholar's default resource packs stay enabled");
            options.save();
        }
    }

    /** The enabled packs as {@code options.txt} has them (empty if it can't be read). */
    private static List<String> savedResourcePacks(Path file) {
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8))
                if (line.startsWith("resourcePacks:"))
                    return JsonParser.parseString(line.substring("resourcePacks:".length())).getAsJsonArray().asList().stream()
                        .map(pack -> pack.getAsString()).toList();
        } catch (IOException | RuntimeException e) {
            Lifeguard.LOGGER.warn("Lifeguard: couldn't read {} to check Scholar's resource packs", file, e);
        }
        return List.of();
    }
}
