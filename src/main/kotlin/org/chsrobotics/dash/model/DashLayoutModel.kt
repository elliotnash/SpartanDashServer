package org.chsrobotics.dash.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.chsrobotics.dash.layout.SplitWidgetPosition

@Serializable
@SerialName("layout")
data class DashLayoutEvent(
    val layout: DashLayoutModel
) : DashEvent

@Serializable
data class DashLayoutModel(
    val columns: Int,
    val rows: Int,
    val widgets: List<WidgetPlacementModel> = listOf()
)

@Serializable
sealed class WidgetPlacementModel {
    abstract val widgetUuid: String
    abstract val column: Int
    abstract val row: Int
}

@Serializable
@SerialName("full")
data class FullWidgetPlacementModel(
    override val widgetUuid: String,
    override val column: Int,
    override val row: Int,
    val columnSpan: Int,
    val rowSpan: Int
) : WidgetPlacementModel()

@Serializable
@SerialName("split")
data class SplitWidgetPlacementModel(
    override val widgetUuid: String,
    override val column: Int,
    override val row: Int,
    val position: SplitWidgetPosition
) : WidgetPlacementModel()
