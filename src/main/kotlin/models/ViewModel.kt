/* ViewModel.kt

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

package models

import PreloadedImages
import javafx.beans.property.SimpleObjectProperty
import java.io.File

enum class ZoomMode(private val label: String) {
    PERCENT_100("100%"),
    PERCENT_200("200%"),
    FIT_TO_WINDOW("Fit to window size"),
    HALF_SCREEN("Fit on screen");

    override fun toString(): String {
        return this.label
    }
}

interface IViewModel {
    fun nextImage()
    fun previousImage()

    val images: PreloadedImages
    val zoomMode: SimpleObjectProperty<ZoomMode>
}

data class ViewModel(private var initialPath: File) : IViewModel {
    override val images: PreloadedImages = PreloadedImages(initialPath)
    override val zoomMode = SimpleObjectProperty<ZoomMode>(ZoomMode.PERCENT_100)

    override fun nextImage() {
        images.moveForward()
    }

    override fun previousImage() {
        images.moveBack()
    }
}
