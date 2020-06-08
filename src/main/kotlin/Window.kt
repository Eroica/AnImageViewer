/* Window.kt

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

import components.AnImageView
import components.IImageContainer
import components.Notification
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ScrollPane
import javafx.scene.input.*
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.Callable

enum class ZOOM_MODE(private val label: String) {
    PERCENT_100("100%"),
    PERCENT_200("200%"),
    FIT_TO_WINDOW("Fit to window size"),
    HALF_SCREEN("Fit on screen");

    override fun toString(): String {
        return this.label
    }
}

interface IZoomMode {
    fun zoomModeProperty(): ObjectProperty<ZOOM_MODE>
}

interface IPath {
    val imagePath: String
}

class MainController(initialImagePath: String) : IImageContainer, IZoomMode, IPath {
    @FXML
    lateinit var stage: Stage

    @FXML
    lateinit var pane: StackPane

    @FXML
    lateinit var scrollPane: ScrollPane

    @FXML
    lateinit var zoomSelection: ComboBox<ZOOM_MODE>

    @FXML
    private lateinit var notification: Notification

    @FXML
    private lateinit var image: AnImageView

    override fun zoomModeProperty(): ObjectProperty<ZOOM_MODE> {
        return zoomSelection.valueProperty()
    }

    override val width: ReadOnlyDoubleProperty
        get() = scrollPane.widthProperty()
    override val height: ReadOnlyDoubleProperty
        get() = scrollPane.heightProperty()

    override val imagePath = initialImagePath

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
            } else if (it.code == KeyCode.RIGHT) {
                onNextClick(it)
            }
        }
        stage.titleProperty().bind(Bindings.createStringBinding(Callable {
            "An Image Viewer – ${Paths.get(URI.create(image.images.getCurrentImage().url))}"
        }, image.images.currentImageProperty()))
        stage.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
            keyCombinations.firstOrNull { it.key.match(keyEvent) }?.let {
                zoomSelection.value = it.value
            }
        }
        // ...
        scrollPane.requestFocus()
    }

    fun onPreviousClick(event: Event) {
        image.previous()
        event.consume()
    }

    fun onNextClick(event: Event) {
        image.next()
        event.consume()
    }

    fun onCopyClick(event: Event) {
        Clipboard.getSystemClipboard().setContent(ClipboardContent().apply {
            putImage(this@MainController.image.imageView.image)
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
            image.images.initialize(dragEvent.dragboard.files.first())
        } catch (e: NoSuchElementException) {
            notification.show()
        }
        dragEvent.consume()
    }
}
