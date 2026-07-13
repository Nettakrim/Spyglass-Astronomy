#version 330

#moj_import <minecraft:dynamictransforms.glsl>
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec4 vertexColor;

void main() {
    int corner = gl_VertexID%4;
    float bias = (fract((gl_VertexID/4)*123.456)-0.5)/8.0;

    int seed = int(Color.a*255);
    float rotationSpeed = (((seed & 15)/15.0  + bias) * 2.0 - 1.0) * 0.1f;
    float twinkleSpeed =  ((seed & 240)/240.0 + bias) * 0.5f + 0.7f;
    float twinkle = 1 - 2.5f * max(sin(ColorModulator.x * twinkleSpeed) - 0.75f,0);

    vec3 worldPos = Position;
    // get an arbitrary perpendicular vector - needs to work for all input vectors!
    vec3 perpendicular = normalize(worldPos.z == 0 ? vec3(-worldPos.y,worldPos.x,worldPos.x) : vec3(worldPos.z,worldPos.z,-worldPos.x-worldPos.y));

    vec3 offset = cross(worldPos, perpendicular);
    vec2 extents = vec2((corner & 2)-1, 1-((corner + 1) & 2)) * UV0.x;
    float time = ColorModulator.x * rotationSpeed;
    float s = sin(time);
    float c = cos(time);

    worldPos +=       offset           * (c*extents.x-s*extents.y);
    worldPos += cross(offset,worldPos) * (s*extents.x+c*extents.y);

    vec4 position = ProjMat * ModelViewMat * vec4(worldPos * 100, 1.0);
    gl_Position = position;

    float heightScale = ColorModulator.y;
    float alpha = abs(UV0.y)-0.125;
    float brightness = heightScale*max(alpha/2 + heightScale/2, 2*alpha-1) + (1-heightScale) * alpha * alpha * alpha;
    if (UV0.y < 0) {
        brightness = (brightness + (0.5f * alpha + 0.5f))/2;
    }

    vertexColor = vec4(Color.rgb, brightness * twinkle) * vec4(ColorModulator.zzzw);
}