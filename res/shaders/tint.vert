#version 120
attribute vec3 position;
attribute vec2 texcoord;
attribute vec3 color;

varying vec2 vertTexcoord;
varying vec4 vertColor;

void main()
{
	vertTexcoord = texcoord;
	vertColor = vec4(color, 1.f);
	//vertColor = vec4(1.0f, 1.0f,1.0f,1.0f);
	//vertColor = gl_Color;

	//normal
	gl_Position = gl_ModelViewProjectionMatrix * vec4(position.x, position.y, position.z, 1.0f);
	//gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	//billboard
	//gl_Position = gl_ProjectionMatrix * (gl_ModelViewMatrix * vec4(0.0, 0.0, 0.0, 1.0) + vec4(position.x, position.y, 0.0, 0.0));
}