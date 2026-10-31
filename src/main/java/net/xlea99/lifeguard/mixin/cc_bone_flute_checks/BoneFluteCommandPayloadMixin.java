package net.xlea99.lifeguard.mixin.cc_bone_flute_checks;

import com.teamabnormals.caverns_and_chasms.common.network.bone_flute.BoneFluteCommandPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xlea99.lifeguard.entry.cc_bone_flute_checks.BoneFluteCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The server only takes a Bone Flute command from a player holding a flute, about once per use ({@link BoneFluteCommands}).
 * Every flute payload's handler goes through this interface's {@code handleCommand}, whose queued work (the lambda, on
 * the server thread) plays the flute's sound and runs the command; hooked there, a refused command plays no sound either.
 */
@Mixin(BoneFluteCommandPayload.class)
public interface BoneFluteCommandPayloadMixin {
    @Inject(method = "lambda$handleCommand$0", at = @At("HEAD"), cancellable = true)
    private void lifeguard$onlyFromAFlute(IPayloadContext context, CallbackInfo ci) {
        if (!BoneFluteCommands.mayCommand(context.player()))
            ci.cancel();
    }
}
