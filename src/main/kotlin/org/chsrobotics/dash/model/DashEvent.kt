package org.chsrobotics.dash.model

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
sealed interface DashEvent {
}
