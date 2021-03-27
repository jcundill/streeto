package org.streeto.mapping;

public class MapBox {
    private final double maxWidth;
    private final double maxHeight;
    private final double scale;
    private final boolean landscape;
    private final PaperSize paperSize;

    public MapBox(double maxWidth, double maxHeight, double scale, boolean landscape) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scale = scale;
        this.landscape = landscape;
        this.paperSize = PaperSize.A4;
    }

    public MapBox(double maxWidth, double maxHeight, double scale, boolean landscape, PaperSize paperSize) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scale = scale;
        this.landscape = landscape;
        this.paperSize = paperSize;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public double getScale() {
        return scale;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }
}
