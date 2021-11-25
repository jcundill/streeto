package org.streeto.ui

import javafx.beans.property.*
import org.streeto.StreetOPreferences
import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize

class ObservablePreferences : StreetOPreferences() {
    val routeChoiceWeightingProperty = SimpleDoubleProperty(routeChoiceWeighting)
     val legLengthWeightingProperty = SimpleDoubleProperty(legLengthWeighting)
     val legComplexityWeightingProperty = SimpleDoubleProperty(legComplexityWeighting)
     val beenHereBeforeWeightingProperty = SimpleDoubleProperty(beenHereBeforeWeighting)
     val comesTooCloseWeightingProperty = SimpleDoubleProperty(comesTooCloseWeighting)
     val dogLegWeightingProperty = SimpleDoubleProperty(dogLegWeighting)
     val distinctControlSiteWeightingProperty = SimpleDoubleProperty(distinctControlSiteWeighting)
     val maxExecutionTimeProperty = SimpleLongProperty(maxExecutionTime)
     val maxGenerationsProperty = SimpleLongProperty(maxGenerations)
     val stoppingFitnessProperty  = SimpleDoubleProperty(stoppingFitness)
     val controlSwapProbabilityProperty = SimpleDoubleProperty(swapProbability)
     val mutateProbabilityProperty = SimpleDoubleProperty(mutateProbability)
     val maxFirstControlDistanceProperty = SimpleDoubleProperty(maxFirstControlDistance)
     val maxLastLegLengthProperty = SimpleDoubleProperty(maxLastLegLength)
     val minLegDistanceProperty = SimpleDoubleProperty(minLegDistance)
     val maxLegDistanceProperty = SimpleDoubleProperty(maxLegDistance)
     val allowedCourseLengthDeltaProperty = SimpleDoubleProperty(allowedCourseLengthDelta)
     val maxMapScaleProperty = SimpleDoubleProperty(maxMapScale)
     val minApproachToFinishProperty = SimpleDoubleProperty(minApproachToFinish)
     val offspringFractionProperty = SimpleDoubleProperty(offspringFraction)
     val populationSizeProperty = SimpleIntegerProperty(populationSize)
     val mutationRadiusProperty = SimpleDoubleProperty(mutationRadius)
     val maxPhenotypeAgeProperty = SimpleLongProperty(maxPhenotypeAge)
     val maxRouteShareProperty = SimpleDoubleProperty(maxRouteShare)
     val maxFurnitureDistanceProperty = SimpleDoubleProperty(maxFurnitureDistance)
     val junctionScoreFactorProperty = SimpleDoubleProperty(junctionScoreFactor)
     val minTurnAngleProperty = SimpleDoubleProperty(minTurnAngle)
     val splitForBetterScaleProperty = SimpleBooleanProperty(isSplitForBetterScale)
     val mapStyleProperty  = SimpleObjectProperty(mapStyle)
     val paperSizeProperty = SimpleObjectProperty(paperSize)
     val printA3OnA4Property = SimpleBooleanProperty(isPrintA3OnA4)
}