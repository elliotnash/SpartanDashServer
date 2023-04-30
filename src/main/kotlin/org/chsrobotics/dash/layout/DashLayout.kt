package org.chsrobotics.dash.layout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.chsrobotics.dash.model.DashLayoutModel
import org.chsrobotics.dash.model.FullWidgetPlacementModel
import org.chsrobotics.dash.model.SplitWidgetPlacementModel
import org.chsrobotics.dash.model.WidgetPlacementModel
import org.chsrobotics.dash.widget.SpartanWidget
import org.chsrobotics.dash.widget.SplittableWidget

class DashLayout internal constructor(
    val columns: Int,
    val rows: Int
) {
    private val widgets = mutableSetOf<WidgetPlacementModel>()
    fun SpartanWidget.place(column: Int, row: Int, columnSpan: Int = 1, rowSpan: Int = 1) {
        widgets.add(FullWidgetPlacementModel(
            widgetUuid = uuid,
            column = column,
            row = row,
            columnSpan = columnSpan,
            rowSpan = rowSpan
        ))
    }
    fun SplittableWidget.placeSplit(position: SplitWidgetPosition, column: Int, row: Int) {
        widgets.add(SplitWidgetPlacementModel(
            widgetUuid = uuid,
            column = column,
            row = row,
            position = position
        ))
    }

    internal fun toDashLayoutModel(): DashLayoutModel {
        return DashLayoutModel(
            columns = columns,
            rows = rows,
            widgets = widgets.toList()
        )
    }
}

@Serializable
enum class SplitWidgetPosition {
    @SerialName("top")
    TOP,
    @SerialName("bottom")
    BOTTOM
}
