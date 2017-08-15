#version 120

uniform sampler2D tex;
uniform sampler2D noiseTex;
uniform float windowWidth;
uniform float windowHeight;
uniform float speed;
uniform float strength;

// Shader entry point
void main()
{
    vec4 pos = gl_FragCoord;
    vec2 noiseUV = gl_TexCoord[0].st;
    vec2 uv = pos.xy / vec2(windowWidth, windowHeight);
    float ticks = speed;
    vec4 noisePixel1 = texture2D(noiseTex, uv.st * 4 + vec2(ticks * 0.324823048, ticks * 0.48913801));
    vec4 noisePixel2 = texture2D(noiseTex, uv.ts * 4 + vec2(ticks * 0.52890348, ticks * 0.6318212));
    vec4 joinedNoise = noisePixel1 + noisePixel2 - 1.0;
    vec4 newColor = texture2D(tex, uv + joinedNoise.rg * strength);
    newColor.a = 1.0;
    gl_FragColor = newColor;
}
