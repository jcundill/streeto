package org.streeto.ui.geocode

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import org.streeto.ui.NewMapLocationEvent

import tornadofx.*

class NominatimView : View("Location Lookup") {
    private val finder = AddressLookup()
    private val source = SimpleStringProperty()
    private val results: ObservableList<Address> = mutableListOf<Address>().asObservable()

    override fun onBeforeShow() {
        super.onBeforeShow()
        source.value = ""
        results.clear()
    }

    private fun lookupAddress() {
        results.clear()
        runAsyncWithOverlay {
            finder.lookup(source.value)
        } ui {
            results.addAll(it)
            if (results.isEmpty()) {
                results.add(Address("No results found", 0.0, 0.0))
            }
        }
    }

    override val root = borderpane {
        prefWidth = 600.0
        top {
            hbox {
                paddingAll = 10.0
                textfield {
                    hgrow = Priority.ALWAYS
                    prefWidth = 500.0
                    textProperty().bindBidirectional(source)
                    onKeyPressed = EventHandler {
                        if (it.code == KeyCode.ENTER) {
                            lookupAddress()
                        }
                    }
                }
                label(" ")
                button("Find") {
                    action {
                        lookupAddress()
                    }
                }
            }
        }
        center {
            listview(results) {
                maxHeight = 200.0
                minWidth = 500.0
                isEditable = false
                cellFormat {
                    text = it.name
                }
                this.onUserSelect {
                    fire(NewMapLocationEvent(it.lat, it.lon))
                    this@NominatimView.close()
                }
            }
        }
        bottom {
            hyperlink("Address Geolocation by Nominatim, OpenStreetMap") {
                action {
                    hostServices.showDocument("https://operations.osmfoundation.org/policies/nominatim/")
                }
            }
        }
    }
}