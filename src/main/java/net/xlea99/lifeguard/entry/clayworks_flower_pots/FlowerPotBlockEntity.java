package net.xlea99.lifeguard.entry.clayworks_flower_pots;

import com.teamabnormals.clayworks.common.DecoratedPotTrim;
import com.teamabnormals.clayworks.core.registry.ClayworksDataComponents;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * One flower pot's color and design. Attached to every flower pot block ({@code FlowerPotBlockMixin}); an undyed,
 * unetched pot saves nothing. The item carries the same as components ({@code base_color}, Clayworks' {@code pot_trim}).
 */
public class FlowerPotBlockEntity extends BlockEntity {
    public static final ModelProperty<DyeColor> COLOR = new ModelProperty<>();
    public static final ModelProperty<DecoratedPotTrim> DESIGN = new ModelProperty<>();
    private static final String TAG_COLOR = "color";
    private static final String TAG_DESIGN = "design";

    @Nullable
    private DyeColor color;
    @Nullable
    private DecoratedPotTrim design;

    public FlowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(FlowerPots.BLOCK_ENTITY.get(), pos, state);
        // A pot just swapped for another (plant in, plant out) takes what the old one left.
        PendingPot.Looks carried = PendingPot.take(pos);
        if (carried != null) {
            this.color = carried.color();
            this.design = carried.design();
        }
    }

    @Nullable
    public DyeColor color() {
        return this.color;
    }

    @Nullable
    public DecoratedPotTrim design() {
        return this.design;
    }

    public boolean plain() {
        return this.color == null && this.design == null;
    }

    public PendingPot.Looks looks() {
        return new PendingPot.Looks(this.color, this.design);
    }

    public void setColor(@Nullable DyeColor color) {
        this.color = color;
        this.changed();
    }

    private void changed() {
        this.setChanged();
        if (this.level != null) {
            if (this.level.isClientSide)
                this.requestModelDataUpdate();
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        this.color = input.get(DataComponents.BASE_COLOR);
        this.design = input.get(ClayworksDataComponents.POT_TRIM.get());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.color != null)
            components.set(DataComponents.BASE_COLOR, this.color);
        if (this.design != null)
            components.set(ClayworksDataComponents.POT_TRIM.get(), this.design);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(TAG_COLOR);
        tag.remove(TAG_DESIGN);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.color != null)
            tag.putString(TAG_COLOR, this.color.getSerializedName());
        if (this.design != null)
            DecoratedPotTrim.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.design)
                .ifSuccess(saved -> tag.put(TAG_DESIGN, saved));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.color = tag.contains(TAG_COLOR) ? DyeColor.byName(tag.getString(TAG_COLOR), null) : null;
        this.design = tag.contains(TAG_DESIGN)
            ? DecoratedPotTrim.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(TAG_DESIGN)).result().orElse(null)
            : null;
        // Updates from the server arrive here on the client: the chunk mesh has to be rebuilt with the new looks. A chunk
        // loading has no level yet and draws fresh anyway.
        if (this.level != null && this.level.isClientSide) {
            this.requestModelDataUpdate();
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level.isClientSide && !this.plain())
            this.requestModelDataUpdate();
    }

    @Override
    public void setRemoved() {
        // The server carries the looks across a pot swap in onRemove; the client never gets onRemove and drops the old
        // block entity here, with the new state already in the chunk, so it can tell a swap from a break and pass them
        // on: the swapped pot draws right on the first frame instead of after the server's update.
        if (this.level != null && this.level.isClientSide && !this.plain()) {
            BlockState next = this.level.getBlockState(this.worldPosition);
            if (next.getBlock() instanceof FlowerPotBlock && PendingPot.willTake(this.worldPosition, next))
                PendingPot.put(this.worldPosition, this.looks());
        }
        super.setRemoved();
    }

    // How the looks reach the renderer: the client's model wrapper reads these; a dedicated server never asks.
    @Override
    public ModelData getModelData() {
        if (this.plain())
            return ModelData.EMPTY;
        return ModelData.builder().with(COLOR, this.color).with(DESIGN, this.design).build();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = this.saveWithoutMetadata(registries);
        // An empty update is ignored by NeoForge: say so explicitly when a pot was washed back to plain.
        if (tag.isEmpty())
            tag.putBoolean("plain", true);
        return tag;
    }
}
