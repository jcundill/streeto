package org.streeto.ui

import tornadofx.*

object ResetRotationEvent : FXEvent()
object ZoomToFitCourseEvent : FXEvent()
object ZoomToFitLegEvent : FXEvent()
class RouteVisibilityEvent(val visible: Boolean) : FXEvent()
class RouteChoiceVisibilityEvent(val visible: Boolean) : FXEvent()