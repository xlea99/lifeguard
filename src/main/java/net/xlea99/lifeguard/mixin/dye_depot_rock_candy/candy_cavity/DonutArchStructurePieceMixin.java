package net.xlea99.lifeguard.mixin.dye_depot_rock_candy.candy_cavity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.world.level.block.Block;
import net.xlea99.lifeguard.entry.dye_depot_rock_candy.RockCandies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sprinkles Dye Depot's mint, coral and aqua rock candy onto Alex's Caves' donut arches, beside Alex's own colors.
 *
 * <p>Deliberately modest: only these three, which sit in Alex's bright candy palette, and only here. Each sprinkle cell
 * picks its color by spreading a hash evenly over the list, so a longer list needs nothing else; Alex's colors each just
 * become a little rarer. Targeted by name: Lifeguard isn't built against Alex's Caves.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.structure.piece.DonutArchStructurePiece")
public abstract class DonutArchStructurePieceMixin {
    @Unique
    private static final String[] LIFEGUARD$SPRINKLES = {"mint", "coral", "aqua"};

    /** Alex's list and the one with ours appended, built once per Alex's list. */
    @Unique
    private static Block[] lifeguard$source;
    @Unique
    private static Block[] lifeguard$sprinkles;

    @ModifyExpressionValue(method = "placeSprinklesAt", at = @At(value = "FIELD",
        target = "Lcom/github/alexmodguy/alexscaves/server/level/structure/piece/DonutArchStructurePiece;SPRINKLES_BLOCKS:[Lnet/minecraft/world/level/block/Block;"))
    private Block[] lifeguard$withDyeDepotSprinkles(Block[] alex) {
        if (lifeguard$source != alex) {
            Block[] ours = Arrays.stream(LIFEGUARD$SPRINKLES)
                .map(RockCandies::block)
                .filter(Objects::nonNull)
                .toArray(Block[]::new);
            Block[] all = Arrays.copyOf(alex, alex.length + ours.length);
            System.arraycopy(ours, 0, all, alex.length, ours.length);
            lifeguard$sprinkles = all;
            lifeguard$source = alex;
        }
        return lifeguard$sprinkles;
    }
}
