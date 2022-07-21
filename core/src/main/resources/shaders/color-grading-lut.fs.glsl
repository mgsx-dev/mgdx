
#ifdef USE_TEXTURE_3D
uniform sampler3D u_textureLUT;
#else
#ifdef PACKED_FLOAT
uniform sampler2D u_textureLUTR;
uniform sampler2D u_textureLUTG;
uniform sampler2D u_textureLUTB;
const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
#define unpack(v) dot(v, bitShifts);
#else
uniform sampler2D u_textureLUT;
#endif
uniform vec2 u_grid;
#endif

uniform float u_invResolution;
uniform sampler2D u_texture;
varying vec2 v_texCoords;


void main() {
	 // scale the input color range to start and end on lattice (pixel center).
	vec4 colorIn = texture2D(u_texture, v_texCoords);
	//vec3 coordsIn = colorIn.rgb * vec3(1.0 - u_invResolution) + vec3(0.5 * u_invResolution);
	vec3 coordsIn = clamp(colorIn.rgb, 0.5 * u_invResolution, 1.0 - u_invResolution);

#ifdef USE_TEXTURE_3D
	vec3 colorOut = texture3D(u_textureLUT, coordsIn).rgb;
#else
#ifdef TRILINEAR
	vec3 rgbValue = coordsIn / u_invResolution;
	vec3 rgbIndex0 = floor(rgbValue);
	vec3 rgbIndex1 = rgbIndex0 + vec3(1.0);
	vec3 rgbMix = rgbValue - rgbIndex0;

	vec2 bCoords0 = vec2(mod(rgbIndex0.z, u_grid.x), floor(rgbIndex0.z / u_grid.x)) / u_grid.xy;
	vec2 bCoords1 = vec2(mod(rgbIndex1.z, u_grid.x), floor(rgbIndex1.z / u_grid.x)) / u_grid.xy;

	vec2 rgCoords0 = rgbIndex0.xy * u_invResolution / u_grid.xy;
	vec2 rgCoords1 = rgbIndex1.xy * u_invResolution / u_grid.xy;

	vec3 colorOut000 = texture2D(u_textureLUT, vec2(rgCoords0.x, rgCoords0.y) + bCoords0).rgb;
	vec3 colorOut001 = texture2D(u_textureLUT, vec2(rgCoords0.x, rgCoords0.y) + bCoords1).rgb;
	vec3 colorOut010 = texture2D(u_textureLUT, vec2(rgCoords0.x, rgCoords1.y) + bCoords0).rgb;
	vec3 colorOut011 = texture2D(u_textureLUT, vec2(rgCoords0.x, rgCoords1.y) + bCoords1).rgb;

	vec3 colorOut100 = texture2D(u_textureLUT, vec2(rgCoords1.x, rgCoords0.y) + bCoords0).rgb;
	vec3 colorOut101 = texture2D(u_textureLUT, vec2(rgCoords1.x, rgCoords0.y) + bCoords1).rgb;
	vec3 colorOut110 = texture2D(u_textureLUT, vec2(rgCoords1.x, rgCoords1.y) + bCoords0).rgb;
	vec3 colorOut111 = texture2D(u_textureLUT, vec2(rgCoords1.x, rgCoords1.y) + bCoords1).rgb;

	vec3 colorOut = mix(
			mix(
				mix(colorOut000, colorOut001, rgbMix.z),
				mix(colorOut010, colorOut011, rgbMix.z), rgbMix.y),
			mix(
				mix(colorOut100, colorOut101, rgbMix.z),
				mix(colorOut110, colorOut111, rgbMix.z), rgbMix.y), rgbMix.x);
#else
	vec2 rgCoords = coordsIn.rg / u_grid.xy;

	float zValue = coordsIn.z / u_invResolution;
	float zIndex0 = floor(zValue);
	float zIndex1 = zIndex0 + 1.0;
	float zMix = zValue - zIndex0;


	vec2 bCoords0 = vec2(mod(zIndex0, u_grid.x), floor(zIndex0 / u_grid.x)) / u_grid.xy;
	vec2 bCoords1 = vec2(mod(zIndex1, u_grid.x), floor(zIndex1 / u_grid.x)) / u_grid.xy;

#ifdef PACKED_FLOAT
	vec2 rgbCoords0 = rgCoords + bCoords0;
	vec2 rgbCoords1 = rgCoords + bCoords1;

	float r0 = unpack(texture2D(u_textureLUTR, rgbCoords0));
	float r1 = unpack(texture2D(u_textureLUTR, rgbCoords1));

	float g0 = unpack(texture2D(u_textureLUTG, rgbCoords0));
	float g1 = unpack(texture2D(u_textureLUTG, rgbCoords1));

	float b0 = unpack(texture2D(u_textureLUTB, rgbCoords0));
	float b1 = unpack(texture2D(u_textureLUTB, rgbCoords1));
	vec3 colorOut0 = vec3(r0,g0,b0);
	vec3 colorOut1 = vec3(r1,g1,b1);
#else
	vec3 colorOut0 = texture2D(u_textureLUT, rgCoords + bCoords0).rgb;
	vec3 colorOut1 = texture2D(u_textureLUT, rgCoords + bCoords1).rgb;
#endif
	vec3 colorOut = mix(colorOut0, colorOut1, zMix);
#endif

#endif

    gl_FragColor = vec4(colorOut, colorIn.a);
}
