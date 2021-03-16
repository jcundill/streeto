package org.streeto;

public class StreetOPreferences {
    private double routeChoiceWeighting = 1.0;
    private double legLengthWeighting = 1.0;
    private double legComplexityWeighting = 1.0;
    private double beenHereBeforeWeighting = 1.0;
    private double comesTooCloseWeighting = 1.0;
    private double dogLegWeighting = 1.0;
    private long maxExecutionTime = 120;
    private long maxGenerations = 100;
    private double stoppingFitness = 0.95;
    private double controlSwapProbability = 0.2;
    private double mutateProbability = 0.2;
    private double maxFirstControlDistance = 500.0;
    private double maxLastLegLength = 500.0;
    private double minLegDistance = 50.0;
    private double allowedCourseLengthDelta = 0.1;
    private double maxMapScale = 15000.0;
    private double minApproachToFinish = 150.0;
    private double offspringFraction = 0.6;
    private int populationSize = 50;
    private double mutationRadius = 500.0;
    private long maxPhenotypeAge = 10L;


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

    public void setControlSwapProbability(double controlSwapProbability) {
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

    public double getMinLegLength() {
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
}
