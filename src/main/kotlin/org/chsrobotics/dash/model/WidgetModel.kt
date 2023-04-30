package org.chsrobotics.dash.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("widget")
data class WidgetEvent(
    val widget: WidgetModel
) : DashEvent

@Serializable
sealed interface WidgetModel : DashEvent {
    val uuid: String
    val name: String?
}

@Serializable
@SerialName("selector")
data class SelectorWidgetModel(
    override val uuid: String,
    override val name: String?,
    val options: Map<String, String>,
    val selected: String?
) : WidgetModel

@Serializable
@SerialName("toggle")
data class ToggleWidgetModel(
    override val uuid: String,
    override val name: String?,
    val style: Style = Style.BUTTON,
    val text: String,
    val checked: Boolean,
    val checkedText: String?
) : WidgetModel {
    @Serializable
    enum class Style {
        @SerialName("slider")
        SLIDER,
        @SerialName("button")
        BUTTON
    }
}
