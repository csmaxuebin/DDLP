/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolife_demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.javatuples.Pair;
import org.javatuples.Septet;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author tom
 */
public class GeoLifeDataSet {

    private static final String file_path = "./Geolife Trajectories 1.3/Data/";
    private static final String db_file = "jdbc:sqlite:GeoLife.db";

    public static void createTable(int user_id, int grid_num)
            throws FileNotFoundException, ClassNotFoundException, SQLException {

        String usr_id = String.format("%1$03d", user_id);

        Class.forName("org.sqlite.JDBC");
        SQLiteConfig config = new SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.MEMORY);
        config.setTempStore(SQLiteConfig.TempStore.MEMORY);
        config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        Connection connect = DriverManager.getConnection(db_file, config.toProperties());

        connect.createStatement().execute("DROP TABLE IF EXISTS grids_" + grid_num + "_" + usr_id + ";");
        String sql = "CREATE TABLE grids_" + grid_num + "_" + usr_id
                + " (grid_id INTEGER, "
                + "counter INTEGER, "
                + "lat TEXT(255,0), "
                + "lng TEXT(255,0), "
                + "lat_min TEXT(255,0), "
                + "lng_min TEXT(255,0), "
                + "lat_max TEXT(255,0), "
                + "lng_max TEXT(255,0));";
        connect.createStatement().execute(sql);
        PreparedStatement ps = connect.prepareStatement("INSERT INTO grids_" + grid_num + "_" + usr_id + " VALUES (?,?,?,?,?,?,?,?);");

        ConcurrentMap<Integer, AtomicInteger> counter_map = new ConcurrentHashMap<>();
        MapGrids map_grids = new MapGrids(39.75001, 40.10001, 116.15001, 116.60001, grid_num);

        for (int i = 0; i < map_grids.getNumofGrids(); i++) {
            counter_map.putIfAbsent(i, new AtomicInteger(0));
        }

        File folder = new File(file_path + usr_id + "/Trajectory/");
        File[] files = folder.listFiles();

        for (int j = 0; j < files.length; j++) {
            if (files[j].isFile()) {
                String file_name = file_path + usr_id + "/Trajectory/" + files[j].getName();
                Scanner scan = new Scanner(new File(file_name));

                for (int k = 0; k < 6; k++) {
                    scan.nextLine();
                }

                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    String[] splited = line.split(",");
                    Septet it = Septet.fromArray(splited);

                    double lat = Double.parseDouble(it.getValue0().toString());
                    double lng = Double.parseDouble(it.getValue1().toString());

                    if (lat > 39.75001 && lat < 40.10001 && lng > 116.15001 && lng < 116.60001) {
                        int key = map_grids.getGridIndexContainsPOI(Pair.with(lat, lng));
                        counter_map.get(key).incrementAndGet();
                    }
                } // while
            } //if
        } // for j

        System.out.println("grids_" + grid_num + "_" + usr_id + " OK");

        for (Integer k : counter_map.keySet()) {
            ps.setInt(1, k); // grid_id
            ps.setInt(2, counter_map.get(k).get()); // counter
            SquaredGrid g = map_grids.getSquaredGrid(k);
            ps.setString(3, g.getCenter().getValue0().toString()); // lat
            ps.setString(4, g.getCenter().getValue1().toString()); // lng
            ps.setString(5, g.getBox().getValue0().toString()); // lat_min
            ps.setString(6, g.getBox().getValue2().toString()); // lng_min
            ps.setString(7, g.getBox().getValue1().toString()); // lat_max
            ps.setString(8, g.getBox().getValue3().toString()); // lng_max
            ps.addBatch();
        }
        connect.setAutoCommit(false);
        ps.executeBatch();
        connect.setAutoCommit(true);

        connect.createStatement().execute("DROP TABLE IF EXISTS geolife_" + grid_num + "_" + usr_id + ";");
        sql = "CREATE TABLE geolife_" + grid_num + "_" + usr_id
                + " (usr_id INTEGER, "
                + "grid_id INTEGER, "
                + "lat TEXT(255,0), "
                + "lng TEXT(255,0), "
                + "lat_grid TEXT(255,0), "
                + "lng_grid TEXT(255,0), "
                + "time TEXT(255,0), "
                + "timestamp TEXT(255,0));";
        connect.createStatement().execute(sql);
        ps = connect.prepareStatement("INSERT INTO geolife_" + grid_num + "_" + usr_id + " VALUES (?,?,?,?,?,?,?,?);");

        for (int j = 0; j < files.length; j++) {
            if (files[j].isFile()) {
                String file_name = file_path + usr_id + "/Trajectory/" + files[j].getName();
                Scanner scan = new Scanner(new File(file_name));

                for (int k = 0; k < 6; k++) {
                    scan.nextLine();
                }

                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    String[] splited = line.split(",");
                    Septet it = Septet.fromArray(splited);

                    double lat = Double.parseDouble(it.getValue0().toString());
                    double lng = Double.parseDouble(it.getValue1().toString());

                    if (lat > 39.75001 && lat < 40.10001 && lng > 116.15001 && lng < 116.60001) {
                        int grid_id = map_grids.getGridIndexContainsPOI(Pair.with(lat, lng));
                        SquaredGrid g = map_grids.getSquaredGrid(grid_id);
                        ps.setInt(1, user_id); // usr_id
                        ps.setInt(2, grid_id); // grid_id
                        ps.setString(3, it.getValue0().toString()); // lat
                        ps.setString(4, it.getValue1().toString()); // lng
                        ps.setString(5, g.getCenter().getValue0().toString()); // lat_grid
                        ps.setString(6, g.getCenter().getValue1().toString()); // lng_grid
                        ps.setString(7, it.getValue4().toString()); // time that have passed since 12/30/1899.
                        ps.setString(8, it.getValue5().toString() + " " + it.getValue6().toString()); // timestamp
                        ps.addBatch();
                    }
                } // while
            } //if
        } // for j
        connect.setAutoCommit(false);
        ps.executeBatch();
        connect.setAutoCommit(true);
        System.out.println("geolife_" + grid_num + "_" + usr_id + " OK");

        connect.close();
    }

    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, SQLException {

        int size = 182;
        for (int i = 0; i < size; i++) { // 182
            GeoLifeDataSet.createTable(i, 25);
        }
        System.out.println("GeoLifeDataSet Loaded Grids 25X25!");

        for (int i = 0; i < size; i++) { // 182
            GeoLifeDataSet.createTable(i, 50);
        }
        System.out.println("GeoLifeDataSet Loaded Grids 50X50!");

        for (int i = 0; i < size; i++) { // 182
            GeoLifeDataSet.createTable(i, 100);
        }
        System.out.println("GeoLifeDataSet Loaded Grids 100X100!");

    }

}
