#version 120

uniform sampler2D tex;
uniform sampler2D noiseTex;
uniform float windowWidth;
uniform float windowHeight;
uniform float ticks;
uniform float strength;

// Shader entry point
void main()
{
    vec2 texPos = gl_TexCoord[0].st;
    vec4 pos = gl_FragCoord;
    vec2 uv = pos.xy / vec2(windowWidth, windowHeight);
//    vec4 noisePixel1 = texture2D(noiseTex, gl_TexCoord[0].st);
//    vec4 noisePixel2 = texture2D(noiseTex, gl_TexCoord[0].ts);
//    vec4 joinedNoise = noisePixel1 + noisePixel2 - 1;
    float s = sin(texPos.x * 2.0 * 3.1415926);
    float c = 0;
    vec2 off = vec2(s, c);
    vec4 newColor = texture2D(tex, uv + off * strength);
//    vec4 newColor = texture2D(tex, uv + vec2(0.015, 0.015));
    newColor.a = 1;
    gl_FragColor = newColor * vec4(1.0, 0.5, 0.5, 1);
}
