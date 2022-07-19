
# GLTF Composer

## Use cases

* Create HDRI environment in Blender and export for a game
	* [x] How to export from Blender (write documentation)
* Prepare IBL for a game
	* [x] export HDRI as cubemaps in various formats
		* [x] to KTX2 (HDR)
		* [x] to PNGs (LDR)
* Inspect scenes
	* [x] model browser
	* [x] profiling
* Preview scenes
	* [x] Load IBLs from HDRI or baked IBLs
	* [x] Emissive bloom
	* [x] test some post processing effects 

## IBL Composer features

* [ ] UI scale
	* [ ] CTRL +/- to zoom in/out ?
	* [x] automatic detection
	* [ ] save to preferences
* [x] Copy light code
* [x] change light color
* [x] load simple model (sphere) with basic material (metal roughness)
* [x] GPU memory statistics
* [x] Export IBL to files (png)
* [~] Import HDR with base exposure, gamma correction ? (useful for LDR targets)

## GLTF Viewer features (complete)

* [x] save to file ?
* [x] import other formats (glb)
* [x] change shader
	* [x] PBR
	* [x] Gouraud
	* [x] Ceil shading
* [x] fog
* [x] skybox on/off
* [x] outlines
* [x] shadows
* [x] use scene camera (with animations)
* [x] show skeleton (overlay)
* [x] material textures (on/off, etc..)
* [x] show only selected node
* [x] fit to scene on loading
* [x] starts with a default IBL

## New features

* [x] Skybox rotation
* [x] IBL rotation
* [x] Ambient strenth and skybox opacity
* [x] Skybox blur
* [ ] better skybox blur (gaussian mipmaps generation)
* [x] Blender like cameras control
* [x] Scene structure browser
* [x] pickup skybox to configure key light direction
* [x] vsync, FPS, profiler (new system tab)
* [~] proxy based profiler (wip)
* [ ] F1 to get help, key bindings, etc..
* [x] generate skin from GIMP (acting also as example for community)
* [ ] IBL export
	* [x] ktx2
	* [ ] exr ?
	* [ ] ktx ?
* [x] MSAA
* [x] FXAA
* [x] MSAA frame buffers
* [ ] MRT based effects :
	* [ ] Deferred lighting (?)
	* [ ] SSAO
	* [ ] SSR
	* [x] Blender "Cavity"
* [x] HDR rendering :
	* [x] HDR Bloom
		* [x] fix blur artifacts
	* [x] Tone mapping
	* [x] HDR particles
* [x] color management
	* [x] color grading with LUT texture
	* [x] cube file loading

## Users feedback

* [ ] minimize window crash FBO construction.
* [ ] center camera to visible area when UI is on
* [ ] Blender camera : allow both middle mouse button and left button
* [ ] Model tree cutted out on the right (scroll pane bug ?)
* [ ] display scroll bars when using scroll pane (model browser)
* [ ] condense material view when there is a lot of textures
* [ ] add a scroll pane to the material view
* [ ] UI freeze for some heavy process :
	* [ ] IBL import/baking freeze the UI : use thread (AssetManager ?) at least to load files
	* [ ] Big model loading
	* [ ] IBL export : use threads to avoid UI freeze
* [ ] animation play/stop should be off when seeking in the animation
* [ ] animation speed range a bit too high (0.1 to 50 could be enough)
* [ ] move first tab content to a help popup (F1 + button)
* [ ] HDPI : text is blurry, would need an HDPI font (HDPI skin ?)

## cleanup

* [ ] remove cubemap-make.fs/vs.glsl they are the same as the one in ibl-composer

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

## Decals

DecalMaterial missing a getter for texture.
