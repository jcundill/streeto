package org.streeto.ui.osmdata

import org.streeto.osmdata.MapDataRepository
import org.streeto.ui.CourseController
import tornadofx.*

class OsmDataController : Controller() {
    val mapsList = SortedFilteredList<MapDataModel>()
    private val courseController: CourseController by inject()
    private val mapDataRepository = MapDataRepository(courseController.osmDir.value)

    fun loadMaps() {
        mapsList.clear()
        mapDataRepository.load()
        val models = mapDataRepository.mapDataList.map { MapDataModel(it.name, it.date) }
        mapsList.addAll(models)
    }

    fun deleteMapData(model: MapDataModel) {
        mapDataRepository.deleteMapData(model.name)
    }

    fun updateMapData(model: MapDataModel) {
        mapDataRepository.updateMapData(model.name)
    }
}


