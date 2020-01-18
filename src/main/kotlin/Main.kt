import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import javafx.stage.StageStyle

fun main(args: Array<String>) {
    Application.launch(AnImageViewer::class.java, *args)
}

class AnImageViewer : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.icons.add(Image(javaClass.getResourceAsStream("Taskbar.png")))
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.width = 0.0
        primaryStage.height = 0.0
        primaryStage.show()

        try {
            val controller = MainController(parameters.raw.first())
            val stage = FXMLLoader(javaClass.getResource("MainWindow.fxml"), null, null, {
                controller
            }).load<Stage>()
            stage.initOwner(primaryStage)
            stage.setOnHidden { Platform.runLater(primaryStage::hide) }
            stage.icons.add(Image(javaClass.getResourceAsStream("TitleBar.png")))

            controller.aboutMenuItem.setOnAction {
                with(FXMLLoader.load<Stage>(javaClass.getResource("AboutWindow.fxml"))) {
                    initStyle(StageStyle.UTILITY)
                    addEventHandler(KeyEvent.KEY_RELEASED) {
                        if (it.code == KeyCode.ESCAPE || (it.code == KeyCode.W && it.isShortcutDown)) {
                            close()
                        }
                    }
                    show()
                }
            }
            stage.show()
        } catch (e: NoSuchElementException) {
            println("No image provided. Exiting ...")
            Platform.exit()
        } catch (e: Exception) {
            e.printStackTrace()
            System.err.println("Loading image ${parameters.raw[0]} failed")
            Platform.exit()
        }
    }
}
