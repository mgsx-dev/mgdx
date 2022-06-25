#version 150

#if __VERSION__ < 330
#extension GL_ARB_explicit_attrib_location : enable
#endif

layout(location = 0) out vec4 out_FragColor0;
layout(location = 1) out vec4 out_FragColor1;
layout(location = 2) out vec4 out_FragColor2;
layout(location = 3) out vec4 out_FragColor3;
layout(location = 4) out vec4 out_FragColor4;
layout(location = 5) out vec4 out_FragColor5;
layout(location = 6) out vec4 out_FragColor6;
layout(location = 7) out vec4 out_FragColor7;

in vec4 v_color;

void main() {
	out_FragColor0 = v_color;
	out_FragColor1 = v_color;
	out_FragColor2 = v_color;
	out_FragColor3 = v_color;
	out_FragColor4 = v_color;
	out_FragColor5 = v_color;
	out_FragColor6 = v_color;
	out_FragColor7 = v_color;
}
