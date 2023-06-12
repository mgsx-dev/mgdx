#line 1

layout(location = 0) out vec4 out_color;

#if defined(colorFlag)
in vec4 v_color;
#endif


#ifdef blendedFlag
in float v_opacity;
#ifdef alphaTestFlag
in float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#ifdef textureFlag
in vec2 v_texCoord0;
#endif // textureFlag

#ifdef textureCoord1Flag
in vec2 v_texCoord1;
#endif // textureCoord1Flag

// texCoord unit mapping

#ifndef v_diffuseUV
#define v_diffuseUV v_texCoord0
#endif


#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef baseColorFactorFlag
uniform vec4 u_BaseColorFactor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
uniform float u_NormalScale;
#endif


#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag

struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];


uniform vec4 u_cameraPosition;

uniform vec2 u_MetallicRoughnessValues;

in vec3 v_position;


#ifdef normalFlag
#ifdef tangentFlag
in mat3 v_TBN;
#else
in vec3 v_normal;
#endif

#endif //normalFlag


#ifndef v_normalUV
#define v_normalUV v_texCoord0
#endif

#ifndef v_metallicRoughnessUV
#define v_metallicRoughnessUV v_texCoord0
#endif

#ifdef metallicRoughnessTextureFlag
uniform sampler2D u_MetallicRoughnessSampler;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

// Find the normal for this fragment, pulling either from a predefined normal map
// or from the interpolated mesh normal and tangent attributes.
vec3 getNormal()
{
#ifdef tangentFlag
#ifdef normalTextureFlag
    vec3 n = texture(u_normalTexture, v_normalUV).rgb;
    n = normalize(v_TBN * ((2.0 * n - 1.0) * vec3(u_NormalScale, u_NormalScale, 1.0)));
#else
    vec3 n = normalize(v_TBN[2].xyz);
#endif
#else
    vec3 n = normalize(v_normal);
#endif

    return n;
}

void main() {
#ifdef baseColorFactorFlag
	vec4 baseColorFactor = u_BaseColorFactor;
#else
	vec4 baseColorFactor = vec4(1.0, 1.0, 1.0, 1.0);
#endif

#ifdef diffuseTextureFlag
    vec4 baseColor = texture(u_diffuseTexture, v_diffuseUV) * baseColorFactor;
#else
    vec4 baseColor = baseColorFactor;
#endif

#ifdef ambientLightFlag
    vec3 ambientFactor = u_ambientLight;
#else
    vec3 ambientFactor = vec3(0.0);
#endif

    float perceptualRoughness = u_MetallicRoughnessValues.y;
    float metallic = u_MetallicRoughnessValues.x;
#ifdef metallicRoughnessTextureFlag
    // Roughness is stored in the 'g' channel, metallic is stored in the 'b' channel.
    // This layout intentionally reserves the 'r' channel for (optional) occlusion map data
    vec4 mrSample = texture(u_MetallicRoughnessSampler, v_metallicRoughnessUV);
    perceptualRoughness = mrSample.g * perceptualRoughness;
    metallic = mrSample.b * metallic;
#endif


    DirectionalLight light = u_dirLights[0];

    vec3 l = normalize(-light.direction);  // Vector from surface point to light

	vec3 surfaceToCamera = u_cameraPosition.xyz - v_position;
	float eyeDistance = length(surfaceToCamera);
	vec3 v = surfaceToCamera / eyeDistance;        // Vector from surface point to camera

	vec3 n = getNormal();                             // normal at surface point
	vec3 reflection = -normalize(reflect(v, n));

//	float NdotV = clamp(abs(dot(n, v)), 0.001, 1.0);

	vec3 h = normalize(l+v);

	float NdotL = clamp(dot(n, l), 0.001, 1.0);
	float NdotH = clamp(dot(n, h), 0.001, 1.0);

	float invRoughnessSquare = 1.0 - perceptualRoughness;

	float shininess = pow(invRoughnessSquare * invRoughnessSquare * 20.0, 2.0) + 1.0;

	float diffuseFactor = NdotL;
	float specularFactor = pow(NdotH, shininess);

	// apply ceil
	diffuseFactor = step(0.01, diffuseFactor);
	specularFactor = step(0.01, specularFactor) * diffuseFactor;

	// Roughness reduce specular power
	diffuseFactor *= (1.0 - perceptualRoughness);
	specularFactor *= (1.0 - perceptualRoughness);

	// Metalness gives material color
	vec3 specularColor = mix(vec3(0.5), baseColor.rgb * 4.0, metallic);

	// Metalness give less light color for diffuse
	vec3 diffuseColor = mix(light.color, vec3(1.0, 1.0, 1.0), metallic);

	// Metallic gives less diffuse
	diffuseFactor *= (1.0 - metallic);

	vec3 lightValue = diffuseColor * diffuseFactor;
	vec3 specular = light.color * specularColor * specularFactor;

	vec3 color = (ambientFactor + lightValue) * baseColor.rgb + specular;
#ifdef unlitFlag
	color = baseColor.rgb;
#endif
#ifdef emissiveColorFlag
	color = u_emissiveColor.rgb;
#endif

	out_color = vec4(color, baseColor.a);
}

