#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
#ifdef GLSL3
#define varying in
#define textureCube texture
#define texture2D texture
out vec4 out_FragColor;
#else
#define out_FragColor gl_FragColor
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_textureCavityScreen;
uniform sampler2D u_textureCavityWorld;
uniform vec4 u_mix;

void main() {

    vec4 base = texture2D(u_texture, v_texCoords);
    vec4 cavityScreen = texture2D(u_textureCavityScreen, v_texCoords);
    vec4 cavityWorld = texture2D(u_textureCavityWorld, v_texCoords);

    // gamma correction
    vec3 colorGC = base.rgb; // pow(base.rgb, vec3(1.0/2.2));

    float cavitySignedS = (cavityScreen.r * 2.0 - 1.0);
    float valleyS = 1.0 - max(0.0, -cavitySignedS);
    float ridgeS = max(0.0, cavitySignedS);

    float ridgeFactorS = mix(0.0, ridgeS, u_mix.x);
    float valleyFactorS = mix(1.0, valleyS, u_mix.y);

    float cavitySignedW = (cavityWorld.r * 2.0 - 1.0);
    float valleyW = 1.0 - max(0.0, -cavitySignedW);
    float ridgeW = max(0.0, cavitySignedW);

    float ridgeFactorW = mix(0.0, ridgeW, u_mix.z);
    float valleyFactorW = mix(1.0, valleyW, u_mix.w);

#ifdef RIDGE_BURN
    vec3 color = (colorGC * (1.0 + ridgeFactorS + ridgeFactorW)) * valleyFactorS * valleyFactorW;
#else
    vec3 color = (colorGC + ridgeFactorS + ridgeFactorW) * valleyFactorS * valleyFactorW;
#endif

    out_FragColor = vec4(color, base.a);
}
