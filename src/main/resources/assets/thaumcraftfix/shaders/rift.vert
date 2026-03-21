// Other versions might be ok but the compatibility profile must be used
#version 330 compatibility

// the vertex data is fed into the shader via a VBO, so this is how to access it
layout (location = 0) in vec3 pos;

// the fragment shader will need this variant of the position for its calculations
// this position will be interpolated for each fragment as it is passed there by OpenGL
out vec4 modelViewPosition;

void main()
{
    gl_Position = gl_ModelViewProjectionMatrix * vec4(pos, 1.0F);
    //modelViewPosition = gl_ModelViewMatrix * vec4(pos, 0.0F);
    modelViewPosition = gl_ModelViewMatrix * gl_Vertex;
}
