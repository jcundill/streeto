package org.streeto.ui

import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize
import tornadofx.*

class PreferencesHandler : Controller() {

    private val STREETO_CONSTRAINTS_MAXFINISHLEN = "streeto.constraints.maxfinishlen"
    private val STREETO_CONSTRAINTS_MAXFIRSTLEN = "streeto.constraints.maxfirstlen"
    private val STREETO_CONSTRAINTS_MINAPPROACHFINISH = "streeto.constraints.minapproachfinish"
    private val STREETO_CONSTRAINTS_MINLEGLEN = "streeto.constraints.minleglen"
    private val STREETO_CONSTRAINTS_MAXLEGLEN = "streeto.constraints.maxleglen"
    private val STREETO_CONSTRAINTS_MAXLENDELTA = "streeto.constraints.maxlendelta"
    private val STREETO_CONSTRAINTS_MUTATION_RADIUS = "streeto.constraints.mutation.radius"
    private val STREETO_CONSTRAINTS_MAXROUTESHARE = "streeto.constraints.maxrouteshare"
    private val STREETO_CONSTRAINTS_MAXFURNITUREDIST = "streeto.constraints.maxfurnituredist"
    private val STREETO_CONSTRAINTS_MINBENDANGLE = "streeto.constraints.minbendangle"
    private val STREETO_CONSTRAINTS_MINSEPARATION = "streeto.constraints.minseparation"
    private val STREETO_CONSTRAINTS_TURNDENSITY = "streeto.constraints.turndensity"

    private val STREETO_PLACEMENT_JUNCTIONFACTOR = "streeto.placement.junctionfactor"
    private val STREETO_PLACEMENT_BENDFACTOR = "streeto.placement.bendfactor"

    private val STREETO_WEIGHTS_ROUTECHOICE = "streeto.weights.routechoice"
    private val STREETO_WEIGHTS_LENGTH = "streeto.weights.length"
    private val STREETO_WEIGHTS_DOGLEG = "streeto.weights.dogleg"
    private val STREETO_WEIGHTS_COMPLEXITY = "streeto.weights.complexity"
    private val STREETO_WEIGHTS_COMINGBACKHERE = "streeto.weights.comingbackhere"
    private val STREETO_WEIGHTS_BEENHEREBEFORE = "streeto.weights.beenherebefore"
    private val STREETO_WEIGHTS_DISTINCTCONTROL = "streeto.weights.distinctcontrol"

    private val STREETO_MAP_MAXSCALE = "streeto.map.maxscale"
    private val STREETO_MAP_ALLOWSPLIT = "streeto.map.allowsplit"
    private val STREETO_MAP_USEA3 = "streeto.map.usea3"
    private val STREETO_MAP_PRINT_A3_ON_A4 = "streeto.map.printa3ona4"
    private val STREETO_MAP_STYLE = "streeto.map.style"

    private val STREETO_JENETICS_SWAPPROBABILITY = "streeto.jenetics.swapprobability"
    private val STREETO_JENETICS_MUTATEPROBABILITY = "streeto.jenetics.mutateprobability"
    private val STREETO_JENETICS_MAXTIME = "streeto.jenetics.maxtime"
    private val STREETO_JENETICS_MAXGENERATIONS = "streeto.jenetics.maxgenerations"
    private val STREETO_JENETICS_MAXFITNESS = "streeto.jenetics.maxfitness"
    private val STREETO_JENETICS_OFFSPRINGFRACTION = "streeto.jenetics.offspringfraction"
    private val STREETO_JENETICS_POPSIZE = "streeto.jenetics.popsize"
    private val STREETO_JENETICS_MAXAGE = "streeto.jenetics.maxage"

