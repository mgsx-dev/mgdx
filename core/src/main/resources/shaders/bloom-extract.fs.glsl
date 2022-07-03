varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec3 u_threshold;

void main() {

    vec4 color = texture2D(u_texture, v_texCoords);

    float brightness = dot(color.rgb, u_threshold); // vec3(0.2126, 0.7152, 0.0722));
    if(brightness > 1.0)
    	gl_FragColor = vec4(color.rgb, 1.0);
    else
    	gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);

}