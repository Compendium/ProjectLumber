#version 120
uniform vec3 cam;
attribute vec3 position;
attribute vec2 texcoord;

varying vec2 vertTexcoord;
varying vec4 vertColor;

void main()
{
	vertTexcoord = texcoord;
	vertColor = vec4(0.0f,1.0f,0.0f,1.0f);
	//vertColor = vec4(position.xyz, 1.0);
	
	gl_Position = (gl_ModelViewProjectionMatrix) * vec4(position, 1.0);
	//gl_PointSize = abs(pow(distance(position, cam), -1.f)*100);
	
	//gl_FrontColor = vertColor;
	//gl_Position = (gl_ModelViewMatrix * vec4(0, position.y, 0, 1)) + (gl_ProjectionMatrix * vec4(position.x, position.y, position.z, 0));
}