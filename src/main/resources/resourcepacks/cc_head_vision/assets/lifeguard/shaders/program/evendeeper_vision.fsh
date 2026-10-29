#version 150

// Looking out of an Evendeeper's head: the Deeper's earthen view through an older, more ruined face. Creeper kin
// never saw anyway; this one's eyes are long gone. Nothing moves that the world doesn't: the view is coarser and
// darker, the picture breaks up into bigger blocks toward the edges, filth sits over it in patches, and light leaks
// rust past the edges of things.

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;

out vec4 fragColor;

const vec3 SLATE = vec3(0.04, 0.04, 0.05);
const vec3 UMBER = vec3(0.34, 0.24, 0.17);
const vec3 STONE = vec3(0.66, 0.60, 0.52);
const vec3 RUST = vec3(0.55, 0.16, 0.05);

const float CENTER_MOSAIC = 6.0; // pixels per cell in the middle (the Deeper's is 4)
const float EDGE_MOSAIC = 14.0;  // and at the edges
const float LEVELS = 4.0;        // shades (the Deeper's is 8)
const float GRIME = 0.65;        // how dark the filthiest patches get
const float VIGNETTE = 2.2;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + 1.0), f.x), f.y);
}

vec3 earth(float luma) {
    return luma < 0.5 ? mix(SLATE, UMBER, luma * 2.0) : mix(UMBER, STONE, (luma - 0.5) * 2.0);
}

float lumaAt(vec2 uv, float mosaic) {
    vec2 cells = InSize / mosaic;
    vec2 cell = (floor(clamp(uv, 0.0, 1.0) * cells) + 0.5) / cells;
    return dot(texture(DiffuseSampler, cell).rgb, vec3(0.3, 0.59, 0.11));
}

void main() {
    vec2 fromCenter = texCoord - 0.5;
    float edge = smoothstep(0.1, 0.6, length(fromCenter * vec2(InSize.x / InSize.y, 1.0)) * 0.8);
    float mosaic = floor(mix(CENTER_MOSAIC, EDGE_MOSAIC, edge) + 0.5);

    float luma = lumaAt(texCoord, mosaic);
    float shifted = lumaAt(texCoord + vec2(0.006, 0.0), mosaic);
    luma = floor(luma * LEVELS + 0.5) / LEVELS;

    vec3 color = earth(luma);
    color += RUST * clamp(shifted - luma, 0.0, 1.0) * 2.5; // light leaking rust past the edges of things

    vec2 p = texCoord * vec2(InSize.x / InSize.y, 1.0);
    float grime = noise(p * 3.0) * 0.6 + noise(p * 9.0) * 0.4;
    color *= 1.0 - GRIME * smoothstep(0.45, 0.85, grime);
    color *= 1.0 - dot(fromCenter, fromCenter) * VIGNETTE;

    fragColor = vec4(max(color, 0.0), 1.0);
}
