/* AnImageView.kt

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

import javafx.beans.InvalidationListener
import javafx.beans.NamedArg
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.stage.Screen
import models.IViewModel
import models.ZoomMode

// Arbitrary width and height padding to leave room for taskbars/menu bars
private const val WIDTH_PADDING = 50
private const val HEIGHT_PADDING = 200

private val SCREEN_HEIGHT = Screen.getPrimary().bounds.height
private val HALF_SCREEN_WIDTH = Screen.getPrimary().bounds.width / 2

interface IImageContainer {
    val width: ReadOnlyDoubleProperty
    val height: ReadOnlyDoubleProperty
}

class AnImageView(
    @NamedArg("container") private val container: IImageContainer,
    @NamedArg("viewModel") private val viewModel: IViewModel
) : StackPane() {
    @FXML
    private lateinit var background: Pane

    @FXML
    lateinit var imageView: ImageView

    init {
        FXMLLoader(javaClass.getResource("AnImageView.fxml")).apply {
            setRoot(this@AnImageView)
            setController(this@AnImageView)
            load()
        }
        viewModel.images.getCurrentImage().apply {
            background.maxWidth = width
            background.maxHeight = height
            if (width > HALF_SCREEN_WIDTH - WIDTH_PADDING || height > SCREEN_HEIGHT - HEIGHT_PADDING) {
                imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                imageView.fitHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                background.maxWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                background.maxHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                viewModel.zoomMode.value = ZoomMode.HALF_SCREEN
            }
        }
        viewModel.zoomMode.addListener(InvalidationListener {
            setZoomMode(viewModel.zoomMode.value)
        })
        viewModel.images.currentImageProperty().addListener(InvalidationListener {
            setZoomMode(viewModel.zoomMode.value)
        })
        imageView.imageProperty().bind(viewModel.images.currentImageProperty())
    }

    private fun setZoomMode(zoomMode: ZoomMode) {
        val image = viewModel.images.getCurrentImage()
        imageView.fitWidthProperty().unbind()
        imageView.fitHeightProperty().unbind()
        imageView.fitWidth = 0.0
        imageView.fitHeight = 0.0
        background.maxWidth = image.width
        background.maxHeight = image.height
        when (zoomMode) {
            ZoomMode.PERCENT_100 -> imageView.fitWidth = 0.0
            ZoomMode.PERCENT_200 -> {
                imageView.fitWidth = image.width * 2
                background.maxWidth = image.width * 2
                background.maxHeight = image.height * 2
            }
            ZoomMode.FIT_TO_WINDOW -> {
                // Need to subtract 2 to account for the border width
                imageView.fitWidthProperty().bind(container.width.subtract(2))
                imageView.fitHeightProperty().bind(container.height.subtract(2))
                background.maxWidth = -1.0
                background.maxHeight = -1.0
            }
            ZoomMode.HALF_SCREEN -> {
                imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                imageView.fitHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                background.maxWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                background.maxHeight = SCREEN_HEIGHT - HEIGHT_PADDING
            }
        }
    }
}
