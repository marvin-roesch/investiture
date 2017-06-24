#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

void main(){
    float x = oneTexel.x;
    float y = oneTexel.y;
    vec4 center = texture2D( DiffuseSampler, texCoord );
    vec4 horizEdge = vec4( 0.0 );
    horizEdge -= texture2D( DiffuseSampler, vec2( texCoord.x - x, texCoord.y - y ) ) * 1.0;
    horizEdge -= texture2D( DiffuseSampler, vec2( texCoord.x - x, texCoord.y     ) ) * 2.0;
    horizEdge -= texture2D( DiffuseSampler, vec2( texCoord.x - x, texCoord.y + y ) ) * 1.0;
    horizEdge += texture2D( DiffuseSampler, vec2( texCoord.x + x, texCoord.y - y ) ) * 1.0;
    horizEdge += texture2D( DiffuseSampler, vec2( texCoord.x + x, texCoord.y     ) ) * 2.0;
    horizEdge += texture2D( DiffuseSampler, vec2( texCoord.x + x, texCoord.y + y ) ) * 1.0;
    vec4 vertEdge = vec4( 0.0 );
    vertEdge -= texture2D( DiffuseSampler, vec2( texCoord.x - x, texCoord.y - y ) ) * 1.0;
    vertEdge -= texture2D( DiffuseSampler, vec2( texCoord.x    , texCoord.y - y ) ) * 2.0;
    vertEdge -= texture2D( DiffuseSampler, vec2( texCoord.x + x, texCoord.y - y ) ) * 1.0;
    vertEdge += texture2D( DiffuseSampler, vec2( texCoord.x - x, texCoord.y + y ) ) * 1.0;
    vertEdge += texture2D( DiffuseSampler, vec2( texCoord.x    , texCoord.y + y ) ) * 2.0;
    vertEdge += texture2D( DiffuseSampler, vec2( texCoord.x + x, texCoord.y + y ) ) * 1.0;
    vec4 edge = sqrt((horizEdge * horizEdge) + (vertEdge * vertEdge));

    gl_FragColor = edge;
}
