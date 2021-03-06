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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import java.io.File

// Supported types from https://docs.oracle.com/javase/8/javafx/api/javafx/scene/image/Image.html
private val IMAGE_FORMATS = setOf("BMP", "GIF", "JPG", "JPEG", "PNG")

class PreloadedImages(imagePath: File) {
    private var images = filterFiles(imagePath)
    private var index = 0
        private set(value) {
            field = value
            isAtBeginning.value = index == 0
            isAtEnd.value = index == images.size - 1
        }
    private val isAtBeginning = SimpleBooleanProperty(true)
    fun getIsAtBeginning() = isAtBeginning.get()
    fun isAtBeginningProperty() = isAtBeginning

    private val isAtEnd = SimpleBooleanProperty(false)
    fun getIsAtEnd() = isAtEnd.get()
    fun isAtEndProperty() = isAtEnd

    private val currentImage = SimpleObjectProperty<Image>()
    fun getCurrentImage(): Image = currentImage.get()
    fun currentImageProperty() = currentImage

    private val imageCache = arrayOfNulls<Image>(3)

    init {
        initialize(imagePath)
    }

    fun initialize(imagePath: File) {
        if (imagePath !in images) {
            images = filterFiles(imagePath)
            index = 0
        } else if (imagePath.isDirectory) {
            images = filterFiles(imagePath)
        } else if (!imagePath.isDirectory && imagePath.extension.toUpperCase() !in IMAGE_FORMATS) {
            throw NoSuchElementException()
        } else {
            index = images.indexOf(imagePath)
        }
        cacheImages()
    }

    private fun filterFiles(path: File): List<File> {
        val directory = if (path.isDirectory) path else path.parentFile
        return directory.walkTopDown()
            .maxDepth(1)
            .filter { it.extension.toUpperCase() in IMAGE_FORMATS }
            .sortedWith(NaturalOrderComparator())
            .toList()
            .also {
                if (it.isEmpty()) {
                    throw NoSuchElementException()
                }
            }
    }

    private fun cacheImages() {
        imageCache[0] = try {
            Image(images[index - 1].toURI().toString())
        } catch (e: IndexOutOfBoundsException) {
            null
        }
        Image(images[index].toURI().toString()).let {
            imageCache[1] = it
        }
        currentImage.value = imageCache[1]
        imageCache[2] = try {
            Image(images[index + 1].toURI().toString())
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    fun moveForward() {
        if (index == images.size - 1) {
            return
        } else {
            index++
            imageCache[0] = imageCache[1]
            imageCache[1] = imageCache[2]
            imageCache[2] = try {
                Image(images[index + 1].toURI().toString())
            } catch (e: IndexOutOfBoundsException) {
                null
            }
            currentImage.value = imageCache[1]
        }
    }

    fun moveBack() {
        if (index == 0) {
            return
        } else {
            index--
            imageCache[2] = imageCache[1]
            imageCache[1] = imageCache[0]
            imageCache[0] = try {
                Image(images[index - 1].toURI().toString())
            } catch (e: IndexOutOfBoundsException) {
                null
            }
            currentImage.value = imageCache[1]
        }
    }
}
