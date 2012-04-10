attribute vec3 position;
attribute vec2 texcoord;

varying vec2 vertTexcoord;

void main()
{
	vertTexcoord = texcoord;
	vec4 pos = gl_ModelViewProjectionMatrix * vec4(position, 1.0f);
	gl_Position = pos;
}