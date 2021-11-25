package org.streeto.ui

import org.streeto.StreetOPreferences
import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences
import kotlin.reflect.jvm.jvmName


class PreferencesHandler {
    val node = Preferences.userRoot().node("StreetO")

    fun loadPreferences(): ObservablePreferences {
        val prefs = ObservablePreferences()
        if( node != null) {
            prefs.stoppingFitnessProperty.value = node.getDouble(IPreferenceConstants.STREETO_JENETICS_MAXFITNESS, prefs.stoppingFitness)
            prefs.maxGenerationsProperty.value = node.getLong(IPreferenceConstants.STREETO_JENETICS_MAXGENERATIONS, prefs.maxGenerations)
            prefs.maxExecutionTimeProperty.value = node.getLong(IPreferenceConstants.STREETO_JENETICS_MAXTIME, prefs.maxExecutionTime)
            prefs.mutateProbabilityProperty.value = node.getDouble(IPreferenceConstants.STREETO_JENETICS_MUTATEPROBABILITY, prefs.mutateProbability)
            prefs.controlSwapProbabilityProperty.value = node.getDouble(IPreferenceConstants.STREETO_JENETICS_SWAPPROBABILITY, prefs.swapProbability)
            prefs.offspringFractionProperty.value = node.getDouble(IPreferenceConstants.STREETO_JENETICS_OFFSPRINGFRACTION, prefs.offspringFraction)
            prefs.populationSizeProperty.value =node.getInt(IPreferenceConstants.STREETO_JENETICS_POPSIZE, prefs.populationSize)
            prefs.maxPhenotypeAgeProperty.value = node.getLong(IPreferenceConstants.STREETO_JENETICS_MAXAGE, prefs.maxPhenotypeAge)
            prefs.paperSizeProperty.value = PaperSize.valueOf(node.get(IPreferenceConstants.STREETO_MAP_USEA3, prefs.paperSize.name) )
            prefs.splitForBetterScaleProperty.value =node.getBoolean(IPreferenceConstants.STREETO_MAP_ALLOWSPLIT, prefs.isSplitForBetterScale)
            prefs.maxMapScaleProperty.value = node.getDouble(IPreferenceConstants.STREETO_MAP_MAXSCALE, prefs.maxMapScale)
            prefs.printA3OnA4Property.value = node.getBoolean(IPreferenceConstants.STREETO_MAP_PRINT_A3_ON_A4, prefs.isPrintA3OnA4)
            prefs.mapStyle  = MapStyle.valueOf(node.get(IPreferenceConstants.STREETO_MAP_STYLE, prefs.mapStyle.value))
            prefs.beenHereBeforeWeightingProperty.value = node.getDouble(
                IPreferenceConstants.STREETO_WEIGHTS_BEENHEREBEFORE,
                prefs.beenHereBeforeWeighting
            )
            prefs.comesTooCloseWeightingProperty.value = node.getDouble(
                IPreferenceConstants.STREETO_WEIGHTS_COMINGBACKHERE,
                prefs.comesTooCloseWeighting
            )
            prefs.legComplexityWeightingProperty.value = node.getDouble(
                IPreferenceConstants.STREETO_WEIGHTS_COMPLEXITY,
                prefs.legComplexityWeighting
            )
            prefs.dogLegWeightingProperty.value = node.getDouble(IPreferenceConstants.STREETO_WEIGHTS_DOGLEG, prefs.dogLegWeighting)
            prefs.legLengthWeightingProperty.value = node.getDouble(IPreferenceConstants.STREETO_WEIGHTS_LENGTH, prefs.legLengthWeighting)
            prefs.routeChoiceWeightingProperty.value = node.getDouble(
                IPreferenceConstants.STREETO_WEIGHTS_ROUTECHOICE,
                prefs.routeChoiceWeighting
            )
            prefs.distinctControlSiteWeightingProperty.value = node.getDouble(
                IPreferenceConstants.STREETO_WEIGHTS_DISTINCTCONTROL,
                prefs.distinctControlSiteWeighting
            )
            prefs.minLegDistanceProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MINLEGLEN, prefs.minLegDistance)
            prefs.maxLegDistanceProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXLEGLEN, prefs.maxLegDistance)
            prefs.minApproachToFinishProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MINAPPROACHFINISH, prefs.minApproachToFinish)
            prefs.maxFirstControlDistanceProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXFIRSTLEN, prefs.maxFirstControlDistance)
            prefs.maxLastLegLengthProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXFINISHLEN, prefs.maxLastLegLength)
            prefs.allowedCourseLengthDeltaProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXLENDELTA, prefs.allowedCourseLengthDelta)
            prefs.mutationRadiusProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MUTATION_RADIUS, prefs.mutationRadius)
            prefs.maxRouteShareProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXROUTESHARE, prefs.maxRouteShare)
            prefs.maxFurnitureDistanceProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXFURNITUREDIST, prefs.maxFurnitureDistance)
            prefs.minTurnAngleProperty.value = node.getDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MINBENDANGLE, prefs.minTurnAngle)

        }
        return prefs
    }
    fun flushPreferences(prefs: ObservablePreferences) {
            if (node != null) {
                node.putDouble(IPreferenceConstants.STREETO_JENETICS_MAXFITNESS, prefs.stoppingFitnessProperty.value)
                node.putLong(IPreferenceConstants.STREETO_JENETICS_MAXGENERATIONS, prefs.maxGenerationsProperty.value)
                node.putLong(IPreferenceConstants.STREETO_JENETICS_MAXTIME, prefs.maxExecutionTimeProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_JENETICS_MUTATEPROBABILITY, prefs.mutateProbabilityProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_JENETICS_SWAPPROBABILITY, prefs.controlSwapProbabilityProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_JENETICS_OFFSPRINGFRACTION, prefs.offspringFractionProperty.value)
                node.putInt(IPreferenceConstants.STREETO_JENETICS_POPSIZE, prefs.populationSizeProperty.value)
                node.putLong(IPreferenceConstants.STREETO_JENETICS_MAXAGE, prefs.maxPhenotypeAgeProperty.value)
                node.put(IPreferenceConstants.STREETO_MAP_USEA3, prefs.paperSizeProperty.value.name)
                node.putBoolean(IPreferenceConstants.STREETO_MAP_ALLOWSPLIT, prefs.splitForBetterScaleProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_MAP_MAXSCALE, prefs.maxMapScaleProperty.value)
                node.putBoolean(IPreferenceConstants.STREETO_MAP_PRINT_A3_ON_A4, prefs.printA3OnA4Property.value)
                node.put(IPreferenceConstants.STREETO_MAP_STYLE, prefs.mapStyleProperty.value.name)
                node.putDouble(
                    IPreferenceConstants.STREETO_WEIGHTS_BEENHEREBEFORE,
                    prefs.beenHereBeforeWeightingProperty.value
                )
                node.putDouble(
                    IPreferenceConstants.STREETO_WEIGHTS_COMINGBACKHERE,
                    prefs.comesTooCloseWeightingProperty.value
                )
                node.putDouble(
                    IPreferenceConstants.STREETO_WEIGHTS_COMPLEXITY,
                    prefs.legComplexityWeightingProperty.value
                )
                node.putDouble(IPreferenceConstants.STREETO_WEIGHTS_DOGLEG, prefs.dogLegWeightingProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_WEIGHTS_LENGTH, prefs.legLengthWeightingProperty.value)
                node.putDouble(
                    IPreferenceConstants.STREETO_WEIGHTS_ROUTECHOICE,
                    prefs.routeChoiceWeightingProperty.value
                )
                node.putDouble(
                    IPreferenceConstants.STREETO_WEIGHTS_DISTINCTCONTROL,
                    prefs.distinctControlSiteWeightingProperty.value
                )
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MINLEGLEN, prefs.minLegDistanceProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MINAPPROACHFINISH, prefs.minApproachToFinishProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXFIRSTLEN, prefs.maxFirstControlDistanceProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXFINISHLEN, prefs.maxLastLegLengthProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXLENDELTA, prefs.allowedCourseLengthDeltaProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MUTATION_RADIUS, prefs.mutationRadiusProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXROUTESHARE, prefs.maxRouteShareProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MAXFURNITUREDIST, prefs.maxFurnitureDistanceProperty.value)
                node.putDouble(IPreferenceConstants.STREETO_CONSTRAINTS_MINBENDANGLE, prefs.minTurnAngleProperty.value)
                try {
                    node.flush()
                } catch (e: BackingStoreException) {
                    println(e)
                }
            }
        }

}