package com.vpedrosa.smarthome.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Platform-specific composable that loads and displays an image from a URI string.
 *
 * @param uri the URI string pointing to the image (content:// or file://).
 * @param contentDescription accessibility description for the image.
 * @param modifier the Modifier to apply to the image.
 * @param contentScale how the image should be scaled within the bounds.
 */
@Composable
expect fun UriImage(
    uri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
)
