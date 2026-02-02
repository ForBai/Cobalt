#version 330 core

uniform float u_Time;
uniform vec2 u_Resolution;
uniform vec2 u_Mouse;

in vec2 vUV;
out vec4 fragColor;

// HSV to RGB conversion
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// Pseudo-random hash
float hash(float n) {
    return fract(sin(n) * 43758.5453123);
}

void main() {
    vec2 uv = vUV;
    vec2 aspect = vec2(u_Resolution.x / u_Resolution.y, 1.0);
    vec2 mouse = u_Mouse / u_Resolution;
    
    // Dark background base
    vec3 color = vec3(0.05, 0.05, 0.08);
    
    const int PARTICLE_COUNT = 55;
    
    for (int i = 0; i < PARTICLE_COUNT; i++) {
        float fi = float(i);
        
        // Base position from hash
        vec2 pos = vec2(hash(fi * 1.23), hash(fi * 4.56));
        
        // Animate with time (floating motion)
        pos.x += sin(u_Time * 0.3 + fi) * 0.1;
        pos.y += cos(u_Time * 0.25 + fi * 0.7) * 0.1;
        
        // Mouse interaction (subtle attraction)
        vec2 toMouse = mouse - pos;
        // Apply aspect ratio correction to mouse interaction if needed, 
        // but simple offset usually feels fine
        pos += toMouse * 0.05;
        
        // Wrap around (optional, but good for keeping particles in view)
        // Since we are adding offsets, particles might drift off. 
        // Simple wrap logic: fract(pos) -> effectively wraps 0..1
        // But doing it after mouse offset can cause jumping.
        // Instead, let's keep it simple: initial pos is 0..1, offsets are small.
        
        // Distance calculation (aspect-corrected)
        float dist = length((uv - pos) * aspect);
        
        // Particle size and falloff
        float size = 0.02 + hash(fi * 7.89) * 0.03;
        // Soft glow
        float intensity = smoothstep(size, 0.0, dist);
        
        // Rainbow color
        // Cycle hue based on index and time
        float hue = fract(fi / float(PARTICLE_COUNT) + u_Time * 0.1);
        vec3 particleColor = hsv2rgb(vec3(hue, 0.8, 1.0));
        
        // Additive blending
        color += particleColor * intensity * 0.5;
    }
    
    // Vignette
    float vignette = 1.0 - length(uv - 0.5) * 0.4;
    color *= vignette;
    
    fragColor = vec4(color, 1.0);
}
