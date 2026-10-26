package net.xlea99.lifeguard.entry.snowy_spirit_edible_treats;

import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * An eaten gumdrop crumbles in its own color ({@link Treats}). Snowy Spirit's gumdrop item model names one particle
 * texture (blue) and each color's model only swaps the texture it draws with, so every gumdrop's crumbs came out blue
 * (Dye the World's colors too: theirs are built the same way). Eating is the only time a gumdrop item makes particles,
 * so this only shows while gumdrops are edible.
 */
public final class GumdropCrumbs {
    private GumdropCrumbs() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(GumdropCrumbs::wrap);
    }

    private static void wrap(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (Item item : BuiltInRegistries.ITEM)
            if (Treats.isGumdrop(item))
                models.computeIfPresent(ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(item)),
                    (key, model) -> new OwnColorCrumbs(model));
    }

    /** A gumdrop item model whose particle is the texture it's drawn with. */
    private static final class OwnColorCrumbs extends BakedModelWrapper<BakedModel> {
        // For one bake; a resource reload builds new wrappers.
        @Nullable
        private TextureAtlasSprite drawn;

        OwnColorCrumbs(BakedModel original) {
            super(original);
        }

        private TextureAtlasSprite crumbs(TextureAtlasSprite fallback) {
            if (drawn == null) {
                drawn = firstSprite(null);
                for (Direction side : Direction.values())
                    if (drawn == null)
                        drawn = firstSprite(side);
                if (drawn == null)
                    drawn = fallback;
            }
            return drawn;
        }

        @Nullable
        private TextureAtlasSprite firstSprite(@Nullable Direction side) {
            for (BakedQuad quad : originalModel.getQuads(null, side, RandomSource.create(42L)))
                return quad.getSprite();
            return null;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return crumbs(originalModel.getParticleIcon());
        }

        @Override
        public TextureAtlasSprite getParticleIcon(ModelData data) {
            return crumbs(originalModel.getParticleIcon(data));
        }
    }
}
