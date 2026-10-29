#version 150

// Spreads bright spots along one direction by taking the brightest pixel nearby, not the average, so a light one
// pixel wide stays at full strength as it grows. Decay 0: a flat grow (run across, then down, to swell each light into
// a block before blurring it into a halo). Decay above 0: each step away counts for less, a streak that fades out.

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 Dir;
uniform float Radius; // in pixels of this target
uniform float Decay;

out vec4 fragColor;

void main() {
    vec3 brightest = texture(DiffuseSampler, texCoord).rgb;
    for (float i = 1.0; i <= Radius; i += 1.0) {
        float weight = exp(-Decay * i / Radius);
        vec2 offset = Dir * oneTexel * i;
        brightest = max(brightest, texture(DiffuseSampler, texCoord + offset).rgb * weight);
        brightest = max(brightest, texture(DiffuseSampler, texCoord - offset).rgb * weight);
    }
    fragColor = vec4(brightest, 1.0);
}
