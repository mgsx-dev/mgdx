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
uniform vec2 u_dir;
uniform float u_fade;

// inspired by https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson5

void main() {
    //this will be our RGBA sum
    vec4 sum = vec4(0.0);
    float hstep = u_dir.x;
    float vstep = u_dir.y;

    //apply blurring, using a 9-tap filter with predefined gaussian weights

    vec4 middle = texture2D(u_texture, v_texCoords);

    sum += texture2D(u_texture, vec2(v_texCoords.x - 4.0*hstep, v_texCoords.y - 4.0*vstep)) * 0.0162162162;
    sum += texture2D(u_texture, vec2(v_texCoords.x - 3.0*hstep, v_texCoords.y - 3.0*vstep)) * 0.0540540541;
    sum += texture2D(u_texture, vec2(v_texCoords.x - 2.0*hstep, v_texCoords.y - 2.0*vstep)) * 0.1216216216;
    sum += texture2D(u_texture, vec2(v_texCoords.x - 1.0*hstep, v_texCoords.y - 1.0*vstep)) * 0.1945945946;

    sum += middle * 0.2270270270;

    sum += texture2D(u_texture, vec2(v_texCoords.x + 1.0*hstep, v_texCoords.y + 1.0*vstep)) * 0.1945945946;
    sum += texture2D(u_texture, vec2(v_texCoords.x + 2.0*hstep, v_texCoords.y + 2.0*vstep)) * 0.1216216216;
    sum += texture2D(u_texture, vec2(v_texCoords.x + 3.0*hstep, v_texCoords.y + 3.0*vstep)) * 0.0540540541;
    sum += texture2D(u_texture, vec2(v_texCoords.x + 4.0*hstep, v_texCoords.y + 4.0*vstep)) * 0.0162162162;

    //discard alpha for our simple demo, multiply by vertex color and return
#ifdef NO_CLIP
    out_FragColor = sum * u_fade;
#else
#ifdef ALPHA_CLIP
    out_FragColor = vec4(min(vec3(1.0, 1.0, 1.0), sum.rgb * u_fade), middle.a);
#else
    out_FragColor = vec4(min(vec3(1.0, 1.0, 1.0), sum.rgb * u_fade), 1.0);
#endif
#endif
}
