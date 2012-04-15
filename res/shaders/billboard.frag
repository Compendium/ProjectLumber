#version 120
//uniform sampler2D texture;
//varying vec2 vertTexcoord;

varying vec4 vertColor;

void main() {
	gl_FragColor = vertColor;
	//gl_FragColor = texture2D(texture, vertTexcoord);
}