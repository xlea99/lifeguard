package net.xlea99.lifeguard.entry.species_soap_washes_stackaticks;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.xlea99.lifeguard.mixin.species_soap_washes_stackaticks.SoapItemAccessor;
import net.xlea99.lifeguard.mixin.species_soap_washes_stackaticks.StackatickAccessor;

/**
 * Supplementaries' soap washes the dye off Species' Stackaticks, instead of a water bucket ({@link SpeciesSoapWashesStackaticks}).
 *
 * <p>Soap is what washes dye off things once Supplementaries is installed (sheep, dyed blocks), but on a Stackatick it
 * only did what it does to any pet: its owner's got made to sit and the soap was used up, the dye left on. Species washes
 * a Stackatick with a sneaking right-click of a water bucket. Soap now washes it, with soap's own sound and bubbles, for
 * whoever Species lets dye it (its owner, or anyone while it's wild), and the water bucket no longer does: it acts on a
 * dyed Stackatick as on any other. An undyed Stackatick takes soap as it did. Ahead of Supplementaries' own handling
 * (its listener is LOW priority), which would make the Stackatick sit. The Stackatick is found by its id; its dye is
 * reached through {@link StackatickAccessor}, by class name.
 */
public final class StackatickSoap {
    private static final ResourceLocation STACKATICK = ResourceLocation.fromNamespaceAndPath("species", "stackatick");

    private static boolean looked;
    private static EntityType<?> stackatickType;

    private StackatickSoap() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, StackatickSoap::interacting);
    }

    private static boolean isStackatick(TamableAnimal animal) {
        if (!looked) {
            looked = true;
            stackatickType = BuiltInRegistries.ENTITY_TYPE.getOptional(STACKATICK).orElse(null);
        }
        return stackatickType != null && animal.getType() == stackatickType;
    }

    private static void interacting(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof TamableAnimal stackatick) || !isStackatick(stackatick) || !SpeciesSoapWashesStackaticks.ENTRY.active())
            return;
        Player player = event.getEntity();
        StackatickAccessor dye = (StackatickAccessor) stackatick;
        // Who Species lets dye a Stackatick.
        if (stackatick.isTame() && !stackatick.isOwnedBy(player) || !dye.lifeguard$isDyed())
            return;
        ItemStack stack = event.getItemStack();
        if (stack.is(ModRegistry.SOAP.get())) {
            if (!event.getLevel().isClientSide())
                dye.lifeguard$setDyed(false);
            SoapItemAccessor.lifeguard$playEffectsAndConsume(stack, player, stackatick);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        } else if (stack.is(Items.WATER_BUCKET) && player.isSecondaryUseActive()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }
}
