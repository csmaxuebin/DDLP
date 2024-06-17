/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolife_demo;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.javatuples.Pair;
import org.ujmp.core.Matrix;

/**
 *
 * @author tom
 */
public class GeoLifeDataAnalyse {

    private static final String db_file = "jdbc:sqlite:GeoLife.db";

    private static double distanceBetween2POIs(Pair<Double, Double> pa, Pair<Double, Double> pb) {
        double earthRadius = 6371; // kilometers
        double dLat = Math.toRadians(pb.getValue0() - pa.getValue0());
        double dLng = Math.toRadians(pb.getValue1() - pa.getValue1());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(pa.getValue0()))
                * Math.cos(Math.toRadians(pb.getValue0()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = earthRadius * c;
        return Double.parseDouble(String.format("%.3f", result));
    }

    public static void writeUserPOIs(int user_id, int grid_num, double interval_in_mins)
            throws IOException, ClassNotFoundException, SQLException {

        String usr_id = String.format("%1$03d", user_id);
        String tab = "geolife_" + grid_num + "_" + usr_id;

        String clause = "SELECT DATE(timestamp) AS date, COUNT(DATE(timestamp)) AS count FROM " + tab
                + " WHERE grid_id IN (SELECT grid_id FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " AND timestamp IN (SELECT min(timestamp) FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " GROUP BY DATE(timestamp) ORDER BY COUNT(DATE(timestamp)) DESC";

        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        if (rs.next()) {//only one date
            String date = rs.getString("date");
            userPOIsToCSV(user_id, grid_num, interval_in_mins, date);
        }

        rs.close();
        stm.close();
        cnt.close();

        System.out.println("csv/" + grid_num + "/POIs/POIs_" + usr_id + ".csv done!");
    }

    private static void userPOIsToCSV(int user_id, int grid_num, double interval_in_mins, String date)
            throws ClassNotFoundException, IOException, SQLException {

        String usr_id = String.format("%1$03d", user_id);
        CSVWriter writer = new CSVWriter(new FileWriter("html/csv/" + grid_num + "/POIs/POIs_" + usr_id + ".csv"), ',');
        List<String[]> newRows = new ArrayList<>();

        String str_time = String.format("%.4f", 0.0007 * interval_in_mins);
        double time_interval = Double.parseDouble(str_time);

        String str_dist = String.format("%.2f", 0.8 * interval_in_mins);
        double dist_interval = Double.parseDouble(str_dist);

        String[] title = new String[4];
        title[0] = "timestamp";
        title[1] = "grid_id";
        title[2] = "lat";
        title[3] = "lng";
        newRows.add(title);

        String clause = "SELECT * FROM geolife_" + grid_num + "_" + usr_id
                + " WHERE DATE(timestamp) LIKE \"" + date + "%\" ORDER BY timestamp ASC";

        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);

        if (rs.next()) {
            String[] rows = new String[4];
            rows[0] = rs.getString("timestamp");
            rows[1] = rs.getString("grid_id");
            rows[2] = rs.getString("lat_grid");
            rows[3] = rs.getString("lng_grid");
            newRows.add(rows);

            double cur_time = Double.parseDouble(rs.getString("time"));
            Pair<Double, Double> pa = Pair.with(Double.parseDouble(rows[2]), Double.parseDouble(rows[3]));

            while (rs.next()) {
                String[] next_rows = new String[4];
                next_rows[0] = rs.getString("timestamp");
                next_rows[1] = rs.getString("grid_id");
                next_rows[2] = rs.getString("lat_grid");
                next_rows[3] = rs.getString("lng_grid");

                double next_time = Double.parseDouble(rs.getString("time"));
                Pair<Double, Double> pb = Pair.with(Double.parseDouble(next_rows[2]), Double.parseDouble(next_rows[3]));
                double time_val = Double.parseDouble(String.format("%.4f", next_time - cur_time));
                double dist_val = Double.parseDouble(String.format("%.2f", distanceBetween2POIs(pa, pb)));

                if (dist_val > 0 && dist_val <= dist_interval){
               //  if (time_val >= time_interval){
                    newRows.add(next_rows);
                    cur_time = next_time;
                    pa = pb;
                }
            }
        }

        rs.close();
        stm.close();
        cnt.close();

        writer.writeAll(newRows);
        writer.close();
    }

    public static void writeGridsToCSV(int grid_num)
            throws IOException, ClassNotFoundException, SQLException {

        String usr_id = String.format("%1$03d", 0);

        CSVWriter writer = new CSVWriter(new FileWriter("html/csv/" + grid_num + "/Grids.csv"), ',');
        List<String[]> newRows = new ArrayList<>();

        String[] title = new String[7];
        title[0] = "grid_id";
        title[1] = "lat";
        title[2] = "lng";
        title[3] = "lat_min";
        title[4] = "lng_min";
        title[5] = "lat_max";
        title[6] = "lng_max";
        newRows.add(title);

        String clause = "SELECT grid_id, lat, lng, lat_min, lng_min, lat_max, lng_max FROM grids_" + grid_num + "_" + usr_id;
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        while (rs.next()) {
            String[] rows = new String[7];
            rows[0] = rs.getString("grid_id");
            rows[1] = rs.getString("lat");
            rows[2] = rs.getString("lng");
            rows[3] = rs.getString("lat_min");
            rows[4] = rs.getString("lng_min");
            rows[5] = rs.getString("lat_max");
            rows[6] = rs.getString("lng_max");
            newRows.add(rows);
        }

        rs.close();
        stm.close();
        cnt.close();

        writer.writeAll(newRows);
        writer.close();
    }

    private static String findChain(int user_id, int grid_num, double interval_in_mins, String date)
            throws ClassNotFoundException, SQLException {

        String usr_id = String.format("%1$03d", user_id);
        String clause = "SELECT * FROM geolife_" + grid_num + "_" + usr_id
                + " WHERE DATE(timestamp) LIKE \"" + date + "%\" ORDER BY timestamp ASC";

        String str_time = String.format("%.4f", 0.0007 * interval_in_mins);
        double time_interval = Double.parseDouble(str_time);

        String str_dist = String.format("%.2f", 0.8 * interval_in_mins);
        double dist_interval = Double.parseDouble(str_dist);

        String result = "";
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);

        if (rs.next()) {
            double cur_time = Double.parseDouble(rs.getString("time"));
            Pair<Double, Double> pa = Pair.with(Double.parseDouble(rs.getString("lat_grid")), Double.parseDouble(rs.getString("lng_grid")));
            String pos = rs.getString("grid_id") + " ";
            result += pos;
//            System.out.println("Start Point : " + pos + "  " + rs.getString("timestamp"));

            while (rs.next()) {
                double next_time = Double.parseDouble(rs.getString("time"));
                Pair<Double, Double> pb = Pair.with(Double.parseDouble(rs.getString("lat_grid")), Double.parseDouble(rs.getString("lng_grid")));
                double time_val = Double.parseDouble(String.format("%.4f", next_time - cur_time));
                double dist_val = Double.parseDouble(String.format("%.2f", distanceBetween2POIs(pa, pb)));

//                System.out.println("cur = " + cur_time + ", next = " + next_time + ", interval = " + time_val +", dist = " + dist_val);

                if (dist_val > 0 && dist_val <= dist_interval){
              //  if (time_val >= time_interval){
                    pos = rs.getString("grid_id") + " ";
                    result += pos;
                    cur_time = next_time;
                    pa = pb;

//                    System.out.println("dist = " + dist_val);
//                    System.out.println(pos + "  " + rs.getString("timestamp"));
                }

            }
        }

        rs.close();
        stm.close();
        cnt.close();

        return result.trim();
    }

