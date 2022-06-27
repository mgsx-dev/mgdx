#version 430

layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

struct VtxData {
   vec3  position;
};

layout (std140, binding = 0) buffer ProcBuffer {
	VtxData verts [];
} procBuffer;

uniform float u_time;

#define usin(_v) (sin(_v)*0.5+0.5)

void main() {
	VtxData in_v = procBuffer.verts[gl_GlobalInvocationID.x];
	in_v.position = vec3(usin(u_time), 0.0, 0.0);
	procBuffer.verts[gl_GlobalInvocationID.x] = in_v;
}
