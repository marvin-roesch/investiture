#version 120

uniform sampler2D tex;
uniform float deltaBrightness;

uniform vec3 backColor;
uniform vec3 impurityColor;
uniform vec3 metalColor;
uniform vec3 hoveredColor;

uniform bool hovered;

uniform float metalLevel;
uniform float impurityLevel;

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 texcoord = vec2(gl_TexCoord[0]);
    vec4 color = texture2D(tex, texcoord);

    vec3 backHSV = rgb2hsv(backColor);
    vec3 backColorEnd = hsv2rgb(vec3(backHSV.x, backHSV.y, backHSV.z - deltaBrightness));

    vec3 metalHSV = rgb2hsv(metalColor);
    vec3 metalColorEnd = hsv2rgb(vec3(metalHSV.x, metalHSV.y, metalHSV.z - deltaBrightness));

    vec3 impurityHSV = rgb2hsv(impurityColor);
    vec3 impurityColorEnd = hsv2rgb(vec3(impurityHSV.x, impurityHSV.y, impurityHSV.z - deltaBrightness));

    vec3 hoverHSV = rgb2hsv(hoveredColor);
    vec3 hoverColorEnd = hsv2rgb(vec3(hoverHSV.x, hoverHSV.y, hoverHSV.z - deltaBrightness));

    vec3 colorTopLeft = vec3(0, 0, 0);
    vec3 colorBottomRight = vec3(0, 0, 0);

    if((1 - texcoord.y) <= metalLevel) {
        colorTopLeft = metalColor;
        colorBottomRight = metalColorEnd;
    } else if((1 - texcoord.y - metalLevel) <= impurityLevel) {
        colorTopLeft = impurityColor;
        colorBottomRight = impurityColorEnd;
    } else {
        colorTopLeft = backColor;
        colorBottomRight = backColorEnd;
    }

    float a = texcoord.x + texcoord.y;
    float b = 2;
    float u = a / b;
	gl_FragColor = vec4(colorTopLeft * (1 -u), color.a) + vec4(colorBottomRight * u, color.a);
}