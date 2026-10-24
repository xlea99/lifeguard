package net.xlea99.lifeguard.entry.sexy_runic_glints;

/**
 * A vertex buffer that fills in any data a vertex is missing (white, no overlay, full light, facing up) instead of
 * throwing "Missing elements in vertex" (see {@code BufferBuilderMixin}). Glint buffers that take full item vertices are
 * made lenient: vanilla always sends full vertices, which pass straight through; this is only a safety net for other
 * mods' renderers sending the position and texture a glint used to need.
 */
public interface LenientVertices {
    void lifeguard$setLenient(boolean lenient);
}
