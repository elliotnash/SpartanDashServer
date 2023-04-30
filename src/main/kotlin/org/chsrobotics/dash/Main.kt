package org.chsrobotics.dash

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.chsrobotics.dash.model.*
import org.chsrobotics.dash.widget.SelectorWidget

fun main(args: Array<String>) {
//    val layoutJson = """
//        {
//          "type": "layout",
//          "layout": {
//            "columns": 3,
//            "rows": 2,
//            "widgets": [
//              {
//                "type": "split",
//                "widgetUuid": "0",
//                "column": 0,
//                "row": 0,
//                "position": "top"
//              },
//              {
//                "type": "split",
//                "widgetUuid": "0",
//                "column": 0,
//                "row": 0,
//                "position": "bottom"
//              },
//              {
//                "type": "full",
//                "widgetUuid": "1",
//                "column": 0,
//                "row": 1,
//                "columnSpan": 1,
//                "rowSpan": 1
//              }
//            ]
//          }
//        }
//    """.trim()
//
//    val selectorJson = """
//        {
//          "type": "widget",
//          "widget": {
//            "type": "selector",
//            "uuid": "8cb783de-f95b-41ba-a7cb-c6bee132bc95",
//            "name": "Drive Mode",
//            "options": {
//              "arcade": "Arcade",
//              "curvature": "Curvature"
//            },
//            "selected": "arcade"
//          }
//        }
//    """.trimIndent()
//
//    val layoutEvent = SpartanDash.json.decodeFromString<DashEvent>(layoutJson)
//    val selectorEvent = SpartanDash.json.decodeFromString<DashEvent>(selectorJson)

    val selector = SelectorWidget(
        "Drive Mode",
        mutableMapOf(
            "arcade" to "Arcade",
            "curvature" to "Curvature"
        ),
        "arcade"
    )

    SpartanDash {
        selector.place(0, 0)
    }

    selector.selected = "curvature"

//
//    selector.options = selector.options + ("mixed" to "Mixed Curvature")
//
    runBlocking {
        while (true) {
            delay(1000)
        }
    }
}
