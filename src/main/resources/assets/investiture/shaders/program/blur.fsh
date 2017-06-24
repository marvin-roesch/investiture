#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

float gauss(float x, float sigma) {
    return (1.0 / sqrt(6.283185307179586 * sigma * sigma)) * exp(-(x * x) / (2.0 * sigma * sigma));
}

void main() {
    int range = int(Radius) * 3;
    float sigma = Radius / 2.0;
    vec4 value = vec4(0.0);
    for (int offset = -range; offset <= range; offset++) {
        float offsetF = float(offset);
        vec2 lColorTexCoord = texCoord;
        lColorTexCoord += vec2(offsetF) * oneTexel * BlurDir;
        vec4 x = lColorTexCoord.x >= 0.0 &&
            lColorTexCoord.x <= 1.0 &&
            lColorTexCoord.y >= 0.0 &&
            lColorTexCoord.y <= 1.0 ?
            texture2D(DiffuseSampler, lColorTexCoord) :
            vec4(0.0);

        // Alpha must be premultiplied in order to properly blur the alpha channel.
        value += vec4(x.rgb, x.a) * gauss(offsetF, sigma);
    }

    // Unpremultiply the alpha.
    value = vec4(value.rgb / value.a, value.a);
    gl_FragColor = value;
}