    public static void writeUserMatrix(int user_id, int grid_num, double interval_in_mins)
            throws IOException, ClassNotFoundException, SQLException {

        MarkovModel model = new MarkovModel();

        String usr_id = String.format("%1$03d", user_id);
        String tab = "geolife_" + grid_num + "_" + usr_id;

        String clause = "SELECT DATE(timestamp) AS date, COUNT(DATE(timestamp)) AS count FROM " + tab
                + " WHERE grid_id IN (SELECT grid_id FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " AND timestamp IN (SELECT min(timestamp) FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " GROUP BY DATE(timestamp) ORDER BY COUNT(DATE(timestamp)) DESC";

        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        while (rs.next()) {  //all date
            String date = rs.getString("date");
            String chain = findChain(user_id, grid_num, interval_in_mins, date);
            model.updateTransition(chain);
        }

        rs.close();
        stm.close();
        cnt.close();

        Matrix matrix = model.normalizeMatrix();
//        matrix.showGUI();

        matrix.save("Java/mx/" + grid_num + "/mx_" + usr_id);
        System.out.println("mx/" + grid_num + "/mx_" + usr_id + " done!");
    }

    private static void printUserLongTrackDate(int user_id, int grid_num)
            throws ClassNotFoundException, SQLException, IOException {

        String usr_id = String.format("%1$03d", user_id);
        String tab = "geolife_" + grid_num + "_" + usr_id;

        String clause = "SELECT DATE(timestamp) AS date, COUNT(DATE(timestamp)) AS count FROM " + tab
                + " WHERE grid_id IN (SELECT grid_id FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " AND timestamp IN (SELECT min(timestamp) FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " GROUP BY DATE(timestamp) ORDER BY COUNT(DATE(timestamp)) DESC";

        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        if (rs.next()) {
            String[] rows = new String[2];
            rows[0] = rs.getString("date");
            rows[1] = rs.getString("count");
            System.out.println("(" + grid_num + ", " + usr_id + ") = " + rows[0] + " " + rows[1]);
        }

        rs.close();
        stm.close();
        cnt.close();
    }

    public static void main(String[] args)
            throws ClassNotFoundException, SQLException, IOException {


        // int size = 182;
        for (int i =84; i < 85; i++) { // 182
            GeoLifeDataAnalyse.writeUserPOIs(i, 50, 5);
            GeoLifeDataAnalyse.writeUserMatrix(i, 50, 5);
            //GeoLifeDataAnalyse.writeUserPOIs(i, 50, 5);
            // GeoLifeDataAnalyse.writeUserMatrix(i, 50, 5);
            //GeoLifeDataAnalyse.writeUserPOIs(41, 25, 5);
            //GeoLifeDataAnalyse.writeUserMatrix(i, 100, 5);
        }
    }
//        int user_id = 0;
//        int grid_num = 50;
//        String usr_id = String.format("%1$03d", user_id);
//        Matrix matrix = Matrix.Factory.load("mx/" + grid_num + "/mx_" + usr_id);
//        System.out.println(matrix);
//        matrix.showGUI();

//        GeoLifeDataAnalyse.writeGridsToCSV(25);

//        String str = GeoLifeDataAnalyse.findChain(0, 50, 5, "2009-04-10");
//        String[] seqs = str.split(" ");
//        System.out.println("Total POIs : " + seqs.length);
//        for(String s : seqs) {
//            System.out.println(s);
//        }
//        
//        File file = new File("UserLongTrackDate.txt");
//        FileOutputStream fis = new FileOutputStream(file);
//        PrintStream out = new PrintStream(fis);
//        System.setOut(out);
//        int size = 182;
//        for (int i = 0; i < size; i++) { // 182
//            GeoLifeDataAnalyse.writeUserPOIs(i, 25, 5);
//        }
//        System.out.println("Longest Trajectory Found in Grids 25X25!");
//        for (int i = 0; i < size; i++) { // 182
//            GeoLifeDataAnalyse.printUserLongTrackDate(i, 50);
//        }
//        System.out.println("Longest Trajectory Found Grids 50X50!");
//
//        for (int i = 0; i < size; i++) { // 182
//            GeoLifeDataAnalyse.printUserLongTrackDate(i, 100);
//        }
//        System.out.println("Longest Trajectory Found Grids 100X100!");


}
