package org.streeto.csim;

import java.util.Objects;


public class Cell {
    public int frequency;
    public int X, Y;
    public boolean fake;
    public String zone;
    public double latitude = -1000, longitude = -1000; //illegal values

    Cell(int X, int Y, boolean fake, String code) {
        frequency = 1;
        this.X = X;
        this.Y = Y;
        this.fake = fake;
        this.zone = code;
    }

    public void increaseFrequency() {
        frequency++;
    }

    public String toString() {
        //getLocation();
        return "{\"code\":" + code() + "," + "\"X\":" + X + "," + "\"Y\":" + Y + ","
                + "\"frequency\":" + frequency + "," + "\"fake\":" + fake + ",\"latitude\":" + latitude + ",\"longitude\":" + longitude + "}";
        //return code()+" ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (X != cell.X) return false;
        if (Y != cell.Y) return false;
        return Objects.equals(zone, cell.zone);
    }

    @Override
    public int hashCode() {
        int result = X;
        result = 31 * result + Y;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        return result;
    }

    public String code() {
        return zone + "-" + X + "-" + Y;
    }
}
