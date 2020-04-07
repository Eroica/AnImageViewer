/* Images.kt

  Copyright (C) 2019-2020 Eroica

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

*/

import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.image.Image
import java.io.File

interface IImages<T> {
    fun currentImage(): T
    fun previousImage(): T
    fun nextImage(): T
}

// Supported types from https://docs.oracle.com/javase/8/javafx/api/javafx/scene/image/Image.html
private val IMAGE_FORMATS = setOf("BMP", "GIF", "JPG", "JPEG", "PNG")

class PreloadedImages(imagePath: File) : IImages<Image?> {
    private val images: List<File?>
    private var index = 1
    private val imageCache = arrayOfNulls<Image?>(3)

    val indexProperty = SimpleIntegerProperty(index)
    val size: Int
        get() = images.size

    init {
        val directory = if (imagePath.isDirectory) imagePath else imagePath.parentFile
        // Pad the list of files with `null' at the beginning and end so that every file has a successor/predecessor.
        images = listOf(null) + directory.walkTopDown()
            .maxDepth(1)
            .filter { it.extension.toUpperCase() in IMAGE_FORMATS }
            .toSortedSet(NaturalOrderComparator()) + listOf(null)

        if (images.all { it == null }) {
            throw NoSuchElementException()
        }

        // Set index to the supplied image (if it's not a directory), otherwise it stays at 1.
        if (!imagePath.isDirectory) {
            index = images.indexOf(imagePath)
        }

        // Create a map of 0, 1, 2, where 1 maps to the current image, 0 to its predecessor (might be null),
        // and 1 to its successor (might be null).
        images.subList(index - 1, index + 2).take(3).forEachIndexed { index, file ->
            if (file != null) {
                imageCache[index] = Image(file.toURI().toString())
            } else {
                imageCache[index] = null
            }
        }
    }

    override fun currentImage() = imageCache[1]

    override fun previousImage(): Image? {
        return try {
            val previousFile = images[--index - 1]
            imageCache[2] = imageCache[1]
            imageCache[1] = imageCache[0]
            imageCache[0] = previousFile?.toURI()?.toString()?.let { Image(it)}
            imageCache[1]
        } catch (e: IndexOutOfBoundsException) {
            ++index
            null
        } finally {
            indexProperty.set(index)
        }
    }

    override fun nextImage(): Image? {
        return try {
            val nextFile = images[++index + 1]
            imageCache[0] = imageCache[1]
            imageCache[1] = imageCache[2]
            imageCache[2] = nextFile?.toURI()?.toString()?.let { Image(it) }
            imageCache[1]
        } catch (e: IndexOutOfBoundsException) {
            --index
            null
        } finally {
            indexProperty.set(index)
        }
    }
}
