package net.xlea99.lifeguard.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

/**
 * Several entries' pack folders as one pack, earlier layers winning: a child entry's assets over its parent's. Kept as
 * one pack because the order of separate always-on packs is up to the game, not us.
 */
final class LayeredPackResources implements PackResources {
    private final PackLocationInfo location;
    /** Highest priority first. */
    private final List<PackResources> layers;

    LayeredPackResources(PackLocationInfo location, List<PackResources> layers) {
        this.location = location;
        this.layers = List.copyOf(layers);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        for (PackResources layer : layers) {
            IoSupplier<InputStream> found = layer.getRootResource(elements);
            if (found != null)
                return found;
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        for (PackResources layer : layers) {
            IoSupplier<InputStream> found = layer.getResource(packType, location);
            if (found != null)
                return found;
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        // Lowest layer first, so a higher layer's file replaces the same path below it.
        Map<ResourceLocation, IoSupplier<InputStream>> found = new LinkedHashMap<>();
        for (int i = layers.size() - 1; i >= 0; i--)
            layers.get(i).listResources(packType, namespace, path, found::put);
        found.forEach(resourceOutput);
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = new LinkedHashSet<>();
        for (PackResources layer : layers)
            namespaces.addAll(layer.getNamespaces(type));
        return namespaces;
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
        for (PackResources layer : layers) {
            T section = layer.getMetadataSection(deserializer);
            if (section != null)
                return section;
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {
        for (PackResources layer : layers)
            layer.close();
    }
}
