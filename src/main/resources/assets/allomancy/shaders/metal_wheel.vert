#version 120

uniform vec2 corner;
uniform float scale;
uniform float time;

varying vec3 pos;

// Shader entry point
void main()
{
    // Pass through the color
    gl_FrontColor = gl_Color;
    vec4 vertex = gl_Vertex;

    // The position has to be scaled down according to the player's settings and
    // translated such that it always is relative to the wheel's corner
    pos = vec3(vec2(vertex.x, vertex.y) / scale - vec2(corner.x, corner.y), time);

    // Pass through the position
    gl_Position = gl_ModelViewProjectionMatrix * vertex;
}
