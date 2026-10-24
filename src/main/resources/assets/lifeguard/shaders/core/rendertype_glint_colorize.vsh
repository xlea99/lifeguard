#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

// Item glint vertices carry everything the item's own vertices do (they're the same vertices), not just position
// and texture coordinates like vanilla's glint, so colorize can light its color the way vanilla lights the item.

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2; // lightmap

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;
uniform int FogShape;
uniform float ItemCoordScale; // how much the glint's coordinates were scaled up (shields, tridents; else 1)

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec2 texCoord0;
out vec2 itemCoord;
out vec4 itemTint;
out vec4 itemLight;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
    // The texture matrix only moves the coordinates around the glint texture; as they are, they find the item's own
    // pixel in its texture. (Shield and trident glint coordinates arrive scaled up to match ordinary items' glint
    // size; undo that for the lookup.)
    itemCoord = UV0 / ItemCoordScale;
    // Vanilla's item shader lights a pixel as texture x color x directional light x lightmap. The color (dyes,
    // spawn egg and potion tints) and the light are kept apart: colorize recolors the first and keeps the second.
    itemTint = Color;
    itemLight = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, vec4(1.0)) * texelFetch(Sampler2, UV2 / 16, 0);
}
