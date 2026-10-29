#version 150

// Vanilla's "sobel" vertex shader, for passes that draw into a target smaller than the screen. Vanilla places the
// pass's quad with the screen's projection (ProjMat), so into a smaller target it only covers the bottom-left corner;
// here the quad fills whatever target it draws into.

in vec4 Position;

uniform vec2 InSize;
uniform vec2 OutSize;

out vec2 texCoord;
out vec2 oneTexel;

void main() {
    gl_Position = vec4(Position.xy / OutSize * 2.0 - 1.0, 0.2, 1.0);
    oneTexel = 1.0 / InSize;
    texCoord = Position.xy / OutSize;
}
