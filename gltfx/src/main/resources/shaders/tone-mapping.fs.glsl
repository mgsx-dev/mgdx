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
layout(location = 0) out vec4 out_FragColor;
#else
#define out_FragColor gl_FragColor
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

// inspired by https://learnopengl.com/Advanced-Lighting/HDR

#ifdef EXPOSURE
uniform float u_exposure;
#endif

#ifdef GAMMA_COMPRESSION
uniform float u_luminosity;
uniform float u_contrast;
#endif

void main() {

    vec3 hdrColor = texture2D(u_texture, v_texCoords).rgb;

#if defined(EXPOSURE)
    // exposure tone mapping
    vec3 ldrColor = vec3(1.0) - exp(hdrColor * -u_exposure);
#elif defined(REINHARD)
    // reinhard tone mapping
    vec3 ldrColor = hdrColor / (hdrColor + vec3(1.0));
#elif defined(GAMMA_COMPRESSION)
    vec3 ldrColor = u_luminosity * pow(hdrColor, vec3(u_contrast));
#else
    vec3 ldrColor = clamp(hdrColor, 0.0, 1.0);
#endif

    // gamma correction
#ifdef GAMMA_CORRECTION
    out_FragColor = vec4(pow(ldrColor, vec3(1.0/GAMMA_CORRECTION)), 1.0);
#else
    out_FragColor = vec4(ldrColor, 1.0);
#endif
}
