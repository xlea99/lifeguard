package net.xlea99.lifeguard.entry.cc_cannon_backfire;

import com.teamabnormals.caverns_and_chasms.common.block.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.api.platform.RegisterFireBehaviorsEvent;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.xlea99.lifeguard.Lifeguard;
import org.jetbrains.annotations.Nullable;

/**
 * A Supplementaries cannon loaded with Caverns & Chasms' gunpowder block blows up. A cannon fires by lighting its charge
 * behind the shot, and loose gunpowder in the barrel goes off with it: the cannon explodes instead of firing (otherwise
 * Supplementaries flies the block like any other and sets it down where it lands). The blocks that do this are
 * {@code #lifeguard:cannon_backfires}: Caverns & Chasms' gunpowder block explodes as it does when lit, anything else
 * with a TNT-strength blast. The cannon fires as usual first, using up its charge and the block, and goes off just
 * after, so nothing it held comes back twice; a cannon block is knocked loose and drops, and a cannon boat takes the
 * blast. Only loaded when the entry is active, so only with both mods installed.
 */
public final class CannonBackfire implements IFireItemBehavior {
    private static final TagKey<Block> BACKFIRES =
        TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Lifeguard.MODID, "cannon_backfires"));
    private static final ResourceLocation GUNPOWDER_BLOCK = ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "gunpowder_block");
    /** Caverns & Chasms' gunpowder block, and TNT. */
    private static final float BLAST_POWER = 4.0F;
    private static final CannonBackfire INSTANCE = new CannonBackfire();

    private CannonBackfire() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CannonBackfire::onRegisterBehaviors);
    }

    /** Supplementaries asks for cannon behaviors after its own, on every data reload (tags are bound by then). */
    private static void onRegisterBehaviors(RegisterFireBehaviorsEvent event) {
        for (Item item : event.getRegistryAccess().registryOrThrow(Registries.ITEM))
            if (item instanceof BlockItem block && block.getBlock().defaultBlockState().is(BACKFIRES))
                event.registerCannonBehavior(item, INSTANCE);
    }

    /** The cannon calls this one, from its own center; the shot would leave {@code fireOffset} further along. */
    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 pos, float fireOffset, Vec3 direction, float power, int inaccuracy,
                        @Nullable Entity owner) {
        backfire(stack, level, pos, owner);
        return true;
    }

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, int inaccuracy, @Nullable Entity owner) {
        backfire(stack, level, firePos, owner);
        return true;
    }

    /** Goes off once the cannon has finished firing (and used up its charge and the block), later this tick. */
    private static void backfire(ItemStack stack, ServerLevel level, Vec3 at, @Nullable Entity owner) {
        boolean gunpowder = GUNPOWDER_BLOCK.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        MinecraftServer server = level.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            // A cannon is as blast-proof as an anvil, and a blast starting inside it dies there, breaking nothing: so
            // it's knocked out first (it drops, with what it held), and the blast goes off where it stood.
            BlockPos cannon = BlockPos.containing(at);
            if (level.getBlockState(cannon).getBlock() instanceof CannonBlock)
                level.destroyBlock(cannon, true, owner);
            if (gunpowder)
                GunpowderBlock.explode(level, cannon, owner instanceof LivingEntity living ? living : null);
            else
                level.explode(owner, at.x, at.y, at.z, BLAST_POWER, Level.ExplosionInteraction.TNT);
        }));
    }
}
