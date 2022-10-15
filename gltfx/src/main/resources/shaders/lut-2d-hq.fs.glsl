// DEPRECATED : use color-grading-lut.fs.glsl

uniform sampler2D u_texture;

uniform sampler2D u_textureLUTR;
uniform sampler2D u_textureLUTG;
uniform sampler2D u_textureLUTB;
uniform float u_dim;
varying vec2 v_texCoords;

const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
#define unpack(v) dot(v, bitShifts);

void main() {
	vec4 colorIn = texture2D(u_texture, v_texCoords);
	 // clamp to avoid linear sampling at edges
	vec3 clampedIn = clamp(colorIn.rgb, 0.5 / u_dim, 1.0 - 1.0 / u_dim);

	vec2 lutCoords = vec2(clampedIn.r / u_dim, clampedIn.g);

	float z0 = floor(clampedIn.b * u_dim) / u_dim;
	float z1 = floor((clampedIn.b * u_dim + 1.0)) / u_dim;
	float z = (clampedIn.b - z0) * u_dim;

	float r0 = unpack(texture2D(u_textureLUTR, vec2(lutCoords.x + z0, lutCoords.y)));
	float r1 = unpack(texture2D(u_textureLUTR, vec2(lutCoords.x + z1, lutCoords.y)));
	float r = mix(r0, r1, z);

	float g0 = unpack(texture2D(u_textureLUTG, vec2(lutCoords.x + z0, lutCoords.y)));
	float g1 = unpack(texture2D(u_textureLUTG, vec2(lutCoords.x + z1, lutCoords.y)));
	float g = mix(g0, g1, z);

	float b0 = unpack(texture2D(u_textureLUTB, vec2(lutCoords.x + z0, lutCoords.y)));
	float b1 = unpack(texture2D(u_textureLUTB, vec2(lutCoords.x + z1, lutCoords.y)));
	float b = mix(b0, b1, z);

    gl_FragColor = vec4(r,g,b, colorIn.a);
}
