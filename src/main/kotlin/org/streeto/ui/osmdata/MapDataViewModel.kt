package org.streeto.ui.osmdata

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.streeto.osmdata.MapData
import tornadofx.*
import java.time.LocalDate

class MapDataModel(name: String, date: LocalDate) : MapData(name, null, date) {
    val nameProperty = SimpleStringProperty(name)
    val dateProperty = SimpleObjectProperty(date)
}

class MapDataViewModel : ItemViewModel<MapDataModel>() {
    val name = bind(MapDataModel::nameProperty)
    val date = bind(MapDataModel::dateProperty)
}