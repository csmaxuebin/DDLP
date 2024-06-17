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
public class TrajectoryAnalyse {

    private String db;
    private String tab;
    private int[] users;
    private String[] times;
    private int grids_in_row;
    private int interval_in_seconds;

    public TrajectoryAnalyse(String db_name, String tab_name, int[] usrs, String[] timestamps, int grid_num, int seconds) {
        db = db_name;
        tab = tab_name;
        users = usrs;
        times = timestamps;
        grids_in_row = grid_num;
        interval_in_seconds = seconds;
    }

    public void writeGridsToCSV() throws IOException, ClassNotFoundException, SQLException {
        CSVWriter writer = new CSVWriter(new FileWriter("csv/"+ grids_in_row +"/Grids.csv"), ',');
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

        String clause = "SELECT grid_id, lat, lng, lat_min, lng_min, lat_max, lng_max FROM grids_bj_" + grids_in_row;
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection("jdbc:sqlite:" + db);
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

    private void writeUserPOIsToCSV(String csv, String uid, String tsp)
            throws ClassNotFoundException, IOException, SQLException {

        CSVWriter writer = new CSVWriter(new FileWriter("csv/" + grids_in_row + "/" + csv), ',');
        List<String[]> newRows = new ArrayList<>();

        String[] title = new String[4];
        title[0] = "timestamp";
        title[1] = "grid_id";
        title[2] = "lat";
        title[3] = "lng";
        newRows.add(title);

        String interval = "datetime((strftime('%s', timestamp) / " + interval_in_seconds + ") * " + interval_in_seconds + ", 'unixepoch')";
        String clause = "SELECT * FROM (SELECT " + interval + " AS time, * FROM " + tab 
                + " WHERE usr_id=" + uid + " AND date(timestamp)=\"" + tsp 
                + "\" GROUP BY time, grid_id ORDER BY timestamp ASC) GROUP BY time";

        System.out.println(clause);
        
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection("jdbc:sqlite:" + db);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        while (rs.next()) {
            String[] rows = new String[4];
            rows[0] = rs.getString("time");
            rows[1] = rs.getString("grid_id");
            rows[2] = rs.getString("lat_grid");
            rows[3] = rs.getString("lng_grid");
            newRows.add(rows);
        }

        rs.close();
        stm.close();
        cnt.close();

        writer.writeAll(newRows);
        writer.close();
    }

    public void writeAllUsersPOIsToCSV()
            throws ClassNotFoundException, SQLException, IOException {
        
        for (int i=0; i<users.length; i++) {
            writeUserPOIsToCSV("POIs_" + users[i] + ".csv", "" + users[i], times[i]);
        }
    }

    private void writeUserTrajectoryToCSV(String csv, String uid, String tsp)
            throws ClassNotFoundException, SQLException, IOException {

        CSVWriter writer = new CSVWriter(new FileWriter("csv/"+grids_in_row+"/" + csv), ',');
        List<String[]> newRows = new ArrayList<>();

        String[] title = new String[3];
        title[0] = "timestamp";
        title[1] = "lat";
        title[2] = "lng";
        newRows.add(title);

        String interval = "datetime((strftime('%s', timestamp) / " + interval_in_seconds + ") * " + interval_in_seconds + ", 'unixepoch')";
        String clause = "SELECT time, lat, lng FROM "
                + "(SELECT *, " + interval + " AS time FROM "
                + tab + " WHERE usr_id=" + uid + "GROUP BY time)"
                + " WHERE date(timestamp)=\"" + tsp + "\"";
        
//        String clause = "SELECT timestamp, lat, lng FROM " + tab +
//                                " WHERE usr_id=" + uid + " AND date(timestamp)=\"" + tsp +"\""+
//                                        " AND grid_id IN (SELECT grid_id FROM "+ tab +
//                                                " WHERE usr_id=" + uid +
//                                                        " GROUP BY grid_id HAVING COUNT(grid_id)>1)" + 
//                                        " AND timestamp IN (SELECT min(timestamp) FROM "  + tab +
//                                                " WHERE usr_id=" + uid +
//                                                        " GROUP BY grid_id HAVING COUNT(grid_id)>1)" + 
//                                        " ORDER BY timestamp ASC";
        
        System.out.println(clause);
        
//        String clause = "SELECT timestamp, lat, lng FROM " + tab + " WHERE usr_id=" + uid + " ORDER BY timestamp ASC";
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection("jdbc:sqlite:" + db);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        while (rs.next()) {
            String[] rows = new String[3];
            rows[0] = rs.getString("time");
            rows[1] = rs.getString("lat");
            rows[2] = rs.getString("lng");
            newRows.add(rows);
        }

        rs.close();
        stm.close();
        cnt.close();

        writer.writeAll(newRows);
        writer.close();
    }

    public void writeAllUsersTrajectoriesToCSV()
            throws ClassNotFoundException, SQLException, IOException {
        
        for (int i=0; i<users.length; i++) {
            writeUserTrajectoryToCSV("Trajectory" + users[i] + ".csv", "" + users[i], times[i]);
        }
    }

    private String predictNextState(Matrix mx, String now) {
        int row = (int) mx.getRowForLabel(now);
//        System.out.println(mx.getRowCount() + "x" + mx.getColumnCount() + " now = " + now + " row = " + row);

        if (row < 0) {
            return "";
        } else if (row < mx.getRowCount()) {
            int col = 0;
            double[][] m = mx.toDoubleArray();
            double max = 0;
            for (int c = 0; c < mx.getColumnCount(); c++) {
                if (m[row][c] > max) {
                    max = m[row][c];
                    col = c;
                }
            }
            return mx.getColumnLabel(col);
        } else {
            return "";
        }

    }

    /* The accuracy is the ratio between the number of 
     * correct predictions over the total number of predictions */
    private double calculateAccuracy(Matrix mx, String seq) {
        if (seq.isEmpty()) {
            return 1;
        }

        String[] seqs = seq.split(" ");
        List<Pair<String, String>> transition = new ArrayList<>();
        for (int a = 0, b = 1; a < seqs.length - 1 && b < seqs.length; a++, b++) {
            Pair<String, String> item = Pair.with(seqs[a], seqs[b]);
            transition.add(item);
        }

        int total = seqs.length - 1;
        int correct = 0;

        for (Pair<String, String> p : transition) {
            String next = predictNextState(mx, p.getValue0());
            if (p.getValue1().equalsIgnoreCase(next)) {
                correct++;
            }
        }

//        System.out.println("correct = " + correct + " total = " + total + " Accuracy = " + (1.0 * correct) / total);
        return (1.0 * correct) / total;
    }

    /* Mehtods for geolife dataset */
    private String findChain(String clause)
            throws ClassNotFoundException, SQLException {
        String result = "";

        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection("jdbc:sqlite:" + db);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        while (rs.next()) {
            String pos = rs.getString("grid_id") + " ";
            result += pos;
        }

        rs.close();
        stm.close();
        cnt.close();

        return result.trim();
    }

    /* conducted to evaluate the accuracy of our prediction algorithm */
    private void evaluateTemporalCorrelation()
            throws ClassNotFoundException, SQLException, IOException {

        double total_accuracy = 0;
        String clause = "SELECT grid_id FROM " + tab + " WHERE usr_id=";

        for (int id : users) {
            String chain = findChain(clause + id);
            Matrix matrix = Matrix.Factory.load("mx/"+grids_in_row+"/mx_" + id);

            // conducted to evaluate the accuracy of our prediction algorithm
            double accuracy = calculateAccuracy(matrix, chain);
            total_accuracy += accuracy;
            System.out.println("UID:" + id + " DistinctPOI: " + matrix.getColumnCount()
                    + " ChainLen = " + chain.split(" ").length
                    + " Accuracy = " + String.format("%.3f", accuracy));
        } // for i

        System.out.println("Average accuracy = " + String.format("%.3f", total_accuracy / users.length));
    }

    public void writeAllUsersMatrices()
            throws IOException, ClassNotFoundException, SQLException {

//        String clause = "SELECT grid_id FROM " + tab + " WHERE usr_id=";
        String interval = "datetime((strftime('%s', timestamp) / " + interval_in_seconds + ") * " + interval_in_seconds + ", 'unixepoch')";        
        for (int id : users) {
        String clause = "SELECT * FROM (SELECT " + interval + " AS time, * FROM " + tab 
                + " WHERE usr_id=" + id + " GROUP BY time, grid_id ORDER BY timestamp ASC) GROUP BY time";
        
            System.out.println(clause);
            String chain = findChain(clause);
            Matrix matrix = new MarkovChain(chain).getMatrix();
//            matrix.showGUI();
            matrix.save("mx/"+grids_in_row+"/mx_" + id);
            System.out.println(id + " done!");
        }
    }

    private void writeUserLongTrackTimestampToCSV(String csv, String uid) 
            throws ClassNotFoundException, SQLException, IOException{
        
        CSVWriter writer = new CSVWriter(new FileWriter("csv/" + grids_in_row + "/" + csv), ',');
        List<String[]> newRows = new ArrayList<>();

        String[] title = new String[4];
        title[0] = "timestamp";
        title[1] = "count";
        newRows.add(title);
               
        String clause = "SELECT timestamp, COUNT(date(timestamp)) AS count FROM " + tab +
                                " WHERE usr_id=" + uid +
                                        " AND grid_id IN (SELECT grid_id FROM "+ tab +
                                                " WHERE usr_id=" + uid +
                                                        " GROUP BY grid_id HAVING COUNT(grid_id)>1)" + 
                                        " AND timestamp IN (SELECT min(timestamp) FROM "  + tab +
                                                " WHERE usr_id=" + uid +
                                                        " GROUP BY grid_id HAVING COUNT(grid_id)>1)" + 
                                        " GROUP BY date(timestamp) ORDER BY COUNT(date(timestamp)) DESC";
               
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection("jdbc:sqlite:" + db);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        while (rs.next()) {
            String[] rows = new String[2];
            rows[0] = rs.getString("timestamp");
            rows[1] = rs.getString("count");
            newRows.add(rows);
        }

        rs.close();
        stm.close();
        cnt.close();
        
        writer.writeAll(newRows);
        writer.close();
    }
    
    public void writeAllUsersLongTrackTimestampToCSV()
            throws ClassNotFoundException, SQLException, IOException {
        for (int id : users) {
            writeUserLongTrackTimestampToCSV("Timestamp" + id + ".csv", "" + id);
        }
    }
    
    
}
