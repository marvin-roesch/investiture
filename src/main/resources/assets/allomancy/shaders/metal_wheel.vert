#version 120

uniform vec2 corner;
uniform float scale;
uniform float time;

varying vec3 pos;

void main() {
    gl_FrontColor = gl_Color;
    vec4 vertex = gl_Vertex;
    pos = vec3(vec2(vertex.x, vertex.y) / scale - vec2(corner.x, corner.y), time);
    gl_Position = gl_ModelViewProjectionMatrix * vertex;
}
