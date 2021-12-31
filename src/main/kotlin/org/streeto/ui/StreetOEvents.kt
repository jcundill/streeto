package org.streeto.ui

import tornadofx.FXEvent

object ResetRotationEvent : FXEvent()
object ZoomToFitCourseEvent : FXEvent()
object ZoomToFitLegEvent : FXEvent()
object NextLegEvent : FXEvent()
object PreviousLegEvent : FXEvent()
class ControlSelectedEvent(val control: Control) : FXEvent()
class ZoomToControlEvent(val control: Control?) : FXEvent()
class RouteVisibilityEvent(val visible: Boolean) : FXEvent()
class RouteChoiceVisibilityEvent(val visible: Boolean) : FXEvent()
object CourseUpdatedEvent : FXEvent()
class NewMapLocationEvent(val lat: Double, val lon: Double) : FXEvent()