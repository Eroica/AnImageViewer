package model

import javafx.scene.image.Image
import java.io.File

interface IImages<T> {
    fun currentImage(): T
    fun previousImage(): T
    fun nextImage(): T
}

private val IMAGE_FORMATS = setOf("JPG", "PNG", "BMP", "TIF", "TIFF", "SVG", "ICO")

class PreloadedImages(imagePath: File) : IImages<Image?> {
    private val images: List<File?>
    private var index = 1
    private val imageCache = HashMap<Int, Image?>()

    init {
        val directory = if (imagePath.isDirectory) imagePath else imagePath.parentFile
        // Pad the list of files with `null' at the beginning and end so that every file has a successor/predecessor.
        images = listOf(null) + directory.walkTopDown()
            .maxDepth(1)
            .filter { it.extension.toUpperCase() in IMAGE_FORMATS }
            .toList() + listOf(null)

        if (images.all { it == null }) {
            throw NoSuchElementException()
        }

        // Set index to the supplied image (if it's not a directory), otherwise it stays at 0.
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
            if (previousFile != null) {
                imageCache[0] = Image(previousFile.toURI().toString())
            } else {
                imageCache[0] = null
            }
            imageCache[1]
        } catch (e: IndexOutOfBoundsException) {
            ++index
            null
        }
    }

    override fun nextImage(): Image? {
        return try {
            val nextFile = images[++index + 1]
            imageCache[0] = imageCache[1]
            imageCache[1] = imageCache[2]
            if (nextFile != null) {
                imageCache[2] = Image(nextFile.toURI().toString())
            } else {
                imageCache[2] = null
            }
            imageCache[1]
        } catch (e: IndexOutOfBoundsException) {
            --index
            null
        }
    }
}
