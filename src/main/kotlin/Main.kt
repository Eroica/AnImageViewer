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

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.*

fun main(args: Array<String>) {
    Application.launch(AnImageViewer::class.java, *args)
}

private val QUIT_COMBINATIONS = setOf(
    KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
    KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)
)

private val ABOUT_COMBINATION = KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN)

class AnImageViewer : Application() {
    override fun start(primaryStage: Stage) {
        try {
            // The controller is instantiated here and not inside the ControllerFactory of `stage'.
            // Otherwise any exception (e.g. when there are no elements in `parameters.raw' will get raised as an
            // LoadException.
            val controller = MainController(parameters.raw.first())
            val stage = FXMLLoader(
                javaClass.getResource("MainWindow.fxml"),
                null,
                null,
                {
                    when (it) {
                        MainController::class.java -> controller
                        else -> throw RuntimeException()
                    }
                })
                .load<Stage>()
            stage.icons.add(Image(javaClass.getResourceAsStream("256.png")))
            stage.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
                if (QUIT_COMBINATIONS.any { it.match(keyEvent) }) {
                    Platform.exit()
                }
                if (ABOUT_COMBINATION.match(keyEvent)) {
                    with(FXMLLoader.load<Stage>(javaClass.getResource("AboutWindow.fxml"))) {
                        initStyle(StageStyle.UTILITY)
                        addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
                            if (keyEvent.code == KeyCode.ESCAPE || QUIT_COMBINATIONS.any { it.match(keyEvent) }) {
                                close()
                            }
                        }
                        show()
                    }
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
