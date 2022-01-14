package org.streeto.csim;

public class SimilarityResult {

    private final double csim;
    private final double cincAB;

    public static final SimilarityResult SAME = new SimilarityResult(1.0, 1.0, 1.0);
    private final double cincBA;

    public SimilarityResult(double csim, double cincAB, double cincBA) {
        this.csim = csim;
        this.cincAB = cincAB;
        this.cincBA = cincBA;
    }

    public double getCsim() {
        return csim;
    }

    public double getCincAB() {
        return cincAB;
    }

    public double getCincBA() {
        return cincBA;
    }

    @Override
    public String toString() {
        return "SimilarityResult [csim=" + csim + ", cincAB=" + cincAB + ", cincBA=" + cincBA + "]";
    }
}
