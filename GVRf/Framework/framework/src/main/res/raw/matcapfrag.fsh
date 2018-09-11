precision highp float;

uniform sampler2D u_texture;

in vec2 vN;
out vec4 outColor;

void main(void)
{
    vec3 base = texture( u_texture, vN ).rgb;
  	outColor = vec4( base, 1. );
}