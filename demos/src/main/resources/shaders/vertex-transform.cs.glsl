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

void main() {
	VtxInData in_v = inBuffer.verts[gl_GlobalInvocationID.x];
	VtxOutData out_v;

	out_v.position = in_v.position;
	// out_v.position.x = fract(u_time);
	out_v.color = vec4(in_v.position);
	out_v.color.r = fract(u_time);
	out_v.color.a = 1.0;

	outBuffer.verts[gl_GlobalInvocationID.x] = out_v;
}
