
# Disclamer

**Never ready for production !** This repository only contains experimental features.

# Features

## LibGDX extensions

* GL30 support
	* Texture3D
	* Transform feedback
* GL31 support
	* Compute shaders
	* Framebuffer multisample
	* Program pipeline
* GL32 support
	* Geometry shaders
	* Tesselation shaders
	* MRT Blending
	* Advanced debugging
* GLMax support (Desktop only allowing access to missing OpenGL features)
* Format support
	* HDRI
	* EXR
	* KTX2
* UI
	* TabPane
	* Convenient utility using java lambda
* Shaders
	* Tone mapping (Reinhard, exposure, and gamma compression)
	* Bloom / Glow / Blur cascade

## GLTF Composer

A new application that will somehow replace GLTF Demo and IBL Composer, see [work in progress](TODO.md).

Usage :

* requires JRE 8+
* F11 : toggle fullscreen
* F12 : switch between GLTFComposer and Demos test suite (draft)
* Drop files : *.hdr, *.ktx2, *.gltf, *.glb


## IBL

### HDR File formats

* HDR import (all platforms)
* EXR import/export (desktop only)
* KTX2 import/export (all platforms)

Command line tools :
* HdrToExr
* HdrToKtx2

Benchmark : using https://polyhaven.com/a/monks_forest (2k, 2048x1024, hdr) as reference : 

| **Format** | **Compression** | **Size** | **Loading time** |
|-----------------------|:---------:|----------:|-------------:|
| HDR (original) 		| RGBE 		|  7.6 MB 	| 123ms |
| EXR float 32 bits		| NONE		| 25.2 MB 	| |
| EXR int 32 bits		| NONE 		| 25.2 MB 	| |
| EXR float 16 bits		| NONE 		| 12.6 MB 	| |
| EXR float 16 bits		| ZIP 		|  7.2 MB 	| |
| EXR float 16 bits		| PIZ 		|  6.7 MB 	| 220ms |
| EXR float 16 bits		| RLE 		| 12.2 MB 	| |
| EXR float 16 bits		| ZIPS 		|  7.7 MB 	| |
| EXR float 16 bits		| ZFP 		|  --- MB 	| |
| KTX2 float 32 bits 	| ZLIB		|  9.3 MB 	| 159ms |
| KTX2 float 16 bits 	| ZLIB		|  8.3 MB 	| 126ms |
| KTX2 Cubemap 6x1k 	|  ???		| ?? 		| |


notes:
* ZFP compression is specific to TinyEXR lib and has limitations: https://github.com/syoyo/tinyexr#zfp
* ZFP compression is not included in Lwjgl build and hasn't been tested.

