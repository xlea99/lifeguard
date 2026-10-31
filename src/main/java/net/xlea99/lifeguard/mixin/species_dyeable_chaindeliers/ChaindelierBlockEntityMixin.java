package net.xlea99.lifeguard.mixin.species_dyeable_chaindeliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.Chaindeliers;
import net.xlea99.lifeguard.entry.species_dyeable_chaindeliers.DyeableChaindelier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A placed Chaindelier keeps its color ({@link Chaindeliers}): taken from its item's {@code dyed_color} when placed (on
 * both sides, so the client draws it right away) and given back to its drop, saved like a Hopelight's ({@code color},
 * left out while it's Species' cyan), and sent to the client, which Species' Chaindelier never needed before (it had
 * nothing to send). Kept whatever the setting, so nothing is lost while it's off. Written against our build of Species'
 * 1.21.1 port (PR #88), by class name; its block entity is otherwise empty.
 */
@Mixin(targets = "com.ninni.species.server.block.entity.ChaindelierBlockEntity")
public abstract class ChaindelierBlockEntityMixin extends BlockEntity implements DyeableChaindelier {
    @Unique
    private int lifeguard$color = Chaindeliers.DEFAULT_COLOR;

    protected ChaindelierBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int lifeguard$getColor() {
        return this.lifeguard$color;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        DyedItemColor dyed = input.get(DataComponents.DYED_COLOR);
        this.lifeguard$color = dyed != null ? dyed.rgb() : Chaindeliers.DEFAULT_COLOR;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.lifeguard$color != Chaindeliers.DEFAULT_COLOR)
            components.set(DataComponents.DYED_COLOR, new DyedItemColor(this.lifeguard$color, false));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(Chaindeliers.COLOR);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lifeguard$color = tag.contains(Chaindeliers.COLOR, Tag.TAG_ANY_NUMERIC) ? tag.getInt(Chaindeliers.COLOR) : Chaindeliers.DEFAULT_COLOR;
        // As flower pots: a color arriving on the client redraws the block (its painted glow is in the chunk's mesh).
        // Chunk load has no level yet, and meshes fresh anyway.
        if (this.level != null && this.level.isClientSide()) {
            this.requestModelDataUpdate();
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.lifeguard$color != Chaindeliers.DEFAULT_COLOR)
            tag.putInt(Chaindeliers.COLOR, this.lifeguard$color);
    }

    /** Always says its color, so the client's never left with an old one. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(Chaindeliers.COLOR, this.lifeguard$color);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level.isClientSide() && this.lifeguard$color != Chaindeliers.DEFAULT_COLOR)
            this.requestModelDataUpdate();
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(Chaindeliers.COLOR_PROPERTY, this.lifeguard$color).build();
    }
}
