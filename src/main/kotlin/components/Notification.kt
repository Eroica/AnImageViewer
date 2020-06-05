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

package components

import javafx.animation.Interpolator
import javafx.animation.PauseTransition
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration

private val ANIM_DURATION = Duration.millis(800.0)
private val EASING = Interpolator.SPLINE(.02, .98, .46, .95)

interface INotification {
    fun show()
}

class Notification : StackPane(), INotification {
    private var isRunning = false

    init {
        FXMLLoader(javaClass.getResource("NotificationPane.fxml")).apply {
            setRoot(this@Notification)
            setController(this@Notification)
            load()
        }
        this.clip = Rectangle(maxWidth, prefHeight)
    }

    override fun show() {
        if (!isRunning) {
            isRunning = true
            val label = FXMLLoader.load<Label>(javaClass.getResource("Notification_TypeError.fxml"))
            label.translateY = -prefHeight
            children.add(label)

            val slideDown = TranslateTransition(ANIM_DURATION, label)
            slideDown.byY = prefHeight
            slideDown.interpolator = EASING
            val slideUp = TranslateTransition(ANIM_DURATION, label)
            slideUp.byY = -prefHeight
            slideUp.interpolator = EASING

            SequentialTransition(slideDown, PauseTransition(Duration.seconds(2.0)), slideUp).apply {
                setOnFinished {
                    isRunning = false
                    this@Notification.children.remove(label)
                }
                play()
            }
        }
    }
}
