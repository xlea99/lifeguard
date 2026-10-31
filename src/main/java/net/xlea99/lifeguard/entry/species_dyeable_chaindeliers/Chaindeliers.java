package net.xlea99.lifeguard.entry.species_dyeable_chaindeliers;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xlea99.lifeguard.Lifeguard;

/**
 * Species' Chaindeliers, dyeable like its Hopelights and Speclights ({@link SpeciesDyeableChaindeliers}).
 *
 * <p>A Chaindelier's glow is the Hopelight's, drawn with Species' spectre cyan baked into its textures instead of tinted
 * with a color it keeps. Here it keeps one the way the 1.21 Hopelight does: vanilla's {@code dyed_color} on its item,
 * and on its block entity once placed ({@code ChaindelierBlockEntityMixin}), Species' cyan when it has none. Crafting it
 * with dyes mixes them in like leather armor ({@link ChaindelierDyeRecipe}), a water cauldron washes it, and the glow
 * draws in its color (the beam by {@code ChaindelierBlockEntityRendererMixin}, the painted glow by
 * {@link ChaindelierModel}). The block keeps its color whatever the setting (broken, it drops with it), so turning the
 * setting off and on again loses nothing. Species isn't compiled against: the Chaindelier is found by its id.
 */
public final class Chaindeliers {
    /** Species' spectre cyan, what an undyed one glows. */
    public static final int DEFAULT_COLOR = 0x7CF2F5;
    public static final String COLOR = "color";
    /** A placed one's color, for the client's model of it (its painted glow is drawn in it once dyed). */
    public static final ModelProperty<Integer> COLOR_PROPERTY = new ModelProperty<>();

    private static final ResourceLocation CHAINDELIER = ResourceLocation.fromNamespaceAndPath("species", "chaindelier");

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Lifeguard.MODID);
    public static final Supplier<RecipeSerializer<ChaindelierDyeRecipe>> DYEING =
        SERIALIZERS.register("species_chaindelier_dyeing", () -> new SimpleCraftingRecipeSerializer<>(ChaindelierDyeRecipe::new));

    private static boolean looked;
    @Nullable
    private static Item item;

    private Chaindeliers() {
    }

    /** Whenever Species is there, never by the switch: the recipe file and the wash ask the switch. */
    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            Item chaindelier = item();
            if (chaindelier != null)
                CauldronInteraction.WATER.map().put(chaindelier, WASH);
        }));
    }

    public static boolean enabled() {
        return SpeciesDyeableChaindeliers.ENTRY.active();
    }

    @Nullable
    public static Item item() {
        if (!looked) {
            looked = true;
            Item found = BuiltInRegistries.ITEM.get(CHAINDELIER);
            item = found == Items.AIR ? null : found;
        }
        return item;
    }

    public static boolean is(ItemStack stack) {
        Item chaindelier = item();
        return chaindelier != null && stack.is(chaindelier);
    }

    /** The color a Chaindelier item was dyed, or null if it's undyed. */
    @Nullable
    public static Integer colorOf(ItemStack stack) {
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        return dyed == null || dyed.rgb() == DEFAULT_COLOR ? null : dyed.rgb();
    }

    /** Dyes a Chaindelier item, or washes it with null. */
    public static void setColor(ItemStack stack, @Nullable Integer color) {
        if (color != null && color != DEFAULT_COLOR)
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        else
            stack.remove(DataComponents.DYED_COLOR);
    }

    /** {@code start} with {@code dyes} mixed in, the way leather armor mixes them. */
    public static int mix(@Nullable Integer start, List<DyeItem> dyes) {
        int[] sum = new int[3];
        int brightest = 0;
        int count = 0;
        if (start != null) {
            int r = start >> 16 & 255;
            int g = start >> 8 & 255;
            int b = start & 255;
            brightest += Math.max(r, Math.max(g, b));
            sum[0] += r;
            sum[1] += g;
            sum[2] += b;
            count++;
        }
        for (DyeItem dye : dyes) {
            int rgb = dye.getDyeColor().getTextureDiffuseColor();
            int r = rgb >> 16 & 255;
            int g = rgb >> 8 & 255;
            int b = rgb & 255;
            brightest += Math.max(r, Math.max(g, b));
            sum[0] += r;
            sum[1] += g;
            sum[2] += b;
            count++;
        }
        int r = sum[0] / count;
        int g = sum[1] / count;
        int b = sum[2] / count;
        float average = (float) brightest / count;
        float max = Math.max(r, Math.max(g, b));
        r = (int) (r * average / max);
        g = (int) (g * average / max);
        b = (int) (b * average / max);
        return (r << 16) + (g << 8) + b;
    }

    /** Like Species' lights (and leather armor): a water cauldron washes a dyed Chaindelier, one level a go. */
    private static final CauldronInteraction WASH = (state, level, pos, player, hand, stack) -> {
        if (!enabled() || colorOf(stack) == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide()) {
            setColor(stack, null);
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    };
}
