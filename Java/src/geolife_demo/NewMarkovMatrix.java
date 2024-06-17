package geolife_demo;

import com.opencsv.CSVWriter;

import java.io.*;
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

public class NewMarkovMatrix {
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



    public static void writeUserMatrix(int user_id, int grid_num, double interval_in_mins)
            throws IOException, ClassNotFoundException, SQLException {

        MarkovModel model = new MarkovModel();

        String usr_id = String.format("%1$03d", user_id);
        String tab = "geolife_" + grid_num + "_" + usr_id;

        String clause = "SELECT DATE(timestamp) AS date, COUNT(DATE(timestamp)) AS count FROM " + tab
                + " WHERE grid_id IN (SELECT grid_id FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " AND timestamp IN (SELECT min(timestamp) FROM " + tab + " GROUP BY grid_id HAVING COUNT(grid_id)>1)"
                + " GROUP BY DATE(timestamp) ORDER BY COUNT(DATE(timestamp)) DESC";
//
        String clause1 = "SELECT DISTINCT(DATE(timestamp)) as date FROM "+tab;

        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause1);
        while (rs.next()) {
            String date = rs.getString("date");
            System.out.println(date);
            String chain = findChain2(user_id, grid_num, date);
            model.updateTransition(chain);
        }

        rs.close();
        stm.close();
        cnt.close();

        Matrix matrix = model.normalizeMatrix();
        System.out.println();
//        matrix.showGUI();

        matrix.save("Java/newMx/" + grid_num + "/mx_" + usr_id);
        System.out.println("mx/" + grid_num + "/mx_" + usr_id + " done!");
    }

    private static String findChain2(int user_id, int grid_num, String date) throws IOException, SQLException, ClassNotFoundException {
        String usr_id = String.format("%1$03d", user_id);

        //SELECT strftime('%H',timestamp) as time1
        //FROM
        //geolife_50_078
        //WHERE time1 > '04'
        //AND DATE(timestamp) LIKE '2008-05-30%'
        String clause = "SELECT *,strftime('%H',timestamp) as time1 FROM geolife_" + grid_num + "_" + usr_id
                + " WHERE time1 > '04' AND DATE(timestamp) LIKE \"" + date + "%\" ORDER BY timestamp ASC";


        CSVWriter writer = new CSVWriter(new FileWriter("Java/pois/splite/" + grid_num+"/"  + usr_id+"_"+date+ ".csv"), ',');
        List<String[]> newRows = new ArrayList<>();
        String[] title = new String[4];
        title[0] = "timestamp";
        title[1] = "grid_id";
        title[2] = "lat";
        title[3] = "lng";
        newRows.add(title);

        String str_dist = String.format("%.2f", 0.8 * 5);
        double dist_interval = Double.parseDouble(str_dist);

        String result = "";
        Class.forName("org.sqlite.JDBC");
        Connection cnt = DriverManager.getConnection(db_file);
        cnt.setAutoCommit(false);
        Statement stm = cnt.createStatement();
        ResultSet rs = stm.executeQuery(clause);
        int[] vector1=new int[2];
        int[] vector2=new int[2];
        String preprePos="";
        if (rs.next()){
            String pos = rs.getString("grid_id") + " ";
            preprePos = pos;
            result += pos;
            String[] rows = new String[4];
            rows[0] = rs.getString("timestamp");
            rows[1] = rs.getString("grid_id");
            rows[2] = rs.getString("lat_grid");
            rows[3] = rs.getString("lng_grid");
            newRows.add(rows);
        }
        boolean flag = false;
        if (rs.next()) {
            double cur_time = Double.parseDouble(rs.getString("time"));
            Pair<Double, Double> pa = Pair.with(Double.parseDouble(rs.getString("lat_grid")), Double.parseDouble(rs.getString("lng_grid")));
            String pos = rs.getString("grid_id") + " ";
            result += pos;
            String prePos = pos;
            String[] rows = new String[4];
            rows[0] = rs.getString("timestamp");
            rows[1] = rs.getString("grid_id");
            rows[2] = rs.getString("lat_grid");
            rows[3] = rs.getString("lng_grid");
            newRows.add(rows);

            while (rs.next()) {

                double next_time = Double.parseDouble(rs.getString("time"));
                Pair<Double, Double> pb = Pair.with(Double.parseDouble(rs.getString("lat_grid")), Double.parseDouble(rs.getString("lng_grid")));
                double time_val = Double.parseDouble(String.format("%.4f", (next_time - cur_time)*1440));
                double dist_val = Double.parseDouble(String.format("%.2f", distanceBetween2POIs(pa, pb)));

               // if (time_val > 1 && time_val <= 360) {
                if (dist_val > 0 && dist_val <= dist_interval){
                    String[] next_rows = new String[4];
                    next_rows[0] = rs.getString("timestamp");
                    next_rows[1] = rs.getString("grid_id");
                    next_rows[2] = rs.getString("lat_grid");
                    next_rows[3] = rs.getString("lng_grid");
                    newRows.add(next_rows);

                    pos = rs.getString("grid_id") + " ";
                    result += pos;

                    //当前grid坐标
                    int[] nowXY = GetGridsXY(Integer.parseInt(pos.trim()),50);
                    //前一个grid坐标
                    int[] XY = GetGridsXY(Integer.parseInt(prePos.trim()),50);
                    //前前一个grid坐标
                    int[] pre_XY = GetGridsXY(Integer.parseInt(preprePos.trim()),50);

                    vector1 = new int[]{nowXY[0]-XY[0],nowXY[1]-XY[1]};
                    vector2 = new int[]{XY[0]-pre_XY[0],XY[1]-pre_XY[1]};
                    double angele = GetAngle(vector1,vector2);
                    if (angele>2.1){
                        System.out.println(angele);
                        break;
                    }
                    //更新gridId
                    preprePos =prePos;
                    prePos = pos;

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
        //System.out.println(result);
        System.out.println(result.trim());
        return result.trim();
    }
    public static int[] GetGridsXY(int gridId,int grid_num){
        return new int[]{ gridId%grid_num , gridId/grid_num };
    }

    public static double GetAngle(int[] v1,int[] v2){
        double up = v1[0]*v2[0]+v1[1]*v2[1];
        double bottom = Math.sqrt(v1[0]*v1[0]+v1[1]*v1[1])*Math.sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
        return Math.acos(up/bottom);
    }







    public static void main (String[]args) throws SQLException, IOException, ClassNotFoundException {
        //writeUserPOIs(78,50,2);
        writeUserMatrix(78,50,5);
        //System.out.println(GetAngle(new int[]{3,0},new int[]{-2,1}));
    }

}