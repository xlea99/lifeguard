package net.xlea99.lifeguard.mixin.cc_hidden_trim_icons;

import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caverns & Chasms adds its trim materials' outlines to every armor trim template's material slot (its own
 * SmithingTemplateItemMixin, at item creation); a smithing screen then cycles through them. This leaves out turquoise's
 * and zirconia's, for a pack that cut them as trim materials.
 */
@Mixin(SmithingTemplateItem.class)
public abstract class SmithingTemplateItemMixin {
    @Unique
    private static final Set<ResourceLocation> lifeguard$HIDDEN = Set.of(
        ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "item/empty_slot_turquoise"),
        ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "item/empty_slot_zirconia"));

    @Inject(method = "getAdditionalSlotEmptyIcons", at = @At("RETURN"), cancellable = true)
    private void lifeguard$hideCutTrimIcons(CallbackInfoReturnable<List<ResourceLocation>> cir) {
        List<ResourceLocation> icons = cir.getReturnValue();
        if (icons.stream().anyMatch(lifeguard$HIDDEN::contains))
            cir.setReturnValue(icons.stream().filter(icon -> !lifeguard$HIDDEN.contains(icon)).toList());
    }
}
