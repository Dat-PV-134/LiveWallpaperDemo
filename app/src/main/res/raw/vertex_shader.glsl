#version 320 es

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec4 aColor;

out vec4 ourColor;

void main() {
    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
    ourColor = aColor;
}
