#version 120

uniform int   numStops;
uniform vec4  colors[16];
uniform float stops[16];

void main() {
    vec2 texCoord = vec2(gl_TexCoord[0]);
//
//    gl_FragColor = mix(colors[0], colors[1], smoothstep(stops[0], stops[1], texCoord.x));
//    for (int i = 1; i < numStops - 1; ++i) {
//        gl_FragColor = mix(gl_FragColor, colors[i+1], smoothstep(stops[i], stops[i], texCoord.s));
//    }
    gl_FragColor = vec4(0, 0.17647f, 0.62352f, 1f);
}
