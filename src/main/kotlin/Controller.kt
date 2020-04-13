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
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.StackPane
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
private const val WIDTH_PADDING = 100
private const val HEIGHT_PADDING = 200

private val SCREEN_HEIGHT = Screen.getPrimary().bounds.height / Screen.getPrimary().outputScaleY
private val HALF_SCREEN_WIDTH = Screen.getPrimary().bounds.width / Screen.getPrimary().outputScaleX / 2

class MainController(initialImagePath: String) {
    @FXML
    lateinit var notificationController: NotificationController

    @FXML
    lateinit var stage: Stage

    @FXML
    lateinit var pane: StackPane

    @FXML
    lateinit var scrollPane: ScrollPane

    @FXML
    lateinit var imageView: ImageView

    @FXML
    lateinit var zoomSelection: ComboBox<ZOOM_MODE>

    val images = PreloadedImages(File(initialImagePath))

    private val keyCombinations = mapOf(
        KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN) to ZOOM_MODE.PERCENT_100,
        KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN) to ZOOM_MODE.PERCENT_200,
        KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN) to ZOOM_MODE.FIT_TO_WINDOW,
        KeyCodeCombination(KeyCode.MINUS) to ZOOM_MODE.HALF_SCREEN
    ).asSequence()

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

        images.getCurrentImage().apply {
            when {
                height > SCREEN_HEIGHT -> {
                    zoomSelection.value = ZOOM_MODE.HALF_SCREEN
                    val aspectRatio = width / height
                    imageView.fitWidth = (SCREEN_HEIGHT - HEIGHT_PADDING) * aspectRatio
                }
                width > HALF_SCREEN_WIDTH -> {
                    zoomSelection.value = ZOOM_MODE.HALF_SCREEN
                    imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                }
                else -> {
                    zoomSelection.value = ZOOM_MODE.PERCENT_100
                }
            }
        }
        imageView.imageProperty().bind(images.currentImageProperty())
        images.currentImageProperty().addListener(InvalidationListener { setZoomMode() })
        zoomSelection.valueProperty().addListener(InvalidationListener { setZoomMode() })
        stage.titleProperty().bind(Bindings.createStringBinding(Callable {
            "An Image Viewer – ${Paths.get(URI.create(images.getCurrentImage().url))}"
        }, images.currentImageProperty()))
        stage.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
            keyCombinations.firstOrNull { it.key.match(keyEvent) }?.let {
                zoomSelection.value = it.value
            }
        }
        scrollPane.requestFocus()
    }

    fun onPreviousClick(event: Event) {
        images.moveBack()
        event.consume()
    }

    fun onNextClick(event: Event) {
        images.moveForward()
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
        try {
            images.initialize(dragEvent.dragboard.files.first())
        } catch (e: NoSuchElementException) {
            notificationController.showError()
        }
        dragEvent.consume()
    }

    private fun setZoomMode() {
        val image = images.getCurrentImage()
        imageView.fitWidthProperty().unbind()
        imageView.fitHeightProperty().unbind()
        imageView.fitWidth = 0.0
        imageView.fitHeight = 0.0
        when (zoomSelection.value) {
            ZOOM_MODE.PERCENT_100 -> imageView.fitWidth = 0.0
            ZOOM_MODE.PERCENT_200 -> imageView.fitWidth = image.width * 2
            ZOOM_MODE.FIT_TO_WINDOW -> {
                // Need to subtract 2 to account for the border width
                imageView.fitWidthProperty().bind(pane.widthProperty().subtract(2))
                imageView.fitHeightProperty().bind(pane.heightProperty().subtract(2))
            }
            ZOOM_MODE.HALF_SCREEN -> {
                if (image.height > SCREEN_HEIGHT) {
                    imageView.fitHeight = SCREEN_HEIGHT - HEIGHT_PADDING
                } else if (image.width > HALF_SCREEN_WIDTH) {
                    imageView.fitWidth = HALF_SCREEN_WIDTH - WIDTH_PADDING
                }
            }
            null -> imageView.fitWidth = 0.0
        }
    }
}
