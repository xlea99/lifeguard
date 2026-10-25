package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import javax.annotation.Nullable;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * How an Amendments hanging flower pot's color and design travel. Its block entity speaks Moonlight's model data, so
 * they go under these keys, and are handed to the hung pot's own model (our wrapper) as NeoForge model data.
 */
public final class HangingPots {
    public static final ModelDataKey<DyeColor> COLOR = new ModelDataKey<>(DyeColor.class);
    public static final ModelDataKey<DecoratedPotTrim> DESIGN = new ModelDataKey<>(DecoratedPotTrim.class);

    /** What a hanging pot's block entity holds; implemented on Amendments' by a mixin. */
    public interface Looks {
        @Nullable
        DyeColor lifeguard$color();

        @Nullable
        DecoratedPotTrim lifeguard$design();

        void lifeguard$setLooks(@Nullable DyeColor color, @Nullable DecoratedPotTrim design);
    }

    private HangingPots() {
    }

    /** The hung pot's looks as the flower pot model reads them, or null if it has none. */
    @Nullable
    public static ModelData forPotModel(@Nullable ExtraModelData data) {
        if (data == null)
            return null;
        DyeColor color = data.get(COLOR);
        DecoratedPotTrim design = data.get(DESIGN);
        if (color == null && design == null)
            return null;
        return ModelData.builder().with(FlowerPotBlockEntity.COLOR, color).with(FlowerPotBlockEntity.DESIGN, design).build();
    }
}
