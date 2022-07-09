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
uniform sampler2D u_textureNormals;
uniform sampler2D u_texturePosition;
uniform vec2 u_sampleDistance;

void main() {

    vec4 base = texture2D(u_texture, v_texCoords);
    vec4 normal = texture2D(u_textureNormals, v_texCoords);
    vec3 position = texture2D(u_texturePosition, v_texCoords).rgb;

    vec2 dx = vec2(u_sampleDistance.x, 0.0);
    vec2 dy = vec2(0.0, u_sampleDistance.y);

    // position convolution
    vec3 positionPX = texture2D(u_texturePosition, v_texCoords + dx).rgb;
    vec3 positionNX = texture2D(u_texturePosition, v_texCoords - dx).rgb;
    vec3 positionPY = texture2D(u_texturePosition, v_texCoords + dy).rgb;
    vec3 positionNY = texture2D(u_texturePosition, v_texCoords - dy).rgb;

    vec3 deltaX = normalize(positionPX - positionNX);
    vec3 deltaY = normalize(positionPY - positionNY);

    // normals convolution
    vec3 normalPX = texture2D(u_textureNormals, v_texCoords + dx).rgb;
    vec3 normalNX = texture2D(u_textureNormals, v_texCoords - dx).rgb;
    vec3 normalPY = texture2D(u_textureNormals, v_texCoords + dy).rgb;
    vec3 normalNY = texture2D(u_textureNormals, v_texCoords - dy).rgb;

    // compute cavity
    vec3 ndx = normalPX - normalNX;
    float dnx = dot(deltaX, ndx);

    vec3 ndy = normalPY - normalNY;
    float dny = dot(deltaY, ndy);

    float dn = (dnx + dny) * 0.5;
    dn = (dnx + dny) * 0.5;

    out_FragColor = vec4(vec3(dn * 0.5 + 0.5), base.a);
}
