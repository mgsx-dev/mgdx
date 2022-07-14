varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec3 u_threshold;

#ifdef SMOOTH
uniform float u_falloff;
#endif
#ifdef CLIP
uniform float u_max;
#endif

void main() {

    vec4 color = texture2D(u_texture, v_texCoords);

    float brightness = dot(color.rgb, u_threshold); // vec3(0.2126, 0.7152, 0.0722));
    if(brightness > 1.0){
#ifdef CLIP
    	color.rgb = color.rgb * min(u_max, brightness) / brightness;
#endif

    	gl_FragColor = vec4(color.rgb, 1.0);
    }
    else{
#ifdef SMOOTH
    	float factor = pow(brightness, u_falloff);
    	if(brightness > 0.0){
    		color = color * factor / brightness;
    	}
    	gl_FragColor = vec4(color.rgb, 1.0);
#else
    	gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
#endif
    }

}
