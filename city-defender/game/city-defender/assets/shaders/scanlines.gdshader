shader_type canvas_item;

uniform float scanline_count : hint_range(100.0, 800.0) = 350.0;
uniform float scanline_intensity : hint_range(0.0, 0.5) = 0.18;
uniform float vignette_intensity : hint_range(0.0, 1.0) = 0.35;
uniform float flicker_speed : hint_range(0.0, 10.0) = 2.5;
uniform float scroll_speed : hint_range(0.0, 0.5) = 0.008;

void fragment() {
	// Scroll scanlines slowly downward over time
	float scrolled_y = mod(UV.y + TIME * scroll_speed, 1.0);
	float line = mod(floor(scrolled_y * scanline_count), 2.0);
	float scanline_alpha = line * scanline_intensity;

	// Vignette: darken toward edges
	vec2 c = UV * 2.0 - 1.0;
	float vignette_alpha = dot(c, c) * vignette_intensity;

	// Subtle flicker
	float flicker = sin(TIME * flicker_speed) * 0.008;

	// Render as a semi-transparent dark overlay on top of the scene
	float alpha = clamp(scanline_alpha + vignette_alpha + flicker, 0.0, 0.95);
	COLOR = vec4(0.0, 0.0, 0.0, alpha);
}
