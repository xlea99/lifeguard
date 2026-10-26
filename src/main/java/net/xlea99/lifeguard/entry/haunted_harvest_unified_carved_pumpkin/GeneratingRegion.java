package net.xlea99.lifeguard.entry.haunted_harvest_unified_carved_pumpkin;

import org.jetbrains.annotations.Nullable;

/** What a worldgen region is generating, as the game tracks it for crash reports ({@code mixin...WorldGenRegionMixin}). */
public interface GeneratingRegion {
    /** "ResourceKey[minecraft:worldgen/structure / minecraft:mansion]", or null between structures and features. */
    @Nullable
    String lifeguard$generating();
}
