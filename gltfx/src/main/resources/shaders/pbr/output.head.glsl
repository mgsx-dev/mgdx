// MRT
#ifdef MRT
#ifdef COLOR_LOCATION
layout(location = COLOR_LOCATION) out vec4 out_FragColor;
#endif

#ifdef EMISSIVE_LOCATION
layout(location = EMISSIVE_LOCATION) out vec4 out_emissive;
#endif

#ifdef BASE_COLOR_LOCATION
layout(location = BASE_COLOR_LOCATION) out vec4 out_baseColor;
#endif

#ifdef POSITION_LOCATION
layout(location = POSITION_LOCATION) out vec3 out_position;
varying vec3 v_localPosition;
#endif

#ifdef NORMAL_LOCATION
layout(location = NORMAL_LOCATION) out vec3 out_normals;
#endif

#ifdef LOCAL_POSITION_LOCATION
in vec3 v_localPosition;
layout(location = LOCAL_POSITION_LOCATION) out vec3 out_localPosition;
#endif

#ifdef ORM_LOCATION
layout(location = ORM_LOCATION) out vec3 out_ORM;
#endif

#ifdef DIFFUSE_LOCATION
layout(location = DIFFUSE_LOCATION) out vec3 out_diffuse;
#endif

#ifdef SPECULAR_LOCATION
layout(location = SPECULAR_LOCATION) out vec3 out_specular;
#endif

#ifdef TRANSMISSION_LOCATION
layout(location = TRANSMISSION_LOCATION) out vec3 out_transmission;
#endif

#else


#ifdef GLSL3
out vec4 out_FragColor;
#else
#define out_FragColor gl_FragColor
#endif

#endif
