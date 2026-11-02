package net.xlea99.lifeguard.entry.ice_stops_crops;

import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.xlea99.lifeguard.boot.EntrySetup;
import net.xlea99.lifeguard.boot.Wiring;

/** Setup for {@link IceStopsCrops}. */
public final class IceStopsCropsSetup implements EntrySetup {
    @Override
    public void setup(Wiring wiring) {
        wiring.whenActive(IceStopsCrops.ENTRY, () -> NeoForge.EVENT_BUS.addListener(IceStopsCropsSetup::onCropGrow));
    }

    private static void onCropGrow(CropGrowEvent.Pre event) {
        if (event.getLevel().isClientSide())
            return;
        if (event.getLevel().getBlockState(event.getPos().below(2)).is(BlockTags.ICE))
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
    }
}
