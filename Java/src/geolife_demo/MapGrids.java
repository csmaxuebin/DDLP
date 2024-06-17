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
public class MapGrids {

    private SquaredGrid[][] grids;
    private Quartet<Double, Double, Double, Double> bound_box; // [min_lat, max_lat, min_lng, max_lng]
    private double grid_size;

    public MapGrids(double minLat, double maxLat, double minLng, double maxLng, int gridsInRow) {
        if (minLat > maxLat || minLng > maxLng) {
            throw new IllegalArgumentException("MapGrids: minLat > maxLat || minLng > maxLng");
        }

//        if (gridsInRow > 50) {
//            throw new IllegalArgumentException("MapGrids: no more than 2500 grids");
//        }

        double lat_size = maxLat - minLat;
        double lng_size = maxLng - minLng;

        grids = new SquaredGrid[gridsInRow][gridsInRow];

        if (lat_size > lng_size) {
            bound_box = Quartet.with(minLat, maxLat, minLng, maxLng + (lat_size - lng_size));
            grid_size = lat_size / gridsInRow;
        } else {
            bound_box = Quartet.with(minLat, maxLat + (lng_size - lat_size), minLng, maxLng);
            grid_size = lng_size / gridsInRow;
        }

        for (int x = 0; x < gridsInRow; x++) {
            double xMin = minLat + ((gridsInRow - x - 1) * grid_size);
            double xMax = xMin + grid_size;
            for (int y = 0; y < gridsInRow; y++) {
                double yMin = minLng + (y * grid_size);
                double yMax = yMin + grid_size;
                grids[x][y] = new SquaredGrid(xMin, xMax, yMin, yMax);
            }
        }
    }

    public int getNumofGrids() {
        return grids[0].length * grids[0].length;
    }

    private double getGridSize() {
        return grid_size;
    }

    public int getGridIndexContainsPOI(Pair<Double, Double> poi) {
        int size = grids[0].length;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (grids[x][y].contains(poi)) {
                    return x * size + y;
                }
            }
        }
        return -1;
    }

    public SquaredGrid getSquaredGrid(int index) {
        int size = grids[0].length;
        if (index < 0 || index > size * size) {
            throw new IllegalArgumentException("getSquaredGrid: index out of bound");
        }

        int x = index / size;
        int y = index % size;
        return grids[x][y];
    }

    @Override
    public String toString() {
        String str = "\n";
        int size = grids[0].length;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                str += grids[x][y].toString();
            }
        }
        return "MapGrids{bound_box=" + bound_box + ", grid_size=" + grid_size + '}' + str;
    }

    public static void main(String[] args) {
/*     
	50*50 -> 1.00km
        45*45 -> 1.11km
        40*40 -> 1.25km
	35*35 -> 1.43km
        30*30 -> 1.66km
        25*25 -> 2.00km
        20*20 -> 2.50km        
*/

        MapGrids map = new MapGrids(39.75, 40.10, 116.15, 116.60, 50);
        System.out.println(map);
    }

}
