#version 330

in vec2 v_texCoord0;
uniform sampler2D tex0;
uniform sampler2D tex1;

out vec4 o_color;
void main() {
    vec4 a = texture(tex0, v_texCoord0);
    vec4 b = texture(tex1, v_texCoord0);
    vec4 color = vec4(0.0);

    if (a.a > b.a) {
        color = a;
    }

    if (b.a > a.a) {
        color = b;
    }

    o_color = color;
}