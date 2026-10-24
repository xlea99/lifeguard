#version 150

#moj_import <fog.glsl>

// The shader for every rune color's item glint (inventory, hand, dropped, item frames), and for the stand-in glints
// over worn armor, elytra, shields and tridents when a color colorizes (see GlintVariants).
//
// Colorize 0: vanilla's glint shader, whatever the color's blend (vanilla or tint), except that a tint
// glint fades out with distance toward 50% gray (no effect) rather than toward black (which would darken).
//
// Colorize > 0 (tint colors only): a plain tint multiplies the item channel by channel, which rewrites the item's
// light and dark: pixels that already hold the tint's color keep their brightness, the rest lose it (under an
// orange tint, diamond's near-white highlights stay bright while its turquoise body goes dark). Colorize instead
// gives each pixel the tint at its own brightness, so the item keeps its shading and only changes color. It's
// worked out here in full, lit the way vanilla lights the item, and drawn over the item (premultiplied blend), not
// multiplied: a multiply can at most double a channel, which isn't enough for a color the item barely has.
// Colorize between 0 and 1 mixes the plain multiply and the colorized color.
//
// Ramp (RampCount > 0): colorize along a gradient of up to 8 colors instead of with one, for a dye whose own
// shading shifts hue (deep shadows, saturated mids, pale highlights). The glint texture is then a gray ray map: its
// brightness says how far into a ray the pixel is, which moves the stretch of the ramp the item spans from RampField
// (black) to RampRay (white). Plain (colorize off), a ramp color is drawn with its rune texture instead (GlintShards).
//
// Banner patterns (BannerCount > 0): a shield's patterns are drawn over its plate in passes of their own, which this
// glint (on the plate) can't see, so they're layered here first, from the same atlas, before colorizing.

uniform sampler2D Sampler0; // the glint
uniform sampler2D Sampler1; // the texture the glint lies over: the block atlas for items, the armor's own for armor
uniform sampler2D Sampler3; // the armor trim atlas, for worn armor with a trim

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GlintAlpha;
uniform float Colorize;
uniform float FadeTo;   // what a plain glint fades to with distance: 0 (black) for added glints, 0.5 for tints
uniform float Strength; // tints: how much the glint affects the item at all (1 = fully); 1 for added glints
uniform float Selective; // > 0: colorize only where the glint is colored, fully at this far from neutral
uniform float HasTrim;  // 1 when the armor has a trim, drawn over it in a separate pass this glint can't see
uniform vec4 TrimRect;  // the trim's sprite on the trim atlas: corner xy, size zw (it maps the model like the armor)
uniform float RampCount; // colors in the ramp, 0 for none
uniform vec4 Ramp0;      // the ramp's colors (rgb) at their brightness (a), darkest first
uniform vec4 Ramp1;
uniform vec4 Ramp2;
uniform vec4 Ramp3;
uniform vec4 Ramp4;
uniform vec4 Ramp5;
uniform vec4 Ramp6;
uniform vec4 Ramp7;
uniform vec2 RampField;  // the stretch of the ramp an item's black to white spans, outside rays
uniform vec2 RampRay;    // the same, in the brightest part of a ray
uniform float BannerCount; // banner pattern layers over the plate, 0 for none
uniform vec4 BannerPlate;  // the plate's sprite on the atlas (Sampler1): corner xy, size zw
uniform vec4 BannerRect0;
uniform vec4 BannerRect1;
uniform vec4 BannerRect2;
uniform vec4 BannerRect3;
uniform vec4 BannerRect4;
uniform vec4 BannerRect5;
uniform vec4 BannerRect6;
uniform vec4 BannerRect7;
uniform vec4 BannerRect8;
uniform vec4 BannerRect9;
uniform vec4 BannerRect10;
uniform vec4 BannerRect11;
uniform vec4 BannerRect12;
uniform vec4 BannerRect13;
uniform vec4 BannerRect14;
uniform vec4 BannerRect15;
uniform vec4 BannerRect16;
uniform vec3 BannerColor0;
uniform vec3 BannerColor1;
uniform vec3 BannerColor2;
uniform vec3 BannerColor3;
uniform vec3 BannerColor4;
uniform vec3 BannerColor5;
uniform vec3 BannerColor6;
uniform vec3 BannerColor7;
uniform vec3 BannerColor8;
uniform vec3 BannerColor9;
uniform vec3 BannerColor10;
uniform vec3 BannerColor11;
uniform vec3 BannerColor12;
uniform vec3 BannerColor13;
uniform vec3 BannerColor14;
uniform vec3 BannerColor15;
uniform vec3 BannerColor16;

in float vertexDistance;
in vec2 texCoord0;
in vec2 itemCoord;
in vec4 itemTint;
in vec4 itemLight;

out vec4 fragColor;

const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

vec3 rampAt(float position) {
    vec4 stops[8] = vec4[8](Ramp0, Ramp1, Ramp2, Ramp3, Ramp4, Ramp5, Ramp6, Ramp7);
    int count = int(RampCount + 0.5);
    if (position <= stops[0].a) {
        return stops[0].rgb;
    }
    for (int i = 1; i < 8; i++) {
        if (i >= count) {
            break;
        }
        if (position <= stops[i].a) {
            return mix(stops[i - 1].rgb, stops[i].rgb, (position - stops[i - 1].a) / max(stops[i].a - stops[i - 1].a, 0.0001));
        }
    }
    return stops[count - 1].rgb;
}

// The ramp's color for an item of this brightness, that far into a ray.
vec3 ramped(float brightness, float ray) {
    vec2 span = mix(RampField, RampRay, ray);
    return rampAt(mix(span.x, span.y, brightness));
}

// The plate's color with the banner's patterns layered over it, as the banner renderer draws them.
vec3 withBanner(vec3 plate) {
    vec4 rects[17] = vec4[17](BannerRect0, BannerRect1, BannerRect2, BannerRect3, BannerRect4, BannerRect5, BannerRect6, BannerRect7, BannerRect8, BannerRect9, BannerRect10, BannerRect11, BannerRect12, BannerRect13, BannerRect14, BannerRect15, BannerRect16);
    vec3 colors[17] = vec3[17](BannerColor0, BannerColor1, BannerColor2, BannerColor3, BannerColor4, BannerColor5, BannerColor6, BannerColor7, BannerColor8, BannerColor9, BannerColor10, BannerColor11, BannerColor12, BannerColor13, BannerColor14, BannerColor15, BannerColor16);
    vec2 onPlate = (itemCoord - BannerPlate.xy) / BannerPlate.zw;
    int count = int(BannerCount + 0.5);
    for (int i = 0; i < 17; i++) {
        if (i >= count) {
            break;
        }
        vec4 layer = textureLod(Sampler1, rects[i].xy + onPlate * rects[i].zw, 0.0);
        plate = mix(plate, layer.rgb * colors[i], layer.a);
    }
    return plate;
}

void main() {
    vec4 glint = texture(Sampler0, texCoord0) * ColorModulator;
    if (glint.a < 0.1) {
        discard;
    }

    if (Colorize <= 0.0) {
        float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
        if (FadeTo > 0.0) {
            fragColor = vec4(mix(vec3(FadeTo), glint.rgb, fade * Strength), glint.a);
        } else {
            fragColor = vec4(glint.rgb * fade, glint.a);   // exactly vanilla's
        }
        return;
    }

    vec4 item = textureLod(Sampler1, itemCoord, 0.0);
    vec3 base = item.rgb * itemTint.rgb;   // the item's color before lighting
    if (HasTrim > 0.5) {
        vec4 trim = textureLod(Sampler3, TrimRect.xy + itemCoord * TrimRect.zw, 0.0);
        if (trim.a >= 0.1) {               // the trim covers the armor here (it's drawn undyed)
            item.a = 1.0;
            base = trim.rgb;
        }
    }
    if (item.a < 0.1) {
        discard;
    }
    if (BannerCount > 0.5) {
        base = withBanner(base);
    }
    bool ramp = RampCount > 0.5;
    float ray = dot(glint.rgb, LUMA);
    vec3 multiplier = glint.rgb * 2.0;     // a tint glint: 50% gray leaves the item alone
    float brightness = dot(base, LUMA);

    vec3 plain = clamp(base * multiplier, 0.0, 1.0);
    vec3 colorized = ramp ? ramped(brightness, ray) : clamp(brightness * multiplier, 0.0, 1.0);
    // Strength fades from the untouched item to the result. Both ends keep the item's light and dark (lowering
    // Colorize instead brings the plain multiply's uneven brightening back).
    // Selective: colorize turns the item's own color into the glint's, which near-neutral parts of a glint (ones
    // meant to let the item show through) would only turn gray. So there, stay a plain multiply.
    float amount = Colorize;
    if (Selective > 0.0 && !ramp) {
        vec3 away = abs(multiplier - 1.0);
        amount *= smoothstep(0.0, Selective, max(away.r, max(away.g, away.b)));
    }
    vec3 color = mix(base, mix(plain, colorized, amount), Strength) * itemLight.rgb;
    color = linear_fog(vec4(color, 1.0), vertexDistance, FogStart, FogEnd, FogColor).rgb;

    // Replaces the item's color outright. (Glint Strength is held at full for tint glints, like a plain tint;
    // strength 0 turns the glint off in the blend instead.)
    fragColor = vec4(color, 1.0);
}
