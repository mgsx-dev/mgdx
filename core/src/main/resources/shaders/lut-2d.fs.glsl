// #version 130

uniform sampler2D u_texture;
uniform sampler2D u_textureLUT;
uniform float u_dim;
varying vec2 v_texCoords;

void main() {
	vec4 colorIn = texture2D(u_texture, v_texCoords);
	 // clamp to avoid linear sampling at edges
	vec3 clampedIn = clamp(colorIn.rgb, 0.5 / u_dim, 1.0 - 1.0 / u_dim);

	vec2 lutCoords = vec2(clampedIn.r / u_dim, clampedIn.g);

	float z0 = floor(clampedIn.b * u_dim) / u_dim;
	float z1 = floor((clampedIn.b * u_dim + 1.0)) / u_dim;

	vec3 colorOut0 = texture2D(u_textureLUT, vec2(lutCoords.x + z0, lutCoords.y)).rgb;
	vec3 colorOut1 = texture2D(u_textureLUT, vec2(lutCoords.x + z1, lutCoords.y)).rgb;
	vec3 colorOut = mix(colorOut0, colorOut1, (clampedIn.b - z0) * u_dim);

    gl_FragColor = vec4(colorOut, colorIn.a);
}
