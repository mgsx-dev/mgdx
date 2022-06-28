#version 430

uniform sampler2D img_in;

layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

layout(rgba8, binding = 0) coherent uniform image2D img_output;

uniform vec2 u_mix;

void main() {
	ivec2 pixel_coords = ivec2(gl_GlobalInvocationID.xy);

	ivec2 input_base = pixel_coords / 2;
	vec4 color1 = texelFetch(img_in, ivec2(input_base.x - 1, input_base.y - 1), 0);
	vec4 color2 = texelFetch(img_in, ivec2(input_base.x + 1, input_base.y - 1), 0);
	vec4 color3 = texelFetch(img_in, ivec2(input_base.x - 1, input_base.y + 1), 0);
	vec4 color4 = texelFetch(img_in, ivec2(input_base.x + 1, input_base.y + 1), 0);

	vec4 color = (color1 + color2 + color3 + color4) / 4.0;
	vec4 base = imageLoad(img_output, pixel_coords);

	color = base* u_mix.x + color * u_mix.y;

	imageStore(img_output, pixel_coords, color);
}
