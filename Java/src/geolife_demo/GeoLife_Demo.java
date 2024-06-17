/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolife_demo;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import eu.simuline.octave.OctaveEngine;
import eu.simuline.octave.OctaveEngineFactory;
import eu.simuline.octave.exception.OctaveEvalException;
import eu.simuline.octave.type.OctaveDouble;
import org.javatuples.Pair;
import java.sql.*;
import org.ujmp.core.Matrix;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tom
 */
public class GeoLife_Demo {

    private static Pair<Integer, Integer> getMapCoordinate(int grid_id, int grid_num) {
        int x = grid_id % grid_num; // 6
        int y = grid_id / grid_num; // 11 (y * grid_num + x = grid_id)
        return Pair.with(x, y);
    }

    private static int getGridIndexAtMapCoordinate(int x, int y, int grid_num) {
        return (y * grid_num + x);
    }

    // T  (row 2, col m) : [X, Y] map coordinate
    private static String getT(Matrix mx, int grid_num) {
        String result = "[ ";
        String x = "";
        String y = "";
        double[][] m = mx.toDoubleArray();
        for (int r = 0; r < mx.getRowCount(); r++) {
            int index = Integer.parseInt(mx.getRowLabel(r));
            Pair xy = getMapCoordinate(index, grid_num);   //index = grid_id
//            System.out.println(xy);
            x += xy.getValue0() + " ";
            y += xy.getValue1() + " ";
        }
        result += x;
        result += "; ";
        result += y;
        return result + "]";
    }

    private static String get_true_loc(Matrix mx, int state_no) {
        String result = "[ ";
        Matrix matrix = mx.getRowList().get(state_no);
        double[][] m = matrix.toDoubleArray();
        for (int c = 0; c < matrix.getColumnCount(); c++) {
            if (c == state_no) {
                result += "1" + " ";
            } else {
                result += "0" + " ";
            }
        }
        return result + "]";
    }

    private static String get_p_prior(Matrix mx, int state_no) {
        String result = "[ ";
        Matrix matrix = mx.getRowList().get(state_no);
        double[][] m = matrix.toDoubleArray();
        for (int c = 0; c < matrix.getColumnCount(); c++) {
            result += m[0][c] + " ";
        }
        return result + "]";
    }

    private static String getOctaveMatrix(Matrix mx) {
        String result = "[ ";
        double[][] m = mx.toDoubleArray();
        for (int r = 0; r < mx.getRowCount(); r++) {
            //System.out.println("r="+r);
            for (int c = 0; c < mx.getColumnCount(); c++) {
                //System.out.println("c="+c);
                result += m[r][c] + " ";
            }
            if (r < mx.getRowCount() - 1) {
                result += "; ";
            }
        }
        return result + "]";
    }

