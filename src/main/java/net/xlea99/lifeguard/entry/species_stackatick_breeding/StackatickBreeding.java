package net.xlea99.lifeguard.entry.species_stackatick_breeding;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Species' Stackaticks breed when fed their breeding food ({@link SpeciesStackatickBreeding}, #21).
 *
 * <p>A Stackatick's own right-click handling comes before breeding ({@code Stackatick.mobInteract}): right-clicked, it
 * lets the player ride it, and sneak-clicked by its owner, it sits or stands. So its breeding food made a tamed one carry
 * its owner or sit, never breed, and a wild one bred only for a sneaking player, all with the sound of it eating. Its
 * breeding food now goes to breeding first, as with any animal: an adult ready to breed eats it and falls in love, and
 * one that isn't ready (just bred, or already in love) leaves it be and doesn't take a rider either. Babies are left to
 * Species, which already lets them eat it to grow. Species isn't compiled against: the Stackatick and its eating sound
 * are found by their ids.
 */
public final class StackatickBreeding {
    private static final ResourceLocation STACKATICK = ResourceLocation.fromNamespaceAndPath("species", "stackatick");
    private static final ResourceLocation EAT = ResourceLocation.fromNamespaceAndPath("species", "entity.stackatick.eat");

    private static boolean looked;
    private static EntityType<?> stackatickType;

    private StackatickBreeding() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, StackatickBreeding::interacting);
    }

    private static boolean isStackatick(Animal animal) {
        if (!looked) {
            looked = true;
            stackatickType = BuiltInRegistries.ENTITY_TYPE.getOptional(STACKATICK).orElse(null);
        }
        return stackatickType != null && animal.getType() == stackatickType;
    }

    private static void interacting(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Animal stackatick) || !isStackatick(stackatick) || !SpeciesStackatickBreeding.ENTRY.active())
            return;
        ItemStack stack = event.getItemStack();
        if (stackatick.isBaby() || !stackatick.isFood(stack))
            return;
        event.setCanceled(true);
        // As vanilla's breeding: the client can't tell whether the animal is ready, so it waits for the server.
        event.setCancellationResult(InteractionResult.CONSUME);
        if (event.getLevel().isClientSide() || stackatick.getAge() != 0 || !stackatick.canFallInLove())
            return;
        Player player = event.getEntity();
        if (!player.getAbilities().instabuild)
            stack.shrink(1);
        stackatick.setInLove(player);
        SoundEvent eat = BuiltInRegistries.SOUND_EVENT.get(EAT);
        if (eat != null && !stackatick.isSilent())
            stackatick.level().playSound(null, stackatick.getX(), stackatick.getY(), stackatick.getZ(), eat, stackatick.getSoundSource(), 1.0F,
                1.0F + (stackatick.getRandom().nextFloat() - stackatick.getRandom().nextFloat()) * 0.2F);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
