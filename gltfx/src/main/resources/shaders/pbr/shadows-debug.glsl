#ifdef shadowDebug
#ifdef shadowMapFlag
#ifdef numCSM

vec4 getCSMShadowDebug(sampler2D sampler, vec3 uv, float pcf){
	return texture2D(sampler, uv.xy);
}
vec4 getShadowDebug()
{
	for(int i=0 ; i<numCSM ; i++){
		vec2 pcfClip = u_csmPCFClip[i];
		float pcf = pcfClip.x;
		float clip = pcfClip.y * 0.0;
		vec3 uv = v_csmUVs[i];
		if(uv.x >= clip && uv.x <= 1.0 - clip &&
			uv.y >= clip && uv.y <= 1.0 - clip &&
			uv.z >= 0.0 && uv.z <= 1.0){
			
			float lum = (i + 1.0) / (numCSM + 1.0);
			
			#if numCSM > 0
			if(i == 0) return getCSMShadowDebug(u_csmSamplers[0], uv, pcf) * lum;
			#endif
			#if numCSM > 1
			if(i == 1) return getCSMShadowDebug(u_csmSamplers[1], uv, pcf) * lum;
			#endif
			#if numCSM > 2
			if(i == 2) return getCSMShadowDebug(u_csmSamplers[2], uv, pcf) * lum;
			#endif
			#if numCSM > 3
			if(i == 3) return getCSMShadowDebug(u_csmSamplers[3], uv, pcf) * lum;
			#endif
			#if numCSM > 4
			if(i == 4) return getCSMShadowDebug(u_csmSamplers[4], uv, pcf) * lum;
			#endif
			#if numCSM > 5
			if(i == 5) return getCSMShadowDebug(u_csmSamplers[5], uv, pcf) * lum;
			#endif
			#if numCSM > 6
			if(i == 6) return getCSMShadowDebug(u_csmSamplers[6], uv, pcf) * lum;
			#endif
			#if numCSM > 7
			if(i == 7) return getCSMShadowDebug(u_csmSamplers[7], uv, pcf) * lum;
			#endif			
		}
	}
	// default map
	return getCSMShadowDebug(u_shadowTexture, v_shadowMapUv, u_shadowPCFOffset);
}

#else

vec4 getShadowDebug()
{
	return texture2D(u_shadowTexture, v_shadowMapUv.xy);
}

#endif

#endif //shadowMapFlag
#endif //shadowDebug