    /*
        paper 2.1 Two Coordinate Systems T is x; true_loc is u
        T           (row 2, col m) : [X, Y] map coordinate
        true_loc    (row 1, col m) : state coordinate
        p_prior     (row 1, col m) : 概率
        state_no                   : markov states index   
        delta = 0.1
        setting = 1
        eps = 0.5           
     */
    private static void runMatlab(int user_id, int grid_num, double delta, double eps,String method)
            throws IOException, ClassNotFoundException, SQLException {

        String usr_id = String.format("%1$03d", user_id);
        Matrix matrix = Matrix.Factory.load("Java/mx/" + grid_num + "/mx_" + usr_id);
       // Matrix matrix = Matrix.Factory.load("Java/newMx/" + grid_num + "/mx_to_" + usr_id);
//        matrix.showGUI();

        List<Integer> usr_poi_trace = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader("html/csv/" + grid_num + "/POIs/POIs_" + usr_id + ".csv"));
        //CSVReader reader = new CSVReader(new FileReader("/home/wilen/work/javawork/LocLok_Demo/html/csv/50/poi/50078_2008-05-30.csv"));
        String[] nextLine = reader.readNext();
        while ((nextLine = reader.readNext()) != null) {
            usr_poi_trace.add(Integer.parseInt(nextLine[1]));//read grid_id
        }
        int examTimes=20;
        for (int k = 10; k < examTimes; k++) {
            CSVWriter writer = new CSVWriter(new FileWriter("html/csv/" + grid_num + "/Matlab/"+method+"_"+k+"_"+"Matlab_" + usr_id+ ".csv"), ',');
            List<String[]> newRows = new ArrayList<>();

            String[] title = new String[8];//
            title[0] = "state_no_vec";
            title[1] = "z";
            title[2] = "z_true";
            title[3] = "pr_post";
            title[4] = "size_deltax";//
            title[5] = "count";//
            title[6] = "ratio";//
            title[7] = "euc_dist";//

            newRows.add(title);

            OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
            octave.eval("addpath(\"Java/matlab/\");");
            //System.out.println("the third load user56");
            //System.out.println("load path.....");
            octave.eval("matrix=" + getOctaveMatrix(matrix));
            //System.out.println("calculate matrix");
            octave.eval("T=" + getT(matrix, grid_num));
            //System.out.println("calculate  T");
            octave.eval("delta=" + delta);
            //System.out.println("calculate delta");
            octave.eval("setting=1");
            //System.out.println("calculate  setting");
            octave.eval("eps=" + eps);
            //System.out.println("calculate  eps");
            // octave.eval("grid_num=" + grid_num);//

            float sum =0;
            float sum_i=0;
            for (int i = 0; i <usr_poi_trace.size(); i++){
            //for (int i = 0; i < 150; i++){
                int state_no = -1;
                for (int j = 0; j < matrix.getRowCount(); j++) {
                    if (usr_poi_trace.get(i) == Integer.parseInt(matrix.getRowLabel(j))) {
                        state_no = j;
                    }

                } // find label in matrix

                if (state_no < 0) {
                    System.out.println("NOT FOUND : " + usr_poi_trace.get(i));
                    continue;
                }

                octave.eval("true_loc=" + get_true_loc(matrix, state_no));
                if (i == 0) {
                    octave.eval("p_prior=" + get_p_prior(matrix, state_no));
                } else {
                    octave.eval("p_prior = pr_post * matrix");
                }
                octave.eval("state_no=" + (state_no + 1)); // matlab index starts from 1 so add 1

                try {
                    octave.eval("pkg load statistics");
                    octave.eval("[DeltaX, state_no_vec,size_deltax] = genPossibleSet(T, p_prior, true_loc, state_no, delta, setting)");
                    if (method.equals(new String("exp"))){
                        octave.eval("[z, z_true, MAX,time_elps,count,euc_dist] = exp_mechanism(true_loc, state_no, eps, DeltaX, T)");
                        octave.eval("[pr_post] = exp_inference(p_prior, z, DeltaX, eps, T,MAX)");
                    }
                    if (method.equals(new String("pim"))){
                        octave.eval("[z, z_true, var_z, A, vertices, time_elps,count,euc_dist] = IM_Release(true_loc, state_no, eps, DeltaX, T)");
                        octave.eval("[pr_post,Trans_vertices,S] = IM_inference(p_prior, z, DeltaX, eps, T, A, vertices)");
                    }
                    if (method.equals(new String("lap"))) {
                        octave.eval("[z, z_true, time_elps,MAX,count,euc_dist] = laplace(true_loc, state_no, eps, DeltaX, T)");
                        octave.eval("[pr_post] = laplace_inference(p_prior, z, DeltaX, eps, T,MAX)");
                    }
                } catch (OctaveEvalException e) {
                    System.out.println("Ignore " + usr_poi_trace.get(i) + " in User : " + usr_id + " which throw " + e);
                    break;
                }

                final OctaveDouble state_no_vec = octave.get(OctaveDouble.class, "state_no_vec");
                final OctaveDouble z = octave.get(OctaveDouble.class, "z");
                final OctaveDouble z_true = octave.get(OctaveDouble.class, "z_true");
                final OctaveDouble pr_post = octave.get(OctaveDouble.class, "pr_post");
                final OctaveDouble size_deltax = octave.get(OctaveDouble.class, "size_deltax");//
                final OctaveDouble count= octave.get(OctaveDouble.class, "count");//
                final OctaveDouble euc_dist= octave.get(OctaveDouble.class, "euc_dist");//

                String[] rows = new String[8];//before is 4

                rows[0] = "";
                for (int j = 1; j <= state_no_vec.getSize(2); j++) {
                    int label = (int) state_no_vec.get(1,j);
                    rows[0] += matrix.getRowLabel(label - 1) + " ";
//                rows[0] += (int) state_no_vec.getData()[j] + " ";
                }
                rows[0] = rows[0].trim();

                rows[1] = "";
                for (int j = 1; j <= z.getSize(1); j++) {
                    rows[1] += (int) z.get(j) + " ";
                }
                rows[1] = rows[1].trim();

//            rows[2] = "";
//            for (int j = 0; j < z_true.getSize()[0]; j++) {
//                rows[2] += (int) z_true.getData()[j] + " ";
//            }
//            rows[2] = rows[2].trim();
                int x = (int) z_true.get(1);
                int y = (int) z_true.get(2);
                rows[2] = "" + getGridIndexAtMapCoordinate(x, y, grid_num);

                rows[3] = "";
                for (int j = 1; j <= state_no_vec.getSize(2); j++) {
                    int idx = (int) state_no_vec.get(1,j);
                    rows[3] += String.format("%.4f", pr_post.get(1,idx)) + " ";
                }
                rows[3] = rows[3].trim();

                //zi ji zeng jia de
                rows[4] = "";//
                rows[4] += (int) size_deltax.get(1) + " ";//
                rows[4] = rows[4].trim();//

                //zi ji zeng jia de
                rows[5] = "";//
                rows[5] += (float) count.get(1) + " ";//
                rows[5] = rows[5].trim();//

                sum+=(float)count.get(1); //count is the number of drift
                sum_i=(float)(i+1); //i is from 0 ; but the number of time is from 1, so add 1  eg:1,2,3,4,5,6....
                rows[6] = "";//
                rows[6] += sum/sum_i+ " ";//drift_ratio
                rows[6] = rows[6].trim();//


                //zi ji zeng jia de
                rows[7] = "";//
                rows[7] += (float) euc_dist.get(1) + " ";//
                rows[7] = rows[7].trim();//

                newRows.add(rows);
            } // for each POI

            octave.close();
            writer.writeAll(newRows);
            writer.close();
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
            throws FileNotFoundException, ClassNotFoundException, SQLException, IOException {

        /* ********************** output to log_file *********************** */
        long startTime = System.currentTimeMillis();
        File file = new File("log_file.txt");
        FileOutputStream fis = new FileOutputStream(file);
        PrintStream out = new PrintStream(fis);
        System.setOut(out);
        /* ***************************************************************** */

       // int[] uid = {3, 7, 16, 56, 68, 78, 106, 163};
        int[] uid={84};

        int grid_num = 50;
        //GeoLifeDataAnalyse.writeGridsToCSV(grid_num);
        int interval_in_mins = 5;
//        for (int i : uid) {
//            GeoLifeDataAnalyse.writeUserPOIs(i, grid_num, interval_in_mins);
//            GeoLifeDataAnalyse.writeUserMatrix(i, grid_num, interval_in_mins);
//        }
        double delta=0.01;
        // double delta = Math.pow(10,-1.5);  // delta 小, 跑圈外的几率小.
         double eps = 1;
         // eps 大, hull面积变小.
//        for (int i : uid) {
//            GeoLifeDataAnalyse.writeUserPOIs(i, grid_num, interval_in_mins);
//        }
        //String[] methods=new String[]{"pim","exp","lap"};
          String[] methods=new String[]{"pim"};
        for (int i : uid) {
            for(String method:methods){
                runMatlab(i, grid_num, delta, eps,method);
            }

        }
        long endTime = System.currentTimeMillis();
        System.out.println("runtime:"+(endTime-startTime)+"ms");//1ms=0.001s
    } // main

}
