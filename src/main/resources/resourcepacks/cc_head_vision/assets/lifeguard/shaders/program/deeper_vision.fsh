#version 150

// Looking out of a Deeper's head: the creeper head's chunky, posterized view, in the earth's browns and grays
// instead of green. Dark grays sit in the shadows, warm umber in the middle, pale stone in the light.

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform float MosaicSize;
uniform float Levels;

out vec4 fragColor;

const vec3 SLATE = vec3(0.09, 0.09, 0.10);
const vec3 UMBER = vec3(0.45, 0.35, 0.26);
const vec3 STONE = vec3(0.80, 0.76, 0.69);

vec3 earth(float luma) {
    return luma < 0.5 ? mix(SLATE, UMBER, luma * 2.0) : mix(UMBER, STONE, (luma - 0.5) * 2.0);
}

void main() {
    vec2 cells = InSize / MosaicSize;
    vec2 uv = (floor(texCoord * cells) + 0.5) / cells;
    float luma = dot(texture(DiffuseSampler, uv).rgb, vec3(0.3, 0.59, 0.11));
    luma = floor(luma * Levels + 0.5) / Levels;
    fragColor = vec4(earth(luma), 1.0);
}
