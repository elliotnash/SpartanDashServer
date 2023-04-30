package org.chsrobotics.dash.widget

import org.chsrobotics.dash.SpartanDash
import org.chsrobotics.dash.ValueUpdater
import org.chsrobotics.dash.model.SelectorWidgetModel
import java.util.*

abstract class SpartanWidget {
    val uuid = UUID.randomUUID().toString()
    abstract var name: String?
}

abstract class SplittableWidget : SpartanWidget()

class SelectorWidget(
    name: String?,
    options: Map<String, String>,
    selected: String?
) : SplittableWidget() {
    init {
        SpartanDash.widgetModels[uuid] = ValueUpdater(SelectorWidgetModel(uuid, name, options, selected))
    }
    private var model: SelectorWidgetModel get() = SpartanDash.widgetModels[uuid]!!.current as SelectorWidgetModel
        set(value) {
            SpartanDash.widgetModels[uuid]!!.current = value
        }
    override var name get() = model.name
        set(value) { model = model.copy(name = value) }
    var options get() = model.options
        set(value) { model = model.copy(options = value) }
    var selected get() = model.selected
        set(value) { model = model.copy(selected = value) }
}
