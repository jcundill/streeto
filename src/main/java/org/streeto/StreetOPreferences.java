package org.streeto;

import org.streeto.mapping.MapStyle;
import org.streeto.mapping.PaperSize;

public class StreetOPreferences {
    private double routeChoiceWeighting = 1.0;
    private double legLengthWeighting = 1.0;
    private double legComplexityWeighting = 1.0;
    private double beenHereBeforeWeighting = 1.0;
    private double comesTooCloseWeighting = 1.0;
    private double dogLegWeighting = 1.0;
    private double distinctControlSiteWeighting = 1.0;
    private long maxExecutionTime = 120;
    private long maxGenerations = 100;
    private double stoppingFitness = 0.95;
    private double controlSwapProbability = 0.2;
    private double mutateProbability = 0.2;
    private double maxFirstControlDistance = 500.0;
    private double maxLastLegLength = 500.0;
    private double minLegDistance = 50.0;
    private double maxLegDistance = 1500.0;
    private double allowedCourseLengthDelta = 0.1;
    private double maxMapScale = 15000.0;
    private double minApproachToFinish = 150.0;
    private double offspringFraction = 0.6;
    private int populationSize = 50;
    private double mutationRadius = 500.0;
    private long maxPhenotypeAge = 10L;
    private double maxRouteShare = 0.5;
    private double maxFurnitureDistance = 25.0;
    private double junctionScoreFactor = 0.85;
    private double minTurnAngle = 30.0;
    private boolean splitForBetterScale = false;
    private MapStyle mapStyle = MapStyle.STREETO;
    private PaperSize paperSize = PaperSize.A4;
    private boolean printA3OnA4 = false;
    private double minControlSeparation = 5.0;
    private double turnDensity = 50.0;
    private double bendScoreFactor = 0.75;
    private double avgLegDistance = 500.0;
    private int csimCellSize = 25;

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }


    public double getRouteChoiceWeighting() {
        return routeChoiceWeighting;
    }

    public void setRouteChoiceWeighting(double routeChoiceWeighting) {
        this.routeChoiceWeighting = routeChoiceWeighting;
    }

    public double getLegLengthWeighting() {
        return legLengthWeighting;
    }

    public void setLegLengthWeighting(double legLengthWeighting) {
        this.legLengthWeighting = legLengthWeighting;
    }

    public double getLegComplexityWeighting() {
        return legComplexityWeighting;
    }

    public void setLegComplexityWeighting(double legComplexityWeighting) {
        this.legComplexityWeighting = legComplexityWeighting;
    }

    public double getBeenHereBeforeWeighting() {
        return beenHereBeforeWeighting;
    }

    public void setBeenHereBeforeWeighting(double beenHereBeforeWeighting) {
        this.beenHereBeforeWeighting = beenHereBeforeWeighting;
    }

    public double getComesTooCloseWeighting() {
        return comesTooCloseWeighting;
    }

    public void setComesTooCloseWeighting(double comesTooCloseWeighting) {
        this.comesTooCloseWeighting = comesTooCloseWeighting;
    }

    public double getDogLegWeighting() {
        return dogLegWeighting;
    }

    public void setDogLegWeighting(double dogLegWeighting) {
        this.dogLegWeighting = dogLegWeighting;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public long getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(long maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public double getStoppingFitness() {
        return stoppingFitness;
    }

    public void setStoppingFitness(double stoppingFitness) {
        this.stoppingFitness = stoppingFitness;
    }

    public double getSwapProbability() {
        return controlSwapProbability;
    }

    public void setSwapProbability(double controlSwapProbability) {
        this.controlSwapProbability = controlSwapProbability;
    }

    public double getMutateProbability() {
        return mutateProbability;
    }

    public void setMutateProbability(double mutateProbability) {
        this.mutateProbability = mutateProbability;
    }

    public double getMaxFirstControlDistance() {
        return maxFirstControlDistance;
    }

    public void setMaxFirstControlDistance(double maxFirstControlDistance) {
        this.maxFirstControlDistance = maxFirstControlDistance;
    }

    public double getMaxLastLegLength() {
        return maxLastLegLength;
    }

    public void setMaxLastLegLength(double maxLastLegLength) {
        this.maxLastLegLength = maxLastLegLength;
    }

    public double getMinLegDistance() {
        return minLegDistance;
    }

    public void setMinLegDistance(double minLegDistance) {
        this.minLegDistance = minLegDistance;
    }

    public double getAllowedCourseLengthDelta() {
        return allowedCourseLengthDelta;
    }

    public void setAllowedCourseLengthDelta(double allowedCourseLengthDelta) {
        this.allowedCourseLengthDelta = allowedCourseLengthDelta;
    }

    public double getMaxMapScale() {
        return maxMapScale;
    }

    public void setMaxMapScale(double maxMapScale) {
        this.maxMapScale = maxMapScale;
    }

    public double getMinApproachToFinish() {
        return minApproachToFinish;
    }

    public void setMinApproachToFinish(double minApproachToFinish) {
        this.minApproachToFinish = minApproachToFinish;
    }

    public double getOffspringFraction() {
        return offspringFraction;
    }

    public void setOffspringFraction(double offspringFraction) {
        this.offspringFraction = offspringFraction;
    }


    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public double getMutationRadius() {
        return mutationRadius;
    }

    public void setMutationRadius(double mutationRadius) {
        this.mutationRadius = mutationRadius;
    }

    public long getMaxPhenotypeAge() {
        return maxPhenotypeAge;
    }

    public void setMaxPhenotypeAge(long maxPhenotypeAge) {
        this.maxPhenotypeAge = maxPhenotypeAge;
    }

    public double getMaxRouteShare() {
        return maxRouteShare;
    }

    public void setMaxRouteShare(double maxRouteShare) {
        this.maxRouteShare = maxRouteShare;
    }

    public double getMaxFurnitureDistance() {
        return maxFurnitureDistance;
    }

    public void setMaxFurnitureDistance(double maxFurnitureDistance) {
        this.maxFurnitureDistance = maxFurnitureDistance;
    }

    public double getJunctionScoreFactor() {
        return junctionScoreFactor;
    }

    public void setJunctionScoreFactor(double junctionScoreFactor) {
        this.junctionScoreFactor = junctionScoreFactor;
    }

    public double getDistinctControlSiteWeighting() {
        return distinctControlSiteWeighting;
    }

    public void setDistinctControlSiteWeighting(double distinctControlSiteWeighting) {
        this.distinctControlSiteWeighting = distinctControlSiteWeighting;
    }

    public double getMinTurnAngle() {
        return minTurnAngle;
    }

    public void setMinTurnAngle(double minTurnAngle) {
        this.minTurnAngle = minTurnAngle;
    }

    public boolean isSplitForBetterScale() {
        return splitForBetterScale;
    }

    public void setSplitForBetterScale(boolean splitForBetterScale) {
        this.splitForBetterScale = splitForBetterScale;
    }

    public MapStyle getMapStyle() {
        return mapStyle;
    }

    public void setMapStyle(MapStyle mapStyle) {
        this.mapStyle = mapStyle;
    }

    public boolean isPrintA3OnA4() {
        return printA3OnA4;
    }

    public void setPrintA3OnA4(boolean printA3OnA4) {
        this.printA3OnA4 = printA3OnA4;
    }

    public double getMaxLegDistance() {
        return maxLegDistance;
    }

    public void setMaxLegDistance(double distance) {
        this.maxLegDistance = distance;
    }

    public double getMinControlSeparation() {
        return minControlSeparation;
    }

    public void setMinControlSeparation(double minControlSeparation) {
        this.minControlSeparation = minControlSeparation;
    }

    public double getTurnDensity() {
        return turnDensity;
    }

    public void setTurnDensity(double turnDensity) {
        this.turnDensity = turnDensity;
    }

    public double getBendScoreFactor() {
        return bendScoreFactor;
    }

    public void setBendScoreFactor(double bendScoreFactor) {
        this.bendScoreFactor = bendScoreFactor;
    }

    public double getAvgLegDistance() {
        return avgLegDistance;
    }

    public void setAvgLegDistance(double avgLegDistance) {
        this.avgLegDistance = avgLegDistance;
    }

    public int getCSIMCellSize() {
        return csimCellSize;
    }

    public void setCSIMCellSize(int csimCellSize) {
        this.csimCellSize = csimCellSize;
    }
}
