in vec3 a_position;
in vec3 a_normal;

@MATRIX_UNIFORMS

out vec2 vN;

void main(void)
{
    vec4 p = vec4( a_position, 1. );

    vec3 e = normalize( vec3( u_mv * p ) );
    vec3 n = normalize( mat3(u_mv_it) * a_normal );

    vec3 r = reflect( e, n );
    float m = 2.82842712474619 * sqrt(r.z + 1.);
    vN = r.xy / m + .5;

    gl_Position = u_mvp * p;
}