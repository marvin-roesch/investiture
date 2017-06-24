uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform float Time;
uniform float Speed;
uniform vec2 Frequency;
uniform vec2 WobbleAmount;

void main() {
    float xOffset = oneTexel.x * sin(texCoord.y * Frequency.x + Speed * Time * 3.1415926535 * 2.0) * WobbleAmount.x;
    float yOffset = oneTexel.y * cos(texCoord.x * Frequency.y + Speed * Time * 3.1415926535 * 2.0) * WobbleAmount.y;
    vec2 offset = vec2(xOffset, yOffset);
    vec4 rgb = texture2D(DiffuseSampler, texCoord + offset);
    gl_FragColor = rgb;
}