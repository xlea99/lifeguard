package net.xlea99.lifeguard.entry.am_lava_vision_fallback;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.xlea99.lifeguard.Lifeguard;
import net.xlea99.lifeguard.boot.BrokenEntries;
import net.xlea99.lifeguard.mixin.am_lava_vision_fallback.PotionBrewingBuilderAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Keeps Alex's Mobs' Potion of Lava Vision brewable when its lava bottles are off. Lava Vision brews from a Lava Bottle
 * (the bottle itself is the base) and a Bone Serpent Tooth. With Alex's Mobs' {@code lavaBottleEnabled = false} a glass
 * bottle can't be filled with lava any more, so the recipe can never be used: the potion is gone. So with lava bottles
 * off, the dead recipe is removed and Lava Vision brews from a Potion of Night Vision instead: see in the dark first,
 * then through lava. Alex's Mobs' own redstone step still lengthens it. Runs after Alex's Mobs has added its recipes;
 * reads Alex's Mobs only by name. Only loaded when the entry is active.
 */
public final class LavaVision {

    private LavaVision() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, LavaVision::onRegisterBrewingRecipes);
    }

    private static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        if (!Boolean.FALSE.equals(lavaBottlesEnabled()))
            return;
        Item lavaBottle = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("alexsmobs", "lava_bottle"));
        Item tooth = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("alexsmobs", "bone_serpent_tooth"));
        Optional<Holder.Reference<Potion>> lavaVision = potion(event, "lava_vision");
        Optional<Holder.Reference<Potion>> longLavaVision = potion(event, "long_lava_vision");
        if (lavaBottle == Items.AIR || tooth == Items.AIR || lavaVision.isEmpty() || longLavaVision.isEmpty()) {
            Lifeguard.LOGGER.warn("Couldn't find Alex's Mobs' Lava Vision recipe parts; leaving Lava Vision as it is");
            return;
        }
        PotionBrewing.Builder builder = event.getBuilder();
        ItemStack bottleStack = new ItemStack(lavaBottle);
        ItemStack toothStack = new ItemStack(tooth);
        ((PotionBrewingBuilderAccessor) builder).lifeguard$recipes()
            .removeIf(recipe -> recipe.isInput(bottleStack) && recipe.isIngredient(toothStack));
        builder.addMix(Potions.NIGHT_VISION, tooth, lavaVision.get());
        // Long into long, 8:00 both, so lengthening before or after the tooth comes out the same.
        builder.addMix(Potions.LONG_NIGHT_VISION, tooth, longLavaVision.get());
    }

    private static Optional<Holder.Reference<Potion>> potion(RegisterBrewingRecipesEvent event, String name) {
        return event.getRegistryAccess().registryOrThrow(Registries.POTION)
            .getHolder(ResourceKey.create(Registries.POTION, ResourceLocation.fromNamespaceAndPath("alexsmobs", name)));
    }

    /** Alex's Mobs' {@code lavaBottleEnabled}, or null if it can't be read. */
    @Nullable
    private static Boolean lavaBottlesEnabled() {
        try {
            return Class.forName("com.github.alexthe666.alexsmobs.config.AMConfig").getField("lavaBottleEnabled").getBoolean(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            BrokenEntries.mark(AmLavaVisionFallback.ENTRY, "Alex's Mobs' AMConfig.lavaBottleEnabled can't be read ("
                + e + "), so it can't tell whether lava bottles are on");
            return null;
        }
    }
}