    fun loadPreferences(): ObservablePreferences {
        val prefs = ObservablePreferences()
        preferences("StreetO") {
            prefs.stoppingFitnessProperty.value =
                getDouble(STREETO_JENETICS_MAXFITNESS, prefs.stoppingFitness)
            prefs.maxGenerationsProperty.value =
                getLong(STREETO_JENETICS_MAXGENERATIONS, prefs.maxGenerations)
            prefs.maxExecutionTimeProperty.value =
                getLong(STREETO_JENETICS_MAXTIME, prefs.maxExecutionTime)
            prefs.mutateProbabilityProperty.value =
                getDouble(STREETO_JENETICS_MUTATEPROBABILITY, prefs.mutateProbability)
            prefs.controlSwapProbabilityProperty.value =
                getDouble(STREETO_JENETICS_SWAPPROBABILITY, prefs.swapProbability)
            prefs.offspringFractionProperty.value =
                getDouble(STREETO_JENETICS_OFFSPRINGFRACTION, prefs.offspringFraction)
            prefs.populationSizeProperty.value =
                getInt(STREETO_JENETICS_POPSIZE, prefs.populationSize)
            prefs.maxPhenotypeAgeProperty.value =
                getLong(STREETO_JENETICS_MAXAGE, prefs.maxPhenotypeAge)

            prefs.paperSizeProperty.value =
                PaperSize.valueOf(get(STREETO_MAP_USEA3, prefs.paperSize.name))
            prefs.splitForBetterScaleProperty.value =
                getBoolean(STREETO_MAP_ALLOWSPLIT, prefs.isSplitForBetterScale)
            prefs.maxMapScaleProperty.value =
                getDouble(STREETO_MAP_MAXSCALE, prefs.maxMapScale)
            prefs.printA3OnA4Property.value =
                getBoolean(STREETO_MAP_PRINT_A3_ON_A4, prefs.isPrintA3OnA4)
            prefs.mapStyle = MapStyle.valueOf(get(STREETO_MAP_STYLE, prefs.mapStyle.name))

            prefs.beenHereBeforeWeightingProperty.value = getDouble(
                STREETO_WEIGHTS_BEENHEREBEFORE,
                prefs.beenHereBeforeWeighting
            )
            prefs.comesTooCloseWeightingProperty.value = getDouble(
                STREETO_WEIGHTS_COMINGBACKHERE,
                prefs.comesTooCloseWeighting
            )
            prefs.legComplexityWeightingProperty.value = getDouble(
                STREETO_WEIGHTS_COMPLEXITY,
                prefs.legComplexityWeighting
            )
            prefs.dogLegWeightingProperty.value =
                getDouble(STREETO_WEIGHTS_DOGLEG, prefs.dogLegWeighting)
            prefs.legLengthWeightingProperty.value =
                getDouble(STREETO_WEIGHTS_LENGTH, prefs.legLengthWeighting)
            prefs.routeChoiceWeightingProperty.value = getDouble(
                STREETO_WEIGHTS_ROUTECHOICE,
                prefs.routeChoiceWeighting
            )
            prefs.distinctControlSiteWeightingProperty.value = getDouble(
                STREETO_WEIGHTS_DISTINCTCONTROL,
                prefs.distinctControlSiteWeighting
            )

            prefs.minLegDistanceProperty.value =
                getDouble(STREETO_CONSTRAINTS_MINLEGLEN, prefs.minLegDistance)
            prefs.maxLegDistanceProperty.value =
                getDouble(STREETO_CONSTRAINTS_MAXLEGLEN, prefs.maxLegDistance)
            prefs.minApproachToFinishProperty.value =
                getDouble(STREETO_CONSTRAINTS_MINAPPROACHFINISH, prefs.minApproachToFinish)
            prefs.maxFirstControlDistanceProperty.value =
                getDouble(STREETO_CONSTRAINTS_MAXFIRSTLEN, prefs.maxFirstControlDistance)
            prefs.maxLastLegLengthProperty.value =
                getDouble(STREETO_CONSTRAINTS_MAXFINISHLEN, prefs.maxLastLegLength)
            prefs.allowedCourseLengthDeltaProperty.value =
                getDouble(STREETO_CONSTRAINTS_MAXLENDELTA, prefs.allowedCourseLengthDelta)
            prefs.mutationRadiusProperty.value =
                getDouble(STREETO_CONSTRAINTS_MUTATION_RADIUS, prefs.mutationRadius)
            prefs.maxRouteShareProperty.value =
                getDouble(STREETO_CONSTRAINTS_MAXROUTESHARE, prefs.maxRouteShare)
            prefs.maxFurnitureDistanceProperty.value =
                getDouble(STREETO_CONSTRAINTS_MAXFURNITUREDIST, prefs.maxFurnitureDistance)
            prefs.minTurnAngleProperty.value =
                getDouble(STREETO_CONSTRAINTS_MINBENDANGLE, prefs.minTurnAngle)
            prefs.minControlSeparationProperty.value =
                getDouble(STREETO_CONSTRAINTS_MINSEPARATION, prefs.minControlSeparation)
            prefs.turnDensityProperty.value =
                getDouble(STREETO_CONSTRAINTS_TURNDENSITY, prefs.turnDensity)

            prefs.junctionScoreFactorProperty.value =
                getDouble(STREETO_PLACEMENT_JUNCTIONFACTOR, prefs.junctionScoreFactor)
            prefs.bendScoreFactorProperty.value =
                getDouble(STREETO_PLACEMENT_BENDFACTOR, prefs.bendScoreFactor)

        }
        return prefs
    }

