attribute vec4 a_position;
attribute vec4 a_color;
varying vec4 v_color;

void main()
{
	// swap R and G
    v_color = vec4(a_color.g, a_color.r, a_color.b, a_color.a);
    gl_Position = a_position;
}
