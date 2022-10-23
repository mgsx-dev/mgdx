
#ifdef BASE_COLOR_LOCATION
    out_baseColor = baseColor;
#endif

#ifdef POSITION_LOCATION
    out_position = v_position;
#endif

#ifdef LOCAL_POSITION_LOCATION
    out_localPosition = v_localPosition;
#endif

#ifdef ORM_LOCATION
    perceptualRoughness = clamp(perceptualRoughness, 0.0, 1.0);
    out_ORM = vec3(ao, perceptualRoughness, metallic);
#endif

#ifdef EMISSIVE_LOCATION
    out_emissive = vec4(emissive, 1.0); // TODO handle blending ?
#endif

#ifdef NORMAL_LOCATION
    out_normals = n;
#endif

#ifndef MRT
#ifndef COLOR_LOCATION
#define COLOR_LOCATION
#endif
#endif

#ifdef COLOR_LOCATION
    // TODO function finalColor() -> vec4 (with gamma applied or not)
#ifdef GAMMA_CORRECTION
    out_FragColor = vec4(pow(color,vec3(1.0/GAMMA_CORRECTION)), baseColor.a);
#else
    out_FragColor = vec4(color, baseColor.a);
#endif

    // Fog applied after gamma correction (TODO ???)
#ifdef fogFlag
#ifdef fogEquationFlag
    float fog = (eyeDistance - u_fogEquation.x) / (u_fogEquation.y - u_fogEquation.x);
    fog = clamp(fog, 0.0, 1.0);
    fog = pow(fog, u_fogEquation.z);
#else
	float fog = min(1.0, eyeDistance * eyeDistance * u_cameraPosition.w);
#endif
	out_FragColor.rgb = mix(out_FragColor.rgb, u_fogColor.rgb, fog * u_fogColor.a);
#endif


	// Blending and Alpha Test
#ifdef blendedFlag
	out_FragColor.a = baseColor.a * u_opacity;
#ifdef alphaTestFlag
	if (out_FragColor.a <= u_alphaTest)
		discard;
#endif
#else
	out_FragColor.a = 1.0;
#endif

#endif
