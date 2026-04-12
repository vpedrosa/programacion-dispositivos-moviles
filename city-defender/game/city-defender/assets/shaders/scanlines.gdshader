shader_type canvas_item;

uniform float scanline_count : hint_range(100.0, 800.0) = 350.0;
uniform float scanline_intensity : hint_range(0.0, 0.5) = 0.18;
uniform float vignette_intensity : hint_range(0.0, 1.0) = 0.35;
uniform float flicker_speed : hint_range(0.0, 10.0) = 2.5;

void fragment() {
	// Scanlines
	float line = mod(floor(UV.y * scanline_count), 2.0);
	float scanline = 1.0 - line * scanline_intensity;

	// Vignette
	vec2 c = UV * 2.0 - 1.0;
	float vignette = 1.0 - dot(c, c) * vignette_intensity;

	// Subtle flicker
	float flicker = 1.0 - sin(TIME * flicker_speed) * 0.008;

	COLOR.rgb *= scanline * vignette * flicker;
	COLOR.a = 1.0;
}
