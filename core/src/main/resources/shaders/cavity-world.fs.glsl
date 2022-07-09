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
uniform sampler2D u_textureNoise;
uniform vec2 u_sampleDistance;
uniform int u_samples;

void main() {

    vec4 base = texture2D(u_texture, v_texCoords);
    vec3 normal = texture2D(u_textureNormals, v_texCoords).rgb;
    vec3 position = texture2D(u_texturePosition, v_texCoords).rgb;

    float accum = 0.0;
    for(int i=0 ; i<u_samples ; i++){
    	float seed = float(i) / float(u_samples);
    	vec4 noise = texture2D(u_textureNoise, vec2(seed  + position.x + position.y, seed + position.z - position.y));
    	vec3 displace = (noise.xyz * 2.0 - 0.5) * noise.a;
    	vec2 vc = v_texCoords + u_sampleDistance * displace.rg;
    	vec3 pos = texture2D(u_texturePosition, vc).rgb;
    	vec3 nor = texture2D(u_textureNormals, vc).rgb;
    	vec3 dif = pos - position;
    	vec3 dir = normalize(dif);
    	float d = dot(dir, normal);
    	float n = dot(nor, normal) * 0.5 + 0.5;
    	accum += -d * (1.0 - n);
    }
    accum *= 10.0 / float(u_samples);

    accum = clamp(accum, -1.0, 1.0) * 0.5 + 0.5;

    out_FragColor = vec4(vec3(accum), base.a);
}
