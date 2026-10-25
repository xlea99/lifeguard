package net.xlea99.lifeguard.entry.amendments_hanging_pot_names;

import net.mehvahdjukaar.amendments.common.block.HangingFlowerPotBlock;
import net.mehvahdjukaar.amendments.common.tile.HangingFlowerPotBlockTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.xlea99.lifeguard.Lifeguard;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

/**
 * Names an Amendments hanging flower pot in Jade after the pot it holds, like a standing one: "Potted Poppy", or the
 * plain name when empty. Jade names a block by its own name, and Amendments gives the hanging pot one fixed name,
 * "Hanging Flower Pot", whatever is planted in it. The pot it holds is already on the client. Switched by Lifeguard's
 * config, so it's marked required to keep it out of Jade's settings.
 */
public enum HangingPotName implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "hanging_pot_name");

    public static void register(IWailaClientRegistration registration) {
        registration.registerBlockComponent(INSTANCE, HangingFlowerPotBlock.class);
        registration.markAsClientFeature(UID);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof HangingFlowerPotBlockTile pot))
            return;
        BlockState held = pot.getHeldBlock();
        if (held.isAir())
            return;
        // How Jade's own providers rename things (player heads, spawners).
        tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(held.getBlock().getName()));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
