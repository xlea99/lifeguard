package net.xlea99.lifeguard.entry.moonlight_jade_contents;

import java.util.Collections;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.xlea99.lifeguard.Lifeguard;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.FluidTextHelper;

/**
 * What's in a Moonlight container, shown in Jade: Supplementaries' jars and goblets and Amendments' cauldrons.
 *
 * <p>Jade only knows NeoForge fluids. Amendments' cauldrons hand Jade their liquid as one, through Moonlight, but most
 * of Moonlight's liquids have no NeoForge fluid (potions, milk, dye, honey, soups), so Jade showed a cauldron of them
 * as empty, and jars and goblets showed nothing at all. Here the liquid is read directly and named as it is: a potion by
 * the name its bottle would have ("Splash Potion of Swiftness"), dye by its color when it's one of the sixteen,
 * anything else by its own name. With Amendments, vanilla's water, powder snow and lava cauldrons get the same line, so
 * every cauldron reads alike. It replaces Jade's fluid line, drawn the way Jade draws it (its Fluid Storage settings and
 * display style), colored like the liquid. Switched by Lifeguard's config, so it's marked required.
 */
public enum SoftFluidContents implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "soft_fluid_contents");
    private static final ResourceLocation DYE = ResourceLocation.fromNamespaceAndPath("amendments", "dye");
    private static final String UNNAMED = "fluid.moonlight.generic_fluid";
    private static final boolean AMENDMENTS = ModList.get().isLoaded("amendments");

    public static void register(IWailaClientRegistration registration) {
        registration.registerBlockComponent(INSTANCE, Block.class);
        registration.markAsClientFeature(UID);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        SoftFluidStack fluid;
        long amount;
        long capacity;
        if (accessor.getBlockEntity() instanceof ISoftFluidTankProvider provider) {
            SoftFluidTank tank = provider.getSoftFluidTank();
            fluid = tank.getFluid();
            // Moonlight counts bottles; a bottle is 250 mB.
            amount = fluid.getCount() * 250L;
            capacity = tank.getCapacity() * 250L;
        } else {
            // Vanilla's water, powder snow and lava cauldrons hold no liquid, just a level; beside Amendments'
            // cauldrons, they're shown the same way.
            BlockState state = accessor.getBlockState();
            RegistryAccess registries = accessor.getLevel().registryAccess();
            Holder<SoftFluid> liquid = !AMENDMENTS ? null
                : state.is(Blocks.WATER_CAULDRON) ? MLBuiltinSoftFluids.WATER.getHolder(registries)
                : state.is(Blocks.POWDER_SNOW_CAULDRON) ? MLBuiltinSoftFluids.POWDERED_SNOW.getHolder(registries)
                : state.is(Blocks.LAVA_CAULDRON) ? MLBuiltinSoftFluids.LAVA.getHolder(registries) : null;
            if (liquid == null)
                return;
            // A third of a bucket per level, or a quarter with Lifeguard's cauldron_fourths.
            int full = Collections.max(LayeredCauldronBlock.LEVEL.getPossibleValues());
            int level = state.hasProperty(LayeredCauldronBlock.LEVEL) ? state.getValue(LayeredCauldronBlock.LEVEL) : full;
            fluid = SoftFluidStack.of(liquid, level);
            amount = Math.round(level * 1000.0 / full);
            capacity = 1000;
        }
        // Jade's line names a liquid without a NeoForge fluid as empty; this one replaces it either way.
        tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
        if (fluid.isEmpty() || !config.get(JadeIds.UNIVERSAL_FLUID_STORAGE)
            || !accessor.showDetails() && config.get(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED))
            return;
        IThemeHelper theme = IThemeHelper.get();
        IWailaConfig.HandlerDisplayStyle style = config.getEnum(JadeIds.UNIVERSAL_FLUID_STORAGE_STYLE);
        Component current = theme.info(FluidTextHelper.getUnicodeMillibuckets(amount, true));
        Component text = !accessor.showDetails() && style == IWailaConfig.HandlerDisplayStyle.PROGRESS_BAR ? current
            : Component.translatable("jade.fluid.with_capacity", current, FluidTextHelper.getUnicodeMillibuckets(capacity, true));
        text = Component.translatable(style == IWailaConfig.HandlerDisplayStyle.PLAIN_TEXT ? "jade.fluid.text" : "jade.fluid",
            theme.info(nameOf(fluid, accessor.getLevel().registryAccess())), text);
        IElementHelper helper = IElementHelper.get();
        switch (style) {
            case PLAIN_TEXT -> tooltip.add(text);
            case ICON -> {
                tooltip.add(helper.smallItem(new ItemStack(Items.BUCKET)));
                tooltip.append(text);
            }
            case PROGRESS_BAR -> {
                int color = 0xFF000000 | fluid.getParticleColor(accessor.getLevel(), accessor.getPosition());
                tooltip.add(helper.progress((float) amount / capacity, text, helper.progressStyle().color(color),
                    BoxStyle.getNestedBox(), true));
            }
        }
    }

    /** The liquid's name, as it would be bottled. */
    private static Component nameOf(SoftFluidStack fluid, RegistryAccess registries) {
        if (fluid.is(MLBuiltinSoftFluids.POTION)) {
            var bottled = fluid.toItem(new ItemStack(Items.GLASS_BOTTLE));
            if (bottled != null)
                return bottled.getFirst().getHoverName();
        }
        Component name = liquidName(fluid.fluid());
        DyedItemColor dyed = fluid.getHolder().is(DYE) ? fluid.getComponents().get(DataComponents.DYED_COLOR) : null;
        if (dyed != null)
            for (DyeColor dye : DyeColor.values())
                if ((dye.getTextureDiffuseColor() & 0xFFFFFF) == (dyed.rgb() & 0xFFFFFF))
                    return Component.translatable("color.minecraft." + dye.getName()).append(" ").append(name);
        return name;
    }

    /** A liquid's own name, or, if it has none (or one nothing translates), what it's bottled or bucketed in. */
    private static Component liquidName(SoftFluid fluid) {
        Component name = fluid.getTranslatedName();
        if (!(name.getContents() instanceof TranslatableContents translatable)
            || !translatable.getKey().equals(UNNAMED) && Language.getInstance().has(translatable.getKey()))
            return name;
        for (FluidContainerList.Category category : fluid.getContainerList().getCategories())
            for (Item item : category.getFilledItems())
                if (item != Items.AIR)
                    return new ItemStack(item).getHoverName();
        return name;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    /** After Jade's own fluid line (1000), so it can be taken out. */
    @Override
    public int getDefaultPriority() {
        return 1100;
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
