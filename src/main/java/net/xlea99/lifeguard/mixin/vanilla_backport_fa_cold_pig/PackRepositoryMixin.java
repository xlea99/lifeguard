package net.xlea99.lifeguard.mixin.vanilla_backport_fa_cold_pig;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.xlea99.lifeguard.entry.vanilla_backport_fa_cold_pig.FaColdPigPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps {@link FaColdPigPack} on the very top. Packs fixed on top stack in the order they're found, earlier ones higher, and Vanilla
 * Backport's is found before Lifeguard's, so asking for the top isn't enough to beat its cold pig.
 */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @ModifyReturnValue(method = "rebuildSelected", at = @At("RETURN"))
    private List<Pack> lifeguard$coldPigOnTop(List<Pack> packs) {
        for (int i = 0; i < packs.size() - 1; i++) {
            if (packs.get(i).getId().equals(FaColdPigPack.PACK_ID)) {
                List<Pack> moved = new ArrayList<>(packs);
                moved.add(moved.remove(i));
                return moved;
            }
        }
        return packs;
    }
}
