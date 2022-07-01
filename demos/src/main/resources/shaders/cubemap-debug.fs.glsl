varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform samplerCube u_textureCube;

#define PI 3.141592653589793

#ifdef MIPMAP
uniform float u_bias;
#endif

uniform mat4 u_offset;

void main() {
	vec4 base = texture2D(u_texture, v_texCoords);

	// equi to sphere
	float ax = PI * 2.0 * v_texCoords.x;
	float ay = PI * v_texCoords.y;
	float x = cos(ax) * sin(ay);
	float y = sin(ax) * sin(ay);
	float z = -cos(ay);

	vec3 dir = (u_offset * vec4(x,y,z,0.0)).xyz;

	dir = normalize(dir);

#ifdef MIPMAP
	vec4 color = textureCube(u_textureCube, dir, u_bias);
#else
	vec4 color = textureCube(u_textureCube, dir);
#endif

	// TODO configurable

	// tone mapping (Reinhard)
	// color.rgb = color.rgb / (color.rgb + vec3(1.0));

	// gamma correction
	// color.rgb = pow(color.rgb, vec3(1.0/2.2));

    gl_FragColor = base * color;
}
