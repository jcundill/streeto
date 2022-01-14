package org.streeto.ui.preferences

import javafx.beans.property.*
import org.streeto.StreetOPreferences
import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize

class ObservablePreferences : StreetOPreferences() {
    val routeChoiceWeightingProperty = SimpleDoubleProperty(super.getRouteChoiceWeighting())
    val legLengthWeightingProperty = SimpleDoubleProperty(super.getLegLengthWeighting())
    val legComplexityWeightingProperty = SimpleDoubleProperty(super.getLegComplexityWeighting())
    val beenHereBeforeWeightingProperty = SimpleDoubleProperty(super.getBeenHereBeforeWeighting())
    val comesTooCloseWeightingProperty = SimpleDoubleProperty(super.getComesTooCloseWeighting())
    val dogLegWeightingProperty = SimpleDoubleProperty(super.getDogLegWeighting())
    val distinctControlSiteWeightingProperty = SimpleDoubleProperty(super.getDistinctControlSiteWeighting())
    val maxExecutionTimeProperty = SimpleLongProperty(super.getMaxExecutionTime())
    val maxGenerationsProperty = SimpleLongProperty(super.getMaxGenerations())
    val stoppingFitnessProperty = SimpleDoubleProperty(super.getStoppingFitness())
    val controlSwapProbabilityProperty = SimpleDoubleProperty(super.getSwapProbability())
    val mutateProbabilityProperty = SimpleDoubleProperty(super.getMutateProbability())
    val maxFirstControlDistanceProperty = SimpleDoubleProperty(super.getMaxFirstControlDistance())
    val maxLastLegLengthProperty = SimpleDoubleProperty(super.getMaxLastLegLength())
    val minLegDistanceProperty = SimpleDoubleProperty(super.getMinLegDistance())
    val maxLegDistanceProperty = SimpleDoubleProperty(super.getMaxLastLegLength())
    val allowedCourseLengthDeltaProperty = SimpleDoubleProperty(super.getAllowedCourseLengthDelta())
    val maxMapScaleProperty = SimpleDoubleProperty(super.getMaxMapScale())
    val minApproachToFinishProperty = SimpleDoubleProperty(super.getMinApproachToFinish())
    val offspringFractionProperty = SimpleDoubleProperty(super.getOffspringFraction())
    val populationSizeProperty = SimpleIntegerProperty(super.getPopulationSize())
    val mutationRadiusProperty = SimpleDoubleProperty(super.getMutationRadius())
    val maxPhenotypeAgeProperty = SimpleLongProperty(super.getMaxPhenotypeAge())
    val maxRouteShareProperty = SimpleDoubleProperty(super.getMaxRouteShare())
    val csimCellSizeProperty = SimpleIntegerProperty(super.getCSIMCellSize())
    val csimThresholdPropery = SimpleDoubleProperty(super.getCSIMThreshold())
    val maxFurnitureDistanceProperty = SimpleDoubleProperty(super.getMaxFurnitureDistance())
    val junctionScoreFactorProperty = SimpleDoubleProperty(super.getJunctionScoreFactor())
    val bendScoreFactorProperty = SimpleDoubleProperty(super.getBendScoreFactor())
    val minTurnAngleProperty = SimpleDoubleProperty(super.getMinTurnAngle())
    val splitForBetterScaleProperty = SimpleBooleanProperty(super.isSplitForBetterScale())
    val mapStyleProperty = SimpleObjectProperty(super.getMapStyle())
    val paperSizeProperty = SimpleObjectProperty(super.getPaperSize())
    val printA3OnA4Property = SimpleBooleanProperty(super.isPrintA3OnA4())
    val minControlSeparationProperty = SimpleDoubleProperty(super.getMinControlSeparation())
    val turnDensityProperty = SimpleDoubleProperty(super.getTurnDensity())
    val avgLegDistanceProperty = SimpleDoubleProperty(super.getAvgLegDistance())

    override fun getBeenHereBeforeWeighting(): Double {
        return beenHereBeforeWeightingProperty.value
    }

    override fun getPaperSize(): PaperSize {
        return paperSizeProperty.value
    }

    override fun getRouteChoiceWeighting(): Double {
        return routeChoiceWeightingProperty.value
    }

    override fun getLegLengthWeighting(): Double {
        return legLengthWeightingProperty.value
    }

    override fun getLegComplexityWeighting(): Double {
        return legComplexityWeightingProperty.value
    }

    override fun getComesTooCloseWeighting(): Double {
        return comesTooCloseWeightingProperty.value
    }

    override fun getDogLegWeighting(): Double {
        return dogLegWeightingProperty.value
    }

    override fun getMaxExecutionTime(): Long {
        return maxExecutionTimeProperty.value
    }

    override fun getMaxGenerations(): Long {
        return maxGenerationsProperty.value
    }

    override fun getStoppingFitness(): Double {
        return stoppingFitnessProperty.value
    }

    override fun getSwapProbability(): Double {
        return controlSwapProbabilityProperty.value
    }

    override fun getMutateProbability(): Double {
        return mutateProbabilityProperty.value
    }

    override fun getMaxFirstControlDistance(): Double {
        return maxFirstControlDistanceProperty.value
    }

    override fun getMaxLastLegLength(): Double {
        return maxLastLegLengthProperty.value
    }

    override fun getMinLegDistance(): Double {
        return minLegDistanceProperty.value
    }

    override fun getAllowedCourseLengthDelta(): Double {
        return allowedCourseLengthDeltaProperty.value
    }

    override fun getMaxMapScale(): Double {
        return maxMapScaleProperty.value
    }

    override fun getMinApproachToFinish(): Double {
        return minApproachToFinishProperty.value
    }

    override fun getOffspringFraction(): Double {
        return offspringFractionProperty.value
    }

    override fun getPopulationSize(): Int {
        return populationSizeProperty.value
    }

    override fun getMutationRadius(): Double {
        return mutationRadiusProperty.value
    }

    override fun getMaxPhenotypeAge(): Long {
        return maxPhenotypeAgeProperty.value
    }

    override fun getMaxRouteShare(): Double {
        return maxRouteShareProperty.value
    }

    override fun getCSIMCellSize(): Int {
        return csimCellSizeProperty.value
    }

    override fun getCSIMThreshold(): Double {
        return csimThresholdPropery.value
    }

    override fun getMaxFurnitureDistance(): Double {
        return maxFurnitureDistanceProperty.value
    }

    override fun getJunctionScoreFactor(): Double {
        return junctionScoreFactorProperty.value
    }

    override fun getBendScoreFactor(): Double {
        return bendScoreFactorProperty.value
    }

    override fun getDistinctControlSiteWeighting(): Double {
        return distinctControlSiteWeightingProperty.value
    }

    override fun getMinTurnAngle(): Double {
        return minTurnAngleProperty.value
    }

    override fun isSplitForBetterScale(): Boolean {
        return splitForBetterScaleProperty.value
    }

    override fun getMapStyle(): MapStyle {
        return mapStyleProperty.value
    }

    override fun isPrintA3OnA4(): Boolean {
        return printA3OnA4Property.value
    }

    override fun getMaxLegDistance(): Double {
        return maxLegDistanceProperty.value
    }

    override fun getMinControlSeparation(): Double {
        return minControlSeparationProperty.value
    }

    override fun getTurnDensity(): Double {
        return turnDensityProperty.value
    }

    override fun getAvgLegDistance(): Double {
        return avgLegDistanceProperty.value
    }
}