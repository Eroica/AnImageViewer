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

fun main(args: Array<String>) {
    Application.launch(AnImageViewer::class.java, *args)
}

private val QUIT_COMBINATIONS = setOf(
    KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
    KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)
)

class AnImageViewer : Application() {
    override fun start(primaryStage: Stage) {
        try {
            val controller = MainController(parameters.raw.first())
            val stage = FXMLLoader(javaClass.getResource("MainWindow.fxml"), null, null, {
                controller
            }).load<Stage>()
            stage.icons.add(Image(javaClass.getResourceAsStream("16.png")))

            stage.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
                if (QUIT_COMBINATIONS.any { it.match(keyEvent) }) {
                    Platform.exit()
                }
            }

            val aboutCombination = KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN)
            stage.addEventHandler(KeyEvent.KEY_RELEASED) {
                if (aboutCombination.match(it)) {
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
