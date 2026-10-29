package net.xlea99.lifeguard.mixin.twigs_bloodstone_obelisks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Twigs' structure templates load from 1.20's {@code structures/} folder when 1.21's {@code structure/} has none.
 *
 * <p>Twigs 3.1.2 still ships the Bloodstone Obelisk at {@code data/twigs/structures/}, and 1.21 only reads
 * {@code structure/}. A template that isn't found becomes an empty one with no error, so every obelisk generates with no
 * blocks and no chest. Only Twigs' namespace, and only when the 1.21 path has nothing, so a datapack's own template
 * there still wins.
 */
@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {
    @Shadow
    private ResourceManager resourceManager;

    @WrapOperation(method = "loadFromResource", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/resources/FileToIdConverter;idToFile(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation lifeguard$twigsOldFolder(FileToIdConverter lister, ResourceLocation id, Operation<ResourceLocation> original) {
        ResourceLocation file = original.call(lister, id);
        if (!id.getNamespace().equals("twigs") || this.resourceManager.getResource(file).isPresent())
            return file;
        ResourceLocation old = id.withPath(path -> "structures/" + path + ".nbt");
        return this.resourceManager.getResource(old).isPresent() ? old : file;
    }
}
