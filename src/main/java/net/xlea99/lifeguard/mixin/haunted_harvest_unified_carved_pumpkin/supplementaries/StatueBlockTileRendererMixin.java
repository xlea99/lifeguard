package net.xlea99.lifeguard.mixin.haunted_harvest_unified_carved_pumpkin.supplementaries;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.StatueBlockTileRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin.UnifiedPumpkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Supplementaries' statues dress up on Halloween in Haunted Harvest's pumpkin and jack o'lantern, vanilla's face ({@link UnifiedPumpkin}). */
@Mixin(StatueBlockTileRenderer.class)
public abstract class StatueBlockTileRendererMixin {
    @Unique
    private static ItemStack lifeguard$carved;
    @Unique
    private static ItemStack lifeguard$lit;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/Item;getDefaultInstance()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack lifeguard$hauntedCostume(ItemStack costume) {
        if (!UnifiedPumpkin.isVanilla(costume) || !UnifiedPumpkin.active())
            return costume;
        if (costume.is(Items.JACK_O_LANTERN)) {
            if (lifeguard$lit == null)
                lifeguard$lit = UnifiedPumpkin.hauntedItem(costume, UnifiedPumpkin.VANILLA_FACE);
            return lifeguard$lit;
        }
        if (lifeguard$carved == null)
            lifeguard$carved = UnifiedPumpkin.hauntedItem(costume, UnifiedPumpkin.VANILLA_FACE);
        return lifeguard$carved;
    }
}
