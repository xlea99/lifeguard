package net.xlea99.lifeguard.mixin.whiter_labels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.labels.ColorManager;
import net.minecraft.world.item.DyeColor;
import net.xlea99.lifeguard.entry.whiter_labels.WhiterPaper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** An undyed label's drawing in shades that sit on whiter paper ({@link WhiterPaper#undyed}). */
@Mixin(ColorManager.class)
public abstract class ColorManagerMixin {

    @ModifyReturnValue(method = "getLight", at = @At("RETURN"))
    private static int lifeguard$whiterLight(int light, @Local(argsOnly = true) @Nullable DyeColor color) {
        return color == null ? WhiterPaper.undyed(light, true) : light;
    }

    @ModifyReturnValue(method = "getDark", at = @At("RETURN"))
    private static int lifeguard$whiterDark(int dark, @Local(argsOnly = true) @Nullable DyeColor color) {
        return color == null ? WhiterPaper.undyed(dark, false) : dark;
    }
}
