
# GLTF Composer

## Use cases

* Create HDRI environment in Blender and export for a game
	* [ ] How to export from Blender (which formats, write documentation)
* Prepare IBL for a game
	* [ ] export HDRI as cubemaps in various formats
		* [x] to KTX2
		* [ ] to EXR
* Inspect scenes
	* [x] model browser
	* [x] profiling
* Preview scenes
	* [x] Load IBLs from HDRI or baked IBLs
	* [x] Emissive bloom (emissive boost by tweaking materials)
	* [ ] test some post processing effects 

## IBL Composer features

* [ ] UI scale
	* [ ] CTRL +/- to zoom in/out ?
	* [x] automatic detection
	* [ ] save to preferences
* [ ] Copy light code
* [ ] change light color
* [ ] load simple model (sphere) with basic material (metal roughness)
* [x] GPU memory statistics
* [~] Export IBL to files (png, jpg, ktx2, ktx, exr?)
* [~] Import HDR with base exposure, gamma correction ?

## GLTF Viewer features

* [ ] save to file ?
* [x] import other formats (glb)
* [ ] change shader : PBR, Gouraud, Ceil shading
* [ ] fog
* [ ] skybox on/off, skybox rotation
* [ ] outlines
* [x] shadows
* [ ] use scene camera (with animations)
* [ ] show skeleton (overlay)
* [x] material textures (on/off, etc..)
* [ ] show only selected node
* [x] fit to scene on loading

## New features

* [x] Blender like cameras control
* [x] Scene structure browser
* [x] pickup skybox to configure key light direction
* [x] vsync, FPS, profiler (new system tab)
* [~] proxy based profiler (wip)
* [ ] F1 to get help, key bindings, etc..
* [x] generate skin from GIMP (acting also as example for community)
* [ ] MSAA
* [ ] FXAA
* [ ] MSAA frame buffers
* MRT based effects :
	* [ ] Deferred lighting
	* [ ] SSAO
	* [ ] SSR
	* [ ] Blender "Cavity"
* HDR rendering :
	* [~] HDR Bloom
		* [ ] fix blur artifacts
	* [x] Tone mapping

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
