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

import components.Window
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import models.ViewModel
import java.io.File
import java.util.*

fun main(args: Array<String>) {
    Application.launch(AnImageViewer::class.java, *args)
}

class AnImageViewer : Application() {
    override fun start(primaryStage: Stage) {
        try {
            val viewModel = ViewModel(File(parameters.raw.first()))
            val stage = Window(viewModel)
            stage.icons.add(Image(javaClass.getResourceAsStream("256.png")))
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
