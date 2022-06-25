varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
	float a = texture2D(u_texture, v_texCoords).a;
    gl_FragColor = v_color * vec4(a, a, a, 1.0);
}
