package net.xlea99.lifeguard.entry.species_bloodlust_all_food;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * Species' Bloodlust keeps every food away, not just plain ones ({@link SpeciesBloodlustAllFood}, #18).
 *
 * <p>Bloodlust stops eating by cancelling the plain item's use (Species' ItemMixin, at the head of {@code Item#use}), so
 * any food with a use of its own got past it: drinks from Farmer's Delight, Neapolitan, Alex's Caves, Snowy Spirit and
 * others. Here anything that would fill hunger or saturation can't be started at all while Bloodlust lasts, from any
 * mod, with Species' own refusal (its message and short cooldown). Potions, milk and anything else without food values
 * stay drinkable, so milk still cures it. Species isn't compiled against: Bloodlust is found by its id.
 */
public final class BloodlustFood {
    private static final ResourceKey<MobEffect> BLOODLUST = ResourceKey.create(Registries.MOB_EFFECT,
        ResourceLocation.fromNamespaceAndPath("species", "bloodlust"));
    /** As long as Species holds an item back after refusing it. */
    private static final int REFUSED_COOLDOWN = 60;

    private static boolean looked;
    private static Holder<MobEffect> bloodlust;

    private BloodlustFood() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Start.class, BloodlustFood::starting);
    }

    private static Holder<MobEffect> bloodlust() {
        if (!looked) {
            looked = true;
            bloodlust = BuiltInRegistries.MOB_EFFECT.getHolder(BLOODLUST).orElse(null);
        }
        return bloodlust;
    }

    private static void starting(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player) || !SpeciesBloodlustAllFood.ENTRY.active())
            return;
        Holder<MobEffect> effect = bloodlust();
        ItemStack stack = event.getItem();
        if (effect == null || !player.hasEffect(effect) || !fills(stack, player))
            return;
        event.setCanceled(true);
        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("effect.species.bloodlust.reason").withStyle(ChatFormatting.DARK_RED), true);
            player.getCooldowns().addCooldown(stack.getItem(), REFUSED_COOLDOWN);
        }
    }

    private static boolean fills(ItemStack stack, Player player) {
        FoodProperties food = stack.getFoodProperties(player);
        return food != null && (food.nutrition() > 0 || food.saturation() > 0);
    }
}
