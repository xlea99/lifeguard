package net.xlea99.lifeguard.mixin.cc_weapons_follow_the_blade;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.teamabnormals.caverns_and_chasms.core.other.CCEvents;
import com.teamabnormals.caverns_and_chasms.core.registry.CCAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Caverns & Chasms' weapon perks only follow the weapon's own swing. Silver tools add magic damage and necromium tools
 * Slowness, both as attributes of the main-hand item, which C&C reads off the attacker after any damage they cause
 * (CCEvents.onLivingDamagePost). So a silver sword in the main hand put its magic damage on every arrow from a bow in
 * the offhand (#36). Here those two only apply when the attacker hit directly: a melee swing, not a projectile. The
 * armor perks in the same handler (sanguine lifesteal, necromium's Slowness on whoever hurt the wearer) are about the
 * wearer, however the damage was dealt, as vanilla's Thorns is, and stay as they are.
 */
@Mixin(CCEvents.class)
public abstract class CCEventsMixin {
    @WrapOperation(method = "onLivingDamagePost", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;getAttribute(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;"))
    private static AttributeInstance lifeguard$weaponPerksOnlyOnDirectHits(LivingEntity entity, Holder<Attribute> attribute,
                                                                         Operation<AttributeInstance> original,
                                                                         @Local(argsOnly = true) LivingDamageEvent.Post event) {
        boolean weaponPerk = attribute.is(CCAttributes.MAGIC_DAMAGE.getKey()) || attribute.is(CCAttributes.SLOWNESS_INFLICTION.getKey());
        if (weaponPerk && !event.getSource().isDirect())
            return null;
        return original.call(entity, attribute);
    }
}
