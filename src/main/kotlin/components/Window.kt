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

package components

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ComboBox
import javafx.scene.control.ScrollPane
import javafx.scene.input.*
import javafx.stage.Stage
import javafx.stage.StageStyle
import models.IViewModel
import models.ZoomMode
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.Callable

private val ZOOM_SHORTCUTS = mapOf(
    KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN) to ZoomMode.PERCENT_100,
    KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN) to ZoomMode.PERCENT_200,
    KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN) to ZoomMode.FIT_TO_WINDOW,
    KeyCodeCombination(KeyCode.MINUS) to ZoomMode.HALF_SCREEN
).asSequence()

private val QUIT_SHORTCUTS = setOf(
    KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
    KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)
)

private val COPY_SHORTCUT = KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN)
private val ABOUT_SHORTCUT = KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN)

// Note that this, despite IntelliJ's suggestion, CANNOT be private.
// The FXML Loader needs to access this later during instantiation of `AnImageView`.
class Window(val viewModel: IViewModel) : Stage(), IImageContainer {
    @FXML
    private lateinit var notification: Notification

    @FXML
    lateinit var zoomSelection: ComboBox<ZoomMode>

    @FXML
    lateinit var scrollPane: ScrollPane

    @FXML
    private lateinit var anImageView: AnImageView

    override val width: ReadOnlyDoubleProperty
        get() = scrollPane.widthProperty()
    override val height: ReadOnlyDoubleProperty
        get() = scrollPane.heightProperty()

    init {
        FXMLLoader(javaClass.getResource("Window.fxml")).apply {
            setRoot(this@Window)
            setController(this@Window)
            load()
        }
        zoomSelection.items = FXCollections.observableArrayList(*ZoomMode.values())
        zoomSelection.valueProperty().bindBidirectional(viewModel.zoomMode)
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.LEFT) {
                viewModel.previousImage()
            } else if (it.code == KeyCode.RIGHT) {
                viewModel.nextImage()
            }
        }
        titleProperty().bind(Bindings.createStringBinding(Callable {
            "An Image Viewer – ${Paths.get(URI.create(viewModel.images.getCurrentImage().url))}"
        }, viewModel.images.currentImageProperty()))
        addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
            ZOOM_SHORTCUTS.firstOrNull { it.key.match(keyEvent) }?.let {
                zoomSelection.value = it.value
                keyEvent.consume()
                return@addEventHandler
            }
            if (COPY_SHORTCUT.match(keyEvent)) {
                onCopyClick(keyEvent)
            } else if (QUIT_SHORTCUTS.any { it.match(keyEvent) }) {
                Platform.exit()
            } else if (ABOUT_SHORTCUT.match(keyEvent)) {
                with(FXMLLoader.load<Stage>(javaClass.getResource("AboutWindow.fxml"))) {
                    initStyle(StageStyle.UTILITY)
                    addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
                        if (keyEvent.code == KeyCode.ESCAPE || QUIT_SHORTCUTS.any { it.match(keyEvent) }) {
                            close()
                        }
                    }
                    show()
                }
            }
            keyEvent.consume()
        }
        // ...
        scrollPane.requestFocus()
    }

    fun onCopyClick(event: Event) {
        Clipboard.getSystemClipboard().setContent(ClipboardContent().apply {
            putImage(anImageView.imageView.image)
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
            viewModel.images.initialize(dragEvent.dragboard.files.first())
        } catch (e: NoSuchElementException) {
            notification.show()
        }
        dragEvent.consume()
    }

    fun onPreviousClick(event: Event) {
        viewModel.previousImage()
        event.consume()
    }

    fun onNextClick(event: Event) {
        viewModel.nextImage()
        event.consume()
    }
}
