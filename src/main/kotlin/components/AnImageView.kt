/* Main.kt

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

package components

import IPath
import IZoomMode
import PreloadedImages
import ZOOM_MODE
import javafx.beans.InvalidationListener
import javafx.beans.NamedArg
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.stage.Screen
import java.io.File

// Arbitrary width and height padding to leave room for taskbars/menu bars
private const val WIDTH_PADDING = 50
private const val HEIGHT_PADDING = 200

private val SCREEN_HEIGHT = Screen.getPrimary().bounds.height
private val HALF_SCREEN_WIDTH = Screen.getPrimary().bounds.width / 2

interface IImage {
    fun next()
    fun previous()
    fun load(path: String)
    fun image(): SimpleObjectProperty<Image>
}

interface IImageContainer {
    val width: ReadOnlyDoubleProperty
    val height: ReadOnlyDoubleProperty
}

class AnImageView(@NamedArg("container") private val container: IImageContainer,
                  @NamedArg("path") private val initialPath: IPath,
                  @NamedArg("zoomMode") private val zoomMode: IZoomMode) : StackPane(), IImage {
    @FXML
    private lateinit var background: Pane

    @FXML
    lateinit var imageView: ImageView

    val images: PreloadedImages

    init {
        FXMLLoader(javaClass.getResource("AnImageView.fxml")).apply {
            setRoot(this@AnImageView)
            setController(this@AnImageView)
            load()
        }
        zoomMode.zoomModeProperty().value = ZOOM_MODE.PERCENT_100
        images = PreloadedImages(File(initialPath.imagePath))
        images.getCurrentImage().apply {
            background.maxWidth = width
            background.maxHeight = height
            if (width > HALF_SCREEN_WIDTH - WIDTH_PADDING || height > SCREEN_HEIGHT - HEIGHT_PADDING) {
                imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                imageView.fitHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                background.maxWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                background.maxHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                zoomMode.zoomModeProperty().value = ZOOM_MODE.HALF_SCREEN
            }
        }
        zoomMode.zoomModeProperty().addListener(InvalidationListener { setZoomMode(zoomMode.zoomModeProperty().value) })
        images.currentImageProperty().addListener(InvalidationListener { setZoomMode(zoomMode.zoomModeProperty().value) })
        imageView.imageProperty().bind(images.currentImageProperty())
    }

    override fun next() {
        images.moveForward()
    }

    override fun previous() {
        images.moveBack()
    }

    override fun load(path: String) {
        images.initialize(File(path))
    }

    override fun image(): SimpleObjectProperty<Image> {
        return images.currentImageProperty()
    }

    private fun setZoomMode(zoomMode: ZOOM_MODE) {
        val image = images.getCurrentImage()
        imageView.fitWidthProperty().unbind()
        imageView.fitHeightProperty().unbind()
        imageView.fitWidth = 0.0
        imageView.fitHeight = 0.0
        background.maxWidth = image.width
        background.maxHeight = image.height
        when (zoomMode) {
            ZOOM_MODE.PERCENT_100 -> imageView.fitWidth = 0.0
            ZOOM_MODE.PERCENT_200 -> {
                imageView.fitWidth = image.width * 2
                background.maxWidth = image.width * 2
                background.maxHeight = image.height * 2
            }
            ZOOM_MODE.FIT_TO_WINDOW -> {
                // Need to subtract 2 to account for the border width
                imageView.fitWidthProperty().bind(container.width.subtract(2))
                imageView.fitHeightProperty().bind(container.height.subtract(2))
                background.maxWidth = -1.0
                background.maxHeight = -1.0
            }
            ZOOM_MODE.HALF_SCREEN -> {
                imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                imageView.fitHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                background.maxWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                background.maxHeight = SCREEN_HEIGHT - HEIGHT_PADDING
            }
        }
    }
}
