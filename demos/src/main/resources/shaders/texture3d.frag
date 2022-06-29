// #version 130

uniform sampler3D u_texture;
varying vec3 v_position;

void main() {
	vec3 color = texture3D(u_texture, v_position * 0.5 + vec3(0.5)).rgb;

    gl_FragColor = vec4(color, 1.0);
}
