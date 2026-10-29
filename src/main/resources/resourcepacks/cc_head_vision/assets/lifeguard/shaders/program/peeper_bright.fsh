#version 150

// The Peeper's glow, step one: only what shines, shrunk to the small glow target. Each glow pixel covers a patch of
// the screen; it keeps the brightest thing in the patch (not the average), so a pair of glowing eyes a few pixels
// wide still lights up. Colors come out more saturated than they went in: the glow keeps its hue when it blows out.

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform vec2 OutSize;

out vec4 fragColor;

const float THRESHOLD_LOW = 0.62;  // brightest channel where glowing starts
const float THRESHOLD_HIGH = 0.92; // and where it's full
const float SATURATE = 1.6;

void main() {
    vec2 patch = oneTexel * InSize / OutSize; // one glow pixel, in screen uv
    vec3 brightest = vec3(0.0);
    float best = 0.0;
    for (int y = 0; y < 4; y++) {
        for (int x = 0; x < 4; x++) {
            vec2 uv = texCoord + patch * (vec2(x, y) - 1.5) / 4.0;
            vec3 c = texture(DiffuseSampler, uv).rgb;
            float shine = smoothstep(THRESHOLD_LOW, THRESHOLD_HIGH, max(c.r, max(c.g, c.b)));
            if (shine > best) {
                best = shine;
                brightest = c;
            }
        }
    }
    float luma = dot(brightest, vec3(0.3, 0.59, 0.11));
    vec3 color = clamp(mix(vec3(luma), brightest, SATURATE), 0.0, 1.0);
    fragColor = vec4(color * best, 1.0);
}
