#version 430
layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;
layout(rgba8, binding = 0) uniform image2D img_output;
uniform float u_time;

#define usin(_v) (sin(_v)*0.5+0.5)

void main() {
	ivec2 pixel_coords = ivec2(gl_GlobalInvocationID.xy);
	ivec2 image_size = imageSize(img_output);
	vec2 coords = vec2(float(pixel_coords.x) / float(image_size.x), float(pixel_coords.y) / float(image_size.y));

	vec4 color = vec4(coords.x + usin(u_time), coords.y + usin(u_time + 0.25), usin(u_time + 0.75), 1.0);

	imageStore(img_output, pixel_coords, color);
}
