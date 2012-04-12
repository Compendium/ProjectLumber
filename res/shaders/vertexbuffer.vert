#version 120
attribute vec3 position;
attribute int type;
//attribute vec2 texcoord;

//varying vec2 vertTexcoord;
varying vec4 vertColor;

void main()
{
	//vertTexcoord = texcoord;
	//vec4 pos = gl_ModelViewProjectionMatrix * vec4(position, 1.0f);
	//gl_Position = pos;
	vertColor = vec4(1.0f,1.0f,1.0f,1.0f);

	if(type == 0) {
		//normal
		gl_Position = gl_ModelViewProjectionMatrix * vec4(position, 1.0f);
	}
	else if (type == 1) {
		//billboard code
		gl_Position = gl_ProjectionMatrix * (gl_ModelViewMatrix * vec4(0.0, 0.0, 0.0, 1.0) + vec4(position.x, position.y, 0.0, 0.0));
	}
}