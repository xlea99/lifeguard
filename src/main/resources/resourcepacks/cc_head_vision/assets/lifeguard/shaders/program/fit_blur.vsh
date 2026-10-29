#version 150

// Vanilla's "blur" vertex shader (for box_blur), with the quad filling its target like fit.vsh.

in vec4 Position;

uniform vec2 InSize;
uniform vec2 OutSize;
uniform vec2 BlurDir;

out vec2 texCoord;
out vec2 sampleStep;

void main() {
    gl_Position = vec4(Position.xy / OutSize * 2.0 - 1.0, 0.2, 1.0);
    sampleStep = BlurDir / InSize;
    texCoord = Position.xy / OutSize;
}
