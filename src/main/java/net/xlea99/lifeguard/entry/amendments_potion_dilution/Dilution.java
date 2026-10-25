package net.xlea99.lifeguard.entry.amendments_potion_dilution;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Honest dilution of instant effects when potions are mixed in an Amendments cauldron.
 *
 * <p>Amendments shares a mixed potion's durations out fairly between the bottles, but an instant effect has no
 * duration, so it scales the level instead and rounds down, never below level I: one Instant Health potion and three
 * Awkward Potions made four Instant Health potions, and again with more Awkward Potions. Here an instant effect counts
 * as doses instead, a level II potion being two doses of level I (exactly true for every brewable one), and each bottle
 * gets its share. Whole doses stay instant; a leftover fraction fizzles out, or with {@link
 * AmendmentsPotionDilution#LEFTOVERS} becomes Regeneration (from Instant Health) or Poison (from Harming, which can't
 * kill), healing or hurting the same amount. A bottle left with no effect at all is an Awkward Potion again.
 */
public final class Dilution {
    /** Ticks of level I Regeneration that heal as much as one dose of Instant Health (4 HP, 1 HP per 50 ticks). */
    private static final int REGEN_TICKS_PER_DOSE = 200;
    /** Ticks of level I Poison that hurt as much as one dose of Instant Damage (6 HP, 1 HP per 25 ticks). */
    private static final int POISON_TICKS_PER_DOSE = 150;

    private Dilution() {
    }

    /**
     * Redoes the instant effects of Amendments' mix of {@code tank} and {@code added} ({@code mixed}, null when it left
     * the tank alone). Durations are Amendments' own.
     */
    @Nullable
    public static SoftFluidStack dilute(@Nullable SoftFluidStack mixed, SoftFluidStack tank, SoftFluidStack added) {
        if (mixed == null)
            return null;
        Map<Holder<MobEffect>, Integer> doses = new LinkedHashMap<>();
        countDoses(doses, tank);
        countDoses(doses, added);
        if (doses.isEmpty())
            return mixed;
        Map<Holder<MobEffect>, MobEffectInstance> effects = new LinkedHashMap<>();
        for (MobEffectInstance effect : mixed.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects())
            if (!effect.getEffect().value().isInstantenous())
                effects.put(effect.getEffect(), effect);
        int bottles = tank.getCount() + added.getCount();
        boolean leftovers = AmendmentsPotionDilution.LEFTOVERS.active();
        doses.forEach((effect, total) -> {
            int whole = total / bottles;
            double leftover = (double) (total % bottles) / bottles;
            if (whole > 0)
                effects.put(effect, new MobEffectInstance(effect, 1, whole - 1));
            if (leftover <= 0 || !leftovers)
                return;
            if (effect.value() == MobEffects.HEAL.value())
                addSlowly(effects, MobEffects.REGENERATION, leftover * REGEN_TICKS_PER_DOSE);
            else if (effect.value() == MobEffects.HARM.value())
                addSlowly(effects, MobEffects.POISON, leftover * POISON_TICKS_PER_DOSE);
        });
        mixed.set(DataComponents.POTION_CONTENTS, effects.isEmpty() ? new PotionContents(Potions.AWKWARD)
            : new PotionContents(Optional.empty(), Optional.of(PotionContents.getColor(effects.values())),
                List.copyOf(effects.values())));
        return mixed;
    }

    /** Adds each instant effect's doses for this many bottles: level I is one dose per bottle, level II two. */
    private static void countDoses(Map<Holder<MobEffect>, Integer> doses, SoftFluidStack fluid) {
        for (MobEffectInstance effect : fluid.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects())
            if (effect.getEffect().value().isInstantenous())
                doses.merge(effect.getEffect(), (effect.getAmplifier() + 1) * fluid.getCount(), Integer::sum);
    }

    /**
     * Adds this many ticks' worth of a level I effect, as more of the same effect if the potion already has it (at its
     * level, which heals or hurts faster, so fewer ticks).
     */
    private static void addSlowly(Map<Holder<MobEffect>, MobEffectInstance> effects, Holder<MobEffect> effect,
                                  double levelOneTicks) {
        MobEffectInstance existing = effects.get(effect);
        int amplifier = existing == null ? 0 : existing.getAmplifier();
        int ticks = Math.max(1, (int) Math.round(levelOneTicks / (1 << amplifier)));
        effects.put(effect, existing == null ? new MobEffectInstance(effect, ticks, 0)
            : new MobEffectInstance(effect, existing.getDuration() + ticks, amplifier, existing.isAmbient(),
                existing.isVisible(), existing.showIcon()));
    }
}