    fun flushPreferences(prefs: ObservablePreferences) {
        preferences("StreetO") {
            putDouble(STREETO_JENETICS_MAXFITNESS, prefs.stoppingFitnessProperty.value)
            putLong(STREETO_JENETICS_MAXGENERATIONS, prefs.maxGenerationsProperty.value)
            putLong(STREETO_JENETICS_MAXTIME, prefs.maxExecutionTimeProperty.value)
            putDouble(
                STREETO_JENETICS_MUTATEPROBABILITY,
                prefs.mutateProbabilityProperty.value
            )
            putDouble(
                STREETO_JENETICS_SWAPPROBABILITY,
                prefs.controlSwapProbabilityProperty.value
            )
            putDouble(
                STREETO_JENETICS_OFFSPRINGFRACTION,
                prefs.offspringFractionProperty.value
            )
            putInt(STREETO_JENETICS_POPSIZE, prefs.populationSizeProperty.value)
            putLong(STREETO_JENETICS_MAXAGE, prefs.maxPhenotypeAgeProperty.value)

            put(STREETO_MAP_USEA3, prefs.paperSizeProperty.value.name)
            putBoolean(STREETO_MAP_ALLOWSPLIT, prefs.splitForBetterScaleProperty.value)
            putDouble(STREETO_MAP_MAXSCALE, prefs.maxMapScaleProperty.value)
            putBoolean(STREETO_MAP_PRINT_A3_ON_A4, prefs.printA3OnA4Property.value)
            put(STREETO_MAP_STYLE, prefs.mapStyleProperty.value.name)

            putDouble(
                STREETO_WEIGHTS_BEENHEREBEFORE,
                prefs.beenHereBeforeWeightingProperty.value
            )
            putDouble(
                STREETO_WEIGHTS_COMINGBACKHERE,
                prefs.comesTooCloseWeightingProperty.value
            )
            putDouble(
                STREETO_WEIGHTS_COMPLEXITY,
                prefs.legComplexityWeightingProperty.value
            )
            putDouble(STREETO_WEIGHTS_DOGLEG, prefs.dogLegWeightingProperty.value)
            putDouble(STREETO_WEIGHTS_LENGTH, prefs.legLengthWeightingProperty.value)
            putDouble(
                STREETO_WEIGHTS_ROUTECHOICE,
                prefs.routeChoiceWeightingProperty.value
            )
            putDouble(
                STREETO_WEIGHTS_DISTINCTCONTROL,
                prefs.distinctControlSiteWeightingProperty.value
            )

            putDouble(STREETO_CONSTRAINTS_MINLEGLEN, prefs.minLegDistanceProperty.value)
            putDouble(STREETO_CONSTRAINTS_MAXLEGLEN, prefs.maxLegDistanceProperty.value)
            putDouble(
                STREETO_CONSTRAINTS_MINAPPROACHFINISH,
                prefs.minApproachToFinishProperty.value
            )
            putDouble(
                STREETO_CONSTRAINTS_MAXFIRSTLEN,
                prefs.maxFirstControlDistanceProperty.value
            )
            putDouble(STREETO_CONSTRAINTS_MAXFINISHLEN, prefs.maxLastLegLengthProperty.value)
            putDouble(
                STREETO_CONSTRAINTS_MAXLENDELTA,
                prefs.allowedCourseLengthDeltaProperty.value
            )
            putDouble(STREETO_CONSTRAINTS_MUTATION_RADIUS, prefs.mutationRadiusProperty.value)
            putDouble(STREETO_CONSTRAINTS_MAXROUTESHARE, prefs.maxRouteShareProperty.value)
            putDouble(
                STREETO_CONSTRAINTS_MAXFURNITUREDIST,
                prefs.maxFurnitureDistanceProperty.value
            )
            putDouble(STREETO_CONSTRAINTS_MINBENDANGLE, prefs.minTurnAngleProperty.value)
            putDouble(STREETO_CONSTRAINTS_MINSEPARATION, prefs.minControlSeparationProperty.value)
            putDouble(STREETO_CONSTRAINTS_TURNDENSITY, prefs.turnDensityProperty.value)

            putDouble(STREETO_PLACEMENT_JUNCTIONFACTOR, prefs.junctionScoreFactorProperty.value)
            putDouble(STREETO_PLACEMENT_BENDFACTOR, prefs.bendScoreFactorProperty.value)
        }
    }

}