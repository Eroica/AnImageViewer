import javafx.animation.Interpolator
import javafx.animation.PauseTransition
import javafx.animation.SequentialTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration

private val ANIM_DURATION = Duration.millis(800.0)
private val EASING = Interpolator.SPLINE(.02, .98, .46, .95)

class NotificationController {
    @FXML
    private lateinit var stackPane: StackPane

    fun initialize() {
        stackPane.clip = Rectangle(stackPane.maxWidth, stackPane.prefHeight)
    }

    fun showError() {
        val label = FXMLLoader.load<Label>(javaClass.getResource("Notification_TypeError.fxml"))
        label.translateY = -stackPane.prefHeight
        stackPane.children.add(label)

        val slideDown = TranslateTransition(ANIM_DURATION, label)
        slideDown.byY = stackPane.prefHeight
        slideDown.interpolator = EASING
        val slideUp = TranslateTransition(ANIM_DURATION, label)
        slideUp.byY = -stackPane.prefHeight
        slideUp.interpolator = EASING

        SequentialTransition(slideDown, PauseTransition(Duration.seconds(2.0)), slideUp).apply {
            setOnFinished { stackPane.children.remove(label) }
            play()
        }
    }
}
