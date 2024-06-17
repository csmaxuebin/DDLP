package geolife_demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.javatuples.Pair;
import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation.Ret;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author tom
 */
public class MarkovChain {

    private String chain;
    private Matrix matrix;

    public MarkovChain() {
        chain = "";
        matrix = Matrix.Factory.emptyMatrix();
    }

    public MarkovChain(String seq) {
        chain = seq;
        matrix = firstOrderTransitionMatrix();
    }

    public Matrix getMatrix() {
        return matrix;
    }

    /* Delete rows with all zeros in transition matrix */
    public static Matrix deleteRowsWithAllZeros(Matrix mx) {
        List<Integer> del_rows = new ArrayList<>();
        for (int r = 0; r < mx.getRowCount(); r++) {
            double sum = mx.getRowList().get(r).getValueSum();
            if (sum == 0) {
                del_rows.add(r);
            }
        }
        return mx.deleteRows(Ret.NEW, del_rows);
    }

    /* Construct a first-order Markov chain transition matrix */
    private Matrix firstOrderTransitionMatrix() {
        String[] seqs = chain.split(" ");

        Set<Integer> states = new TreeSet<>();
        for (String s : seqs) {
            if (!s.isEmpty()) {
                states.add(Integer.parseInt(s));
            }
        }
        
        if (states.isEmpty()) {
            return Matrix.Factory.emptyMatrix();
        }
        
        Map<Pair<String, String>, Integer> transition_ctr = new TreeMap<>();
        for (int a = 0, b = 1; a < seqs.length - 1 && b < seqs.length; a++, b++) {
            Pair<String, String> item = Pair.with(seqs[a], seqs[b]);
            if (transition_ctr.containsKey(item)) {
                int ctr = transition_ctr.get(item);
                transition_ctr.replace(item, ++ctr);
            } else {
                transition_ctr.put(item, 1);
            }
        }

        Matrix result = Matrix.Factory.zeros(states.size(), states.size());
        Map<String, Integer> state_index = new TreeMap<>();
        int idx = 0;
        for (Integer s : states) {
            String str = s.toString();
            state_index.put(str, idx);
            result.setRowLabel(idx, str);
            result.setColumnLabel(idx, str);
            idx++;
        }

        transition_ctr.forEach((k, v) -> {
            if (state_index.containsKey(k.getValue0()) && state_index.containsKey(k.getValue1())) {
                result.setAsDouble(v, state_index.get(k.getValue0()), state_index.get(k.getValue1()));
            }
        });

        double[][] m = result.toDoubleArray();
        for (int r = 0; r < result.getRowCount(); r++) {
            double sum = result.getRowList().get(r).getValueSum();
            if (sum != 0) {
                for (int c = 0; c < result.getColumnCount(); c++) {
                    result.setAsDouble(m[r][c] / sum, r, c);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String str = "1 2 1 1 3 4 4 1 2 4 1 4 3 4 4 4 3 1 3 2 3 3 3 4 2 2 3";
        Matrix mx = new MarkovChain(str).getMatrix();
//        mx.showGUI();

        System.out.println(mx);
    }
    
}
