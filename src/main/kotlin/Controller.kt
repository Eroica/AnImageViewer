/* Controller.kt

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

import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.stage.Screen
import javafx.stage.Stage
import java.io.File
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.Callable

enum class ZOOM_MODE(private val label: String) {
    PERCENT_100("100%"),
    PERCENT_200("200%"),
    FIT_TO_WINDOW("Fit to window size"),
    HALF_SCREEN("Half of the screen");

    override fun toString(): String {
        return this.label
    }
}

// Arbitrary width and height padding to leave room for taskbars/menu bars
private val WIDTH_PADDING = 100
private val HEIGHT_PADDING = 200

private val SCREEN_HEIGHT = Screen.getPrimary().bounds.height / Screen.getPrimary().outputScaleY
private val HALF_SCREEN_WIDTH = Screen.getPrimary().bounds.width / Screen.getPrimary().outputScaleX / 2

class MainController(initialImagePath: String) {
    @FXML
    lateinit var stage: Stage

    @FXML
    lateinit var scrollPane: ScrollPane

    @FXML
    lateinit var previousButton: Button

    @FXML
    lateinit var nextButton: Button

    @FXML
    lateinit var imageView: ImageView

    @FXML
    lateinit var zoomSelection: ComboBox<ZOOM_MODE>

    private val currentImage = SimpleObjectProperty<Image>()
    private var images = PreloadedImages(File(initialImagePath))

    private val keyCombinations = setOf(
        Pair(KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), ZOOM_MODE.PERCENT_100),
        Pair(KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), ZOOM_MODE.PERCENT_200),
        Pair(KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN), ZOOM_MODE.FIT_TO_WINDOW),
        Pair(KeyCodeCombination(KeyCode.MINUS), ZOOM_MODE.HALF_SCREEN)
    )

    fun initialize() {
        zoomSelection.items = FXCollections.observableArrayList(*ZOOM_MODE.values())
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.LEFT) {
                onPreviousClick(it)
            }
            if (it.code == KeyCode.RIGHT) {
                onNextClick(it)
            }
        }
        previousButton.disableProperty().bind(images.indexProperty.isEqualTo(1))
        nextButton.disableProperty().bind(images.indexProperty.isEqualTo(images.size - 2))
        images.currentImage()?.let {
            when {
                it.height > SCREEN_HEIGHT -> {
                    zoomSelection.value = ZOOM_MODE.HALF_SCREEN
                    val aspectRatio = it.width / it.height
                    imageView.fitWidth = (SCREEN_HEIGHT - HEIGHT_PADDING) * aspectRatio
                }
                it.width > HALF_SCREEN_WIDTH -> {
                    zoomSelection.value = ZOOM_MODE.HALF_SCREEN
                    imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                }
                else -> {
                    zoomSelection.value = ZOOM_MODE.PERCENT_100
                }
            }
            currentImage.bind(imageView.imageProperty())
            imageView.image = it
        }
        zoomSelection.valueProperty().addListener(InvalidationListener {
            setZoomMode(imageView.image)
        })
        stage.titleProperty().bind(Bindings.createStringBinding(Callable {
            "An Image Viewer – ${Paths.get(URI.create(currentImage.get().url))}"
        }, currentImage))
        stage.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
            keyCombinations.firstOrNull { it.first.match(keyEvent) }?.let {
                zoomSelection.value = it.second
            }
        }
        scrollPane.requestFocus()
    }

    private fun loadImage(image: Image) {
        setZoomMode(image)
        imageView.image = image
    }

    fun onPreviousClick(event: Event) {
        images.previousImage()?.let { loadImage(it) }
        event.consume()
    }

    fun onNextClick(event: Event) {
        images.nextImage()?.let { loadImage(it) }
        event.consume()
    }

    fun onCopyClick(event: Event) {
        Clipboard.getSystemClipboard().setContent(ClipboardContent().apply {
            putImage(imageView.image)
        })
        event.consume()
    }

    fun onDragOver(dragEvent: DragEvent) {
        if (dragEvent.dragboard.hasFiles()) {
            dragEvent.acceptTransferModes(*TransferMode.ANY)
        }
        dragEvent.consume()
    }

    fun onDragDropped(dragEvent: DragEvent) {
        images = PreloadedImages(dragEvent.dragboard.files.first())
        images.currentImage()?.let { loadImage(it) }
        dragEvent.consume()
    }

    private fun setZoomMode(image: Image) {
        imageView.fitWidthProperty().unbind()
        imageView.fitHeightProperty().unbind()
        when (zoomSelection.value) {
            ZOOM_MODE.PERCENT_100 -> imageView.fitWidth = 0.0
            ZOOM_MODE.PERCENT_200 -> imageView.fitWidth = image.width * 2
            ZOOM_MODE.FIT_TO_WINDOW -> {
                // Need to subtract 2 to make up for the border width
                imageView.fitWidthProperty().bind(scrollPane.widthProperty().subtract(2))
                imageView.fitHeightProperty().bind(scrollPane.heightProperty().subtract(2))
            }
            ZOOM_MODE.HALF_SCREEN -> {
                if (image.height > SCREEN_HEIGHT) {
                    val aspectRatio = image.width / image.height
                    imageView.fitWidth = (SCREEN_HEIGHT - HEIGHT_PADDING) * aspectRatio
                } else if (image.width > HALF_SCREEN_WIDTH) {
                    imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                }
            }
            null -> imageView.fitWidth = 0.0
        }
    }
}
