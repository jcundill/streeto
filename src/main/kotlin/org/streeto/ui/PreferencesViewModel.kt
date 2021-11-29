package org.streeto.ui

import javafx.beans.property.*
import org.streeto.StreetOPreferences
import tornadofx.*

class PreferencesViewModel : ItemViewModel<ObservablePreferences>() {

    val routeChoiceWeighting = bind(ObservablePreferences::routeChoiceWeightingProperty)
    val legLengthWeighting = bind(ObservablePreferences::routeChoiceWeightingProperty)
    val legComplexityWeighting = bind(ObservablePreferences::legComplexityWeightingProperty)
    val beenHereBeforeWeighting = bind(ObservablePreferences::beenHereBeforeWeightingProperty)
    val comesTooCloseWeighting = bind(ObservablePreferences::comesTooCloseWeightingProperty)
    val dogLegWeighting = bind(ObservablePreferences::dogLegWeightingProperty)
    val distinctControlSiteWeighting = bind(ObservablePreferences::distinctControlSiteWeightingProperty)
    val maxExecutionTime = bind(ObservablePreferences::maxExecutionTimeProperty)
    val maxGenerations = bind(ObservablePreferences::maxGenerationsProperty)
    val stoppingFitness = bind(ObservablePreferences::stoppingFitnessProperty)
    val controlSwapProbability = bind(ObservablePreferences::controlSwapProbabilityProperty)
    val mutateProbability = bind(ObservablePreferences::mutateProbabilityProperty)
    val maxFirstControlDistance = bind(ObservablePreferences::maxFirstControlDistanceProperty)
    val maxLastLegLength = bind(ObservablePreferences::maxLastLegLengthProperty)
    val minLegDistance = bind(ObservablePreferences::minLegDistanceProperty)
    val maxLegDistance = bind(ObservablePreferences::maxLegDistanceProperty)
    val allowedCourseLengthDelta = bind(ObservablePreferences::allowedCourseLengthDeltaProperty)
    val maxMapScale = bind(ObservablePreferences::maxMapScaleProperty)
    val minApproachToFinish = bind(ObservablePreferences::minApproachToFinishProperty)
    val offspringFraction = bind(ObservablePreferences::offspringFractionProperty)
    val populationSize = bind(ObservablePreferences::populationSizeProperty)
    val mutationRadius = bind(ObservablePreferences::mutationRadiusProperty)
    val maxPhenotypeAge = bind(ObservablePreferences::maxPhenotypeAgeProperty)
    val maxRouteShare = bind(ObservablePreferences::maxRouteShareProperty)
    val maxFurnitureDistance = bind(ObservablePreferences::maxFurnitureDistanceProperty)
    val junctionScoreFactor = bind(ObservablePreferences::junctionScoreFactorProperty)
    val minTurnAngle = bind(ObservablePreferences::minTurnAngleProperty)
    val splitForBetterScale = bind(ObservablePreferences::splitForBetterScaleProperty)
    val mapStyle = bind(ObservablePreferences::mapStyleProperty)
    val paperSize = bind(ObservablePreferences::paperSizeProperty)
    val printA3OnA4 = bind(ObservablePreferences::printA3OnA4Property)
}