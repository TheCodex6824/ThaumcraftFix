// we do not use compatibility profile anymore due to vertex attributes colliding with other things on NVIDIA drivers
#version 330 core

// the vertex data is fed into the shader via a VBO, so this is how to access it
layout (location = 0) in vec3 pos;

// coordinate transformation matrices (replacing compatibility profile builtins)
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

// the fragment shader will need this variant of the position for its calculations
// this position will be interpolated for each fragment as it is passed there by OpenGL
out vec4 modelViewPosition;

void main()
{
    vec4 posVec4 = vec4(pos, 1.0F);
    gl_Position = projectionMatrix * modelViewMatrix * posVec4;
    modelViewPosition = modelViewMatrix * posVec4;
}
