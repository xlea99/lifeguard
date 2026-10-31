package net.xlea99.lifeguard.mixin.clayworks_flower_pots_amendments;

import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import com.teamabnormals.clayworks.core.registry.ClayworksDataComponents;
import javax.annotation.Nullable;
import net.mehvahdjukaar.amendments.common.tile.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.ClayworksFlowerPots;
import net.xlea99.lifeguard.entry.clayworks_flower_pots.HangingPots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A hung flower pot keeps its color and design. Hanging a pot swaps it for Amendments' own block, whose block entity
 * only remembered which pot it shows; it now holds the looks too, taken from the pot item as it's placed (the item's
 * components) and planting in it only changes the pot it shows. They reach the model as Moonlight model data, and
 * Supplementaries' soap washes the color off, as on a standing pot.
 */
@Mixin(HangingFlowerPotBlockTile.class)
public abstract class HangingFlowerPotBlockTileMixin extends MimicBlockTile implements HangingPots.Looks, IWashable {
    @Unique
    private static final String lifeguard$COLOR = "lifeguard_color";
    @Unique
    private static final String lifeguard$DESIGN = "lifeguard_design";

    @Unique
    @Nullable
    private DyeColor lifeguard$color;
    @Unique
    @Nullable
    private DecoratedPotTrim lifeguard$design;

    protected HangingFlowerPotBlockTileMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    @Override
    public DyeColor lifeguard$color() {
        return this.lifeguard$color;
    }

    @Nullable
    @Override
    public DecoratedPotTrim lifeguard$design() {
        return this.lifeguard$design;
    }

    @Override
    public void lifeguard$setLooks(@Nullable DyeColor color, @Nullable DecoratedPotTrim design) {
        this.lifeguard$color = color;
        this.lifeguard$design = design;
        this.setChanged();
        if (this.level != null) {
            if (this.level.isClientSide)
                this.requestModelReload();
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        this.lifeguard$color = input.get(DataComponents.BASE_COLOR);
        this.lifeguard$design = input.get(ClayworksDataComponents.POT_TRIM.get());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.lifeguard$color != null)
            components.set(DataComponents.BASE_COLOR, this.lifeguard$color);
        if (this.lifeguard$design != null)
            components.set(ClayworksDataComponents.POT_TRIM.get(), this.lifeguard$design);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.lifeguard$color != null)
            tag.putString(lifeguard$COLOR, this.lifeguard$color.getSerializedName());
        if (this.lifeguard$design != null)
            DecoratedPotTrim.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.lifeguard$design)
                .ifSuccess(saved -> tag.put(lifeguard$DESIGN, saved));
    }

    // Also how the client hears of new looks.
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lifeguard$color = tag.contains(lifeguard$COLOR) ? DyeColor.byName(tag.getString(lifeguard$COLOR), null) : null;
        this.lifeguard$design = tag.contains(lifeguard$DESIGN)
            ? DecoratedPotTrim.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(lifeguard$DESIGN)).result().orElse(null)
            : null;
        if (this.level != null && this.level.isClientSide) {
            this.requestModelReload();
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        super.addExtraModelData(builder);
        if (this.lifeguard$color != null)
            builder.with(HangingPots.COLOR, this.lifeguard$color);
        if (this.lifeguard$design != null)
            builder.with(HangingPots.DESIGN, this.lifeguard$design);
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hit) {
        if (!ClayworksFlowerPots.SOAP.active() || this.lifeguard$color == null)
            return false;
        if (!level.isClientSide)
            this.lifeguard$setLooks(null, this.lifeguard$design);
        return true;
    }
}
