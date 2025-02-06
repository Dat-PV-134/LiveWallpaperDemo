#version 320 es

precision mediump float;  // Define precision for float types

in vec4 ourColor;
out vec4 FragColor;

void main() {
    FragColor = ourColor;
}
