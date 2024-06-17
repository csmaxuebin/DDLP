package geolife_demo;

import org.ujmp.core.Matrix;

import java.io.IOException;

public class testMatrix {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Matrix matrix = Matrix.Factory.load("Java/mx/" + "50" + "/mx_" + "163");
        matrix.showGUI();
    }
}
