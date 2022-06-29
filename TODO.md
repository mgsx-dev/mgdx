
# Demos fixes

Rename shaders with proper extensions :
.vert - a vertex shader
.tesc - a tessellation control shader
.tese - a tessellation evaluation shader
.geom - a geometry shader
.frag - a fragment shader
.comp - a compute shader
https://stackoverflow.com/questions/6432838/what-is-the-correct-file-extension-for-glsl-shaders


# LibGDX fixes

## GLProfiler context

Lwjgl3Graphics#setGL30 should set gl20 as well (like lwjgl2 backend)
Lwjgl3Window#makeCurrent is called before app enabing GLProfiler, so interceptors are not fully set
for the current frame !
