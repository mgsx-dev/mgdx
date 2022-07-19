// #version 130

uniform sampler2D u_texture;
uniform sampler3D u_textureLUT;
varying vec2 v_texCoords;

void main() {
	vec4 colorIn = texture2D(u_texture, v_texCoords);

	vec3 colorOut = texture3D(u_textureLUT, clamp(colorIn.rgb, 0.0, 1.0)).rgb;

    gl_FragColor = vec4(colorOut, colorIn.a);
}
