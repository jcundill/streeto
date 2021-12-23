package org.streeto.ui

import tornadofx.View

abstract class StreetOView(title: String) : View(title) {
    override fun onBeforeShow() {
        Styles.styleView(this)
        super.onBeforeShow()
    }
}