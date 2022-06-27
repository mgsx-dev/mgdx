#version 430

// Compute Shader SSB Data Structure and Buffer Definition

struct VtxInData {
   vec3  position;
};

layout (std140, binding = 0) buffer srcBuffer {
	VtxInData verts [];
} inBuffer ;

struct VtxOutData {
   vec3  position;
   vec4  color;
};

layout (std140, binding = 1) buffer dstBuffer {
	VtxOutData verts [];
} outBuffer;


layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

uniform float u_time;

void main() {
	VtxInData in_v = inBuffer.verts[gl_GlobalInvocationID.x];

	VtxOutData out_v = outBuffer.verts[gl_GlobalInvocationID.x];

	out_v.position = in_v.position + vec3(0.5, 0.0, 0.0);
	out_v.color = vec4(0.0, 0.0, fract(u_time), 1.0);
}
