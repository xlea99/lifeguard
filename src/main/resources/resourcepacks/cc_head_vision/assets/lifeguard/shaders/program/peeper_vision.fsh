#version 150

// Looking out of a Peeper's head: a cave eye, made for the dark. Anything that shines blows out into huge halos and
// long streaks (the piglin head's glitter, turned up past sense, for every color). Everything else drains and falls
// away into the dark around the middle of your view, which swells toward you. The whole picture trembles, and
// throbs on a heartbeat: the glow surges, the middle lurches closer and the colors split.

uniform sampler2D DiffuseSampler;
uniform sampler2D GlowSampler;   // the glowing parts, blurred wide (small target)
uniform sampler2D StreakSampler; // the same, smeared sideways

in vec2 texCoord;

uniform vec2 InSize;
uniform float Time; // 0..1, loops every second

out vec4 fragColor;

#define TAU 6.28318530718

const float GLOW = 5.0;          // halo strength
const float STREAK = 1.4;        // streak strength
const float SURGE = 1.2;         // extra glow on a beat
const float SWELL = 0.22;        // how much bigger the middle is
const float BEAT_SWELL = 0.05;   // extra on a beat
const float SPLIT = 0.010;       // color split on a beat
const float TREMOR = 0.0010;     // the constant shake
const float DRAIN = 0.15;        // saturation left outside the glow
const float DARK = 0.5;          // brightness left outside the glow
const float FALLOFF = 0.9;       // how far the edges fall into the dark

/** A heartbeat once a second: a sharp "lub", a softer "dub" just after, each fading fast. */
float heartbeat(float t) {
    float lub = exp(-fract(t) * 12.0);
    float dub = exp(-fract(t - 0.2) * 14.0);
    return lub + 0.6 * dub;
}

void main() {
    float beat = heartbeat(Time);
    vec2 aspect = vec2(InSize.x / InSize.y, 1.0);

    // Tremor: fast and small, whole cycles per second; harder on the beat.
    vec2 shake = vec2(sin(TAU * Time * 23.0), cos(TAU * Time * 29.0)) * TREMOR * (0.6 + beat);

    // Swell: the middle of the view is magnified, more on the beat; the edges stay put.
    vec2 fromCenter = texCoord - 0.5;
    float r = length(fromCenter * aspect);
    float zoom = 1.0 - (SWELL + BEAT_SWELL * beat) * (1.0 - smoothstep(0.0, 0.7, r));
    vec2 uv = 0.5 + fromCenter * zoom + shake;

    // The world, colors split along the line from the middle on the beat, drained and darkened toward the edges.
    vec2 split = fromCenter * SPLIT * beat;
    vec3 world = vec3(
        texture(DiffuseSampler, uv + split).r,
        texture(DiffuseSampler, uv).g,
        texture(DiffuseSampler, uv - split).b);
    float luma = dot(world, vec3(0.3, 0.59, 0.11));
    world = mix(vec3(luma), world, DRAIN) * DARK;
    world *= 1.0 - smoothstep(0.12, 0.8, r) * FALLOFF;

    // The glow: everywhere, not darkened, surging on the beat.
    vec3 glow = texture(GlowSampler, uv).rgb * GLOW + texture(StreakSampler, uv).rgb * STREAK;
    glow *= 1.0 + SURGE * beat;

    fragColor = vec4(clamp(world + glow, 0.0, 1.0), 1.0);
}
