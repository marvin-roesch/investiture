#version 120

uniform sampler2D tex;
uniform float deltaBrightness;

uniform vec3 backColour;
uniform vec3 impurityColour;
uniform vec3 metalColour;
uniform vec3 hoveredColour;

uniform bool hovered;

uniform float metalLevel;
uniform float impurityLevel;

// Converts a color from RGB space to HSV space
vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

// Converts a color from HSV space to RGB space
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// Shader entry point
void main()
{
    vec2 texCoord = vec2(gl_TexCoord[0]);
    vec4 colour = texture2D(tex, texCoord);

    // Take all provided colors and darken them for the graidnet
    vec3 backHSV = rgb2hsv(backColour);
    vec3 backColourEnd = hsv2rgb(vec3(backHSV.x, backHSV.y, backHSV.z - deltaBrightness));

    vec3 metalHSV = rgb2hsv(metalColour);
    vec3 metalColourEnd = hsv2rgb(vec3(metalHSV.x, metalHSV.y, metalHSV.z - deltaBrightness));

    vec3 impurityHSV = rgb2hsv(impurityColour);
    vec3 impurityColourEnd = hsv2rgb(vec3(impurityHSV.x, impurityHSV.y, impurityHSV.z - deltaBrightness));

    vec3 hoverHSV = rgb2hsv(hoveredColour);
    vec3 hoverColourEnd = hsv2rgb(vec3(hoverHSV.x, hoverHSV.y, hoverHSV.z - deltaBrightness));

    vec3 colourStart = vec3(0, 0, 0);
    vec3 colourEnd = vec3(0, 0, 0);


    if(!hovered && (1 - texCoord.y) <= metalLevel)
    {
        // The UVs are beyond the metal level, so use the metal colour
        colourStart = metalColour;
        colourEnd = metalColourEnd;
    }
    else if(!hovered && (1 - texCoord.y - metalLevel) <= impurityLevel)
    {
        // The UVs are beyond the impurity level but above the metal one, so use the impurity colour
        colourStart = impurityColour;
        colourEnd = impurityColourEnd;
    }
    else
    {
        // The UVs are not in the range of any level, so use the default ones
        colourStart = hovered ? hoveredColour : backColour;
        colourEnd = hovered ? hoverColourEnd : backColourEnd;
    }

    // Calculate a gradient from top left to bottom right
    float a = texCoord.x + texCoord.y;
    float b = 2;
    float u = a / b;
	gl_FragColor = vec4(colourStart * (1 -u), colour.a) + vec4(colourEnd * u, colour.a);
}