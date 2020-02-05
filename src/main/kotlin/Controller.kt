import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleGroup
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import java.io.File
import java.net.URI
import java.util.concurrent.Callable

const val MAX_IMAGE_SIZE = 800.0

class MainController(initialImagePath: String) {
    @FXML
    lateinit var stage: Stage

    @FXML
    lateinit var scrollPane: ScrollPane

    @FXML
    lateinit var imageView: ImageView

    @FXML
    lateinit var aboutMenuItem: MenuItem

    @FXML
    lateinit var zoomMode: ToggleGroup

    private val currentImage = SimpleObjectProperty<Image>()
    private val images = PreloadedImages(File(initialImagePath))

    fun initialize() {
        zoomMode.selectedToggleProperty().addListener(InvalidationListener { setZoomMode(imageView.image) })
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            if (keyEvent.code == KeyCode.LEFT) {
                images.previousImage()?.let { loadImage(it) }
                keyEvent.consume()
            }
            if (keyEvent.code == KeyCode.RIGHT) {
                images.nextImage()?.let { loadImage(it) }
                keyEvent.consume()
            }
        }
        images.currentImage()?.let {
            if (it.width > MAX_IMAGE_SIZE) {
                imageView.fitWidth = MAX_IMAGE_SIZE
            } else if (it.height > MAX_IMAGE_SIZE) {
                imageView.fitHeight = MAX_IMAGE_SIZE
            }
            currentImage.bind(imageView.imageProperty())
            imageView.image = it
        }
        stage.titleProperty().bind(Bindings.createStringBinding(Callable {
            "An Image Viewer – ${URI(currentImage.get().url).path}"
        }, currentImage))
    }

    private fun loadImage(image: Image) {
        setZoomMode(image)
        imageView.image = image
    }

    fun previousImage(actionEvent: ActionEvent) {
        images.previousImage()?.let { loadImage(it) }
        actionEvent.consume()
    }

    fun nextImage(actionEvent: ActionEvent) {
        images.nextImage()?.let { loadImage(it) }
        actionEvent.consume()
    }

    fun copyToClipboard(actionEvent: ActionEvent) {
        Clipboard.getSystemClipboard().setContent(ClipboardContent().apply {
            putImage(imageView.image)
        })
        actionEvent.consume()
    }

    fun close() {
        Platform.exit()
    }

    private fun setZoomMode(image: Image) {
        imageView.fitWidthProperty().unbind()
        when (zoomMode.toggles.indexOf(zoomMode.selectedToggle)) {
            0 -> imageView.fitWidth = 0.0
            1 -> imageView.fitWidth = image.width * 2
            2 -> imageView.fitWidthProperty().bind(stage.widthProperty().subtract(50.0))
            3 -> imageView.fitWidth = if (image.width > MAX_IMAGE_SIZE) MAX_IMAGE_SIZE else 0.0
        }
    }
}
