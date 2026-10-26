package net.xlea99.lifeguard.mixin.no_end_cave_sounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * No cave mood sounds in the End.
 *
 * <p>Vanilla's mood builds up wherever the blocks around you get no sky light, and plays the biome's mood sound when
 * it fills. Every End biome uses the Overworld's cave sound, and the End has no sky light anywhere, so the whole
 * dimension counts as underground: a cave noise every few minutes on open islands. The biome's mood settings are read
 * once per biome change; in the End, plain {@code ambient.cave} now reads as none. A modded End biome with its own mood
 * sound keeps it.
 */
@Mixin(BiomeAmbientSoundsHandler.class)
public abstract class BiomeAmbientSoundsHandlerMixin {
    @Shadow
    @Final
    private LocalPlayer player;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/biome/Biome;getAmbientMood()Ljava/util/Optional;"))
    private Optional<AmbientMoodSettings> lifeguard$noEndCaveMood(Biome biome, Operation<Optional<AmbientMoodSettings>> original) {
        Optional<AmbientMoodSettings> mood = original.call(biome);
        if (this.player.level().dimension() == Level.END && mood.isPresent()
            && mood.get().getSoundEvent().value() == SoundEvents.AMBIENT_CAVE.value())
            return Optional.empty();
        return mood;
    }
}
