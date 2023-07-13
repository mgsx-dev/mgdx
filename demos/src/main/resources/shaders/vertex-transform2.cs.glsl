#version 430

// Compute Shader SSB Data Structure and Buffer Definition

struct VtxInData {
   vec4  position;
};

layout (std140, binding = 0) buffer srcBuffer {
	VtxInData verts [];
} inBuffer ;

struct VtxOutData {
   vec4  position;
   vec4  color;
};

layout (std140, binding = 1) buffer dstBuffer {
	VtxOutData verts [];
} outBuffer;


layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

uniform float u_time;
uniform float u_amplitude;

void main() {
	VtxInData in_v = inBuffer.verts[gl_GlobalInvocationID.x];
	VtxOutData out_v;

	float angle = (u_time + in_v.position.x * 54.765 + in_v.position.y * 13.123);
	float amp = u_amplitude;
	vec4 pos;
	pos.x = in_v.position.x + sin(angle) * amp;
	pos.y = in_v.position.y + cos(angle) * amp;
	pos.z = 0.0;
	pos.w = 1.0;
	out_v.position = pos;
	out_v.color = vec4(1.0, sin(angle * 2.0) * 0.5 + 0.5, 0.0, 1.0);

	outBuffer.verts[gl_GlobalInvocationID.x] = out_v;
}
