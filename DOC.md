# GLTF Composer

## Create your own skybox and IBL in Blender

With Blendr you can render your scene as 360° and export to an HDR file. You can then drop this file into GLTF Composer or load it in your game. You could then convert them later to ktx2 in order to speed up loading.

### Base settings

Few settings are required to export as HDR file in Blender: 

* Render properties
	* Render engine : Cycles
	* Device : GPU compute (if supported)
	* Sampling
		* Path tracing Render : final quality (it's up to your machine and your time)
		* Path tracing Viewport : preview quality (better start at 1)
	* Denoising
		* Render : OpenImageDenoiser (the best for my setup)
		* Viewport : automatic (the best for my setup)

* Output settings
	* Dimensions
		* Resolution : 4096 x 2048 (or any power of two rectangle with 2:1 ratio), this is the recommended for 2k skybox.
		* Percentage : 10% when previewing, 100% for final rendering
	* Output
		* File format : Radiance HDR
		
* Camera
	* create it if needed, set it at zero (Alt+G) with front view orientation (Alt+R, R+X+90)
	* set projection in camera settings :
		* Lens / Type : Panoramic
		* Lens / Panoram type : Equirectangular

### Modeling

This is where you can express your creativity to make awesome background. Few tips : 
* create a flipped sphere for the far sky and ground and apply a gradient to it.
* add another flipped sphere inside the sky one with transparency and a cloud texture
* add a big quad inside the cloud sphere and sculpt the background
* add the sun by creating a small sphere behind the clouds. Apply an emissive material to it (eg. 30.0)
* set the overall ambient color, you could even use another IBL/Skybox at this point

### Exporting

Once you're set and your scene is complete, you can render (F12) and have a tea. Once finished you can save the image as HDR, you could also save a copy as JPEG in order to have a preview of it.

