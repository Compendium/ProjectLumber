#version 120
uniform sampler2D texture;

varying vec2 vertTexcoord;

void main() {
	//gl_FragColor = vertColor;
	vec4 c = texture2D(texture, vertTexcoord);
	//c *= vertColor;
	//vec4 c = vertColor;
	
	//if(c.a == 0.0f)
		//discard;

	gl_FragColor = c;
}