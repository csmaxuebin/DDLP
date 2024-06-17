/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolife_demo;

import org.javatuples.Pair;
import org.javatuples.Quartet;

/**
 *
 * @author tom
 */
public class SquaredGrid {

    private Quartet<Double, Double, Double, Double> grid; // [min_lat, max_lat, min_lng, max_lng]
    private double grid_size;

    public SquaredGrid(double minLat, double maxLat, double minLng, double maxLng) {
        if (minLat > maxLat || minLng > maxLng) {
            throw new IllegalArgumentException("SquaredGrid: minLat > maxLat || minLng > maxLng");
        }

        double lat_size = maxLat - minLat;
        double lng_size = maxLng - minLng;

        if (lat_size > lng_size) {
            grid = Quartet.with(minLat, maxLat, minLng, maxLng + (lat_size - lng_size));
            grid_size = lat_size;
        } else {
            grid = Quartet.with(minLat, maxLat + (lng_size - lat_size), minLng, maxLng);
            grid_size = lng_size;
        }
    }

    public Pair<Double, Double> getCenter() {
        double lat = (grid.getValue0() + grid.getValue1()) / 2;
        double lng = (grid.getValue2() + grid.getValue3()) / 2;
        return Pair.with(lat, lng);
    }

    public Quartet<Double, Double, Double, Double> getBox() {
        return grid;
    }

    public double getGridSize() {
        return grid_size;
    }

    public boolean contains(Pair<Double, Double> poi) {
        return (poi.getValue0() >= grid.getValue0()) && (poi.getValue0() <= grid.getValue1())
                && (poi.getValue1() >= grid.getValue2()) && (poi.getValue1() <= grid.getValue3());
    }

    @Override
    public String toString() {
        return "SquaredGrid{" + "grid=" + grid + ", center=" + getCenter() + ", dist_in_km=" + getGridSizeInKM() + "}\n";
    }

    public double getGridSizeInKM() {
        return calculateGridSizeInKM(grid.getValue0(), grid.getValue2(), grid.getValue1(), grid.getValue2());
    }

    private double calculateGridSizeInKM(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6373; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

}
