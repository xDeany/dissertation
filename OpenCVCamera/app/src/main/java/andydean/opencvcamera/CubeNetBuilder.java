package andydean.opencvcamera;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CubeNetBuilder extends AppCompatActivity {

    //THE NET List of List going Red, Orange, Yellow, Green, Blue, White
    /*              |-----------|
                    | 0 | 1 | 2 |
                    | 7 | Y | 3 |
                    | 6 | 5 | 4 |
        |-----------|-----------|-----------|-----------|
        | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 |
        | 7 | B | 3 | 7 | R | 3 | 7 | G | 3 | 7 | O | 3 |
        | 6 | 5 | 4 | 6 | 5 | 4 | 6 | 5 | 4 | 6 | 5 | 4 |
        |-----------|-----------|-----------|-----------|
                    | 0 | 1 | 2 |
                    | 7 | W | 3 |
                    | 6 | 5 | 4 |
                    |-----------|

     Index in single list form
                    |-----------|
                    | 16| 17| 18|
                    | 23| Y | 19|
                    | 22| 21| 20|
        |-----------|-----------|-----------|-----------|
        | 32| 33| 34| 0 | 1 | 2 | 24| 25| 26| 8 | 9 | 10|
        | 39| B | 35| 7 | R | 3 | 31| G | 27| 15| O | 11|
        | 38| 37| 36| 6 | 5 | 4 | 30| 29| 28| 14| 13| 12|
        |-----------|-----------|-----------|-----------|
                    | 40| 41| 42|
                    | 47| W | 43|
                    | 46| 45| 44|
                    |-----------|
     */
    //Faces stored in ROYGBW order for consistency
    List<CubePiece> piecesLeft = new ArrayList<>();
    List<List<View>> onScreenCube = new ArrayList<>();
    ArrayList<ArrayList<Character>> faces;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_net_builder);
        faces = (ArrayList<ArrayList<Character>>) getIntent().getSerializableExtra("faces");

        resetPiecesLeft();
        connectScreenCube();
        resetOnScreenCubeColours();
        updateColours(faces);
        /*boolean valid = cubeFit(faces, 5);
        double[] rgbVals = ColourDetector.getRGB('Y');
        findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        if(valid)
            rgbVals = ColourDetector.getRGB('G');
        else
            rgbVals = ColourDetector.getRGB('R');

        findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
*/
        Button startFit = (Button) findViewById(R.id.start_cube_fit);

        startFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = cubeFit(faces, 0);
                double[] rgbVals = ColourDetector.getRGB('Y');
                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

                if(valid)
                    rgbVals = ColourDetector.getRGB('G');
                else
                    rgbVals = ColourDetector.getRGB('R');

                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
                updateColours(faces);
            }
        });


    }

    private boolean cubeFit(ArrayList<ArrayList<Character>> faces, int level) {
        for (int rotations = 0; rotations < 4; rotations++) {
            //updateColours(faces);
            if (isValid(faces))
                return true;
            Collections.rotate(faces.get(level), 2);
            if(level < 5 && cubeFit(faces, level + 1))
                return true;
        }
        return false;
    }

    private boolean isValid(ArrayList<ArrayList<Character>> net){
        int red = 0;
        int orange = 0;
        int yellow = 0;
        int green = 0;
        int blue = 0;
        int white = 0;
        int black = 0;
        //Simple check to count the number of times each sticker colour appears
        for(ArrayList<Character> lc : net) {
            for (Character c : lc) {
                switch (c) {
                    case 'R':
                        red++;
                        break;
                    case 'O':
                        orange++;
                        break;
                    case 'Y':
                        yellow++;
                        break;
                    case 'G':
                        green++;
                        break;
                    case 'B':
                        blue++;
                        break;
                    case 'W':
                        white++;
                        break;
                    default:
                        black++;
                        break;
                }
            }
        }

        if(black > 0 || red != 8 || orange != 8 || yellow != 8 || green != 8 || blue != 8 || white != 8)
            return false;

        List<CubePiece> cube = netToPieces(net);
        for(CubePiece c : cube) {
            if(piecesLeft.size() > 1) {
                Iterator<CubePiece> plItr = piecesLeft.iterator();
                int sze = piecesLeft.size();
                while (plItr.hasNext() && piecesLeft.size() == sze) {
                    CubePiece pl = plItr.next();
                    if(!c.hasDuplicate() && pl.equals(c))
                        piecesLeft.remove(pl);
                }
            }else{
                if(piecesLeft.get(0).equals(c))
                    piecesLeft.remove(0);
            }
        }
        boolean valid = piecesLeft.isEmpty();
        resetPiecesLeft();
        return valid;
    }

    private List<CubePiece> netToPieces(ArrayList<ArrayList<Character>> net){
        ArrayList<Character> n = new ArrayList<>();
        for(ArrayList<Character> l : net)
            n.addAll(l);

        List<CubePiece> cube = new ArrayList<>();

        //Corners, red (top) face
        cube.add(new CubePiece(n.get(0), n.get(22), n.get(34)));
        cube.add(new CubePiece(n.get(2), n.get(20), n.get(24)));
        cube.add(new CubePiece(n.get(4), n.get(42), n.get(30)));
        cube.add(new CubePiece(n.get(6), n.get(36), n.get(40)));

        //Corners, orange (bottom) face
        cube.add(new CubePiece(n.get(8), n.get(26), n.get(18)));
        cube.add(new CubePiece(n.get(10), n.get(32), n.get(16)));
        cube.add(new CubePiece(n.get(12), n.get(46), n.get(38)));
        cube.add(new CubePiece(n.get(14), n.get(28), n.get(44)));

        //Edges, red (top) face
        cube.add(new CubePiece(n.get(1), n.get(21)));
        cube.add(new CubePiece(n.get(3), n.get(31)));
        cube.add(new CubePiece(n.get(5), n.get(41)));
        cube.add(new CubePiece(n.get(7), n.get(35)));

        //Edges, middle row
        cube.add(new CubePiece(n.get(43), n.get(29)));
        cube.add(new CubePiece(n.get(25), n.get(19)));
        cube.add(new CubePiece(n.get(23), n.get(33)));
        cube.add(new CubePiece(n.get(37), n.get(47)));

        //Edges, orange (bottom) face
        cube.add(new CubePiece(n.get(9), n.get(17)));
        cube.add(new CubePiece(n.get(11), n.get(39)));
        cube.add(new CubePiece(n.get(13), n.get(45)));
        cube.add(new CubePiece(n.get(15), n.get(27)));


        return cube;
    }

    private void resetPiecesLeft(){
        piecesLeft.clear();
        piecesLeft.add(new CubePiece('R', 'B'));
        piecesLeft.add(new CubePiece('R', 'B', 'W'));
        piecesLeft.add(new CubePiece('R', 'W'));
        piecesLeft.add(new CubePiece('R', 'W', 'G'));
        piecesLeft.add(new CubePiece('R', 'G'));
        piecesLeft.add(new CubePiece('R', 'G', 'Y'));
        piecesLeft.add(new CubePiece('R', 'Y'));
        piecesLeft.add(new CubePiece('R', 'Y', 'B'));
        piecesLeft.add(new CubePiece('Y', 'B'));
        piecesLeft.add(new CubePiece('W', 'B'));
        piecesLeft.add(new CubePiece('Y', 'G'));
        piecesLeft.add(new CubePiece('W', 'G'));
        piecesLeft.add(new CubePiece('O', 'B'));
        piecesLeft.add(new CubePiece('O', 'B', 'W'));
        piecesLeft.add(new CubePiece('O', 'W'));
        piecesLeft.add(new CubePiece('O', 'W', 'G'));
        piecesLeft.add(new CubePiece('O', 'G'));
        piecesLeft.add(new CubePiece('O', 'G', 'Y'));
        piecesLeft.add(new CubePiece('O', 'Y'));
        piecesLeft.add(new CubePiece('O', 'Y', 'B'));
    }

    private void resetOnScreenCubeColours(){
        for(int i=0; i<6; i++){
            List<View> onScreenSide = onScreenCube.get(i);
            for(int j=0; j<8; j++){
                View onScreenSquare = onScreenSide.get(j);

                double[] rgbVals = ColourDetector.getRGB('X');
                onScreenSquare.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
            }
        }
        //Reset centres
        View cYellow = findViewById(R.id.y9);
        double[] rgbVals = ColourDetector.getRGB('Y');
        cYellow.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        View cBlue = findViewById(R.id.b9);
        rgbVals = ColourDetector.getRGB('B');
        cBlue.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        View cRed = findViewById(R.id.r9);
        rgbVals = ColourDetector.getRGB('R');
        cRed.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        View cGreen = findViewById(R.id.g9);
        rgbVals = ColourDetector.getRGB('G');
        cGreen.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        View cOrange = findViewById(R.id.o9);
        rgbVals = ColourDetector.getRGB('O');
        cOrange.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        View cWhite = findViewById(R.id.w9);
        rgbVals = ColourDetector.getRGB('W');
        cWhite.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

    }

    private void connectScreenCube(){
        ArrayList<View> yellow = new ArrayList<>(8);
        yellow.add(findViewById(R.id.y1));
        yellow.add(findViewById(R.id.y2));
        yellow.add(findViewById(R.id.y3));
        yellow.add(findViewById(R.id.y4));
        yellow.add(findViewById(R.id.y5));
        yellow.add(findViewById(R.id.y6));
        yellow.add(findViewById(R.id.y7));
        yellow.add(findViewById(R.id.y8));

        ArrayList<View> blue = new ArrayList<>(8);
        blue.add(findViewById(R.id.b1));
        blue.add(findViewById(R.id.b2));
        blue.add(findViewById(R.id.b3));
        blue.add(findViewById(R.id.b4));
        blue.add(findViewById(R.id.b5));
        blue.add(findViewById(R.id.b6));
        blue.add(findViewById(R.id.b7));
        blue.add(findViewById(R.id.b8));

        ArrayList<View> red = new ArrayList<>(8);
        red.add(findViewById(R.id.r1));
        red.add(findViewById(R.id.r2));
        red.add(findViewById(R.id.r3));
        red.add(findViewById(R.id.r4));
        red.add(findViewById(R.id.r5));
        red.add(findViewById(R.id.r6));
        red.add(findViewById(R.id.r7));
        red.add(findViewById(R.id.r8));

        ArrayList<View> green = new ArrayList<>(8);
        green.add(findViewById(R.id.g1));
        green.add(findViewById(R.id.g2));
        green.add(findViewById(R.id.g3));
        green.add(findViewById(R.id.g4));
        green.add(findViewById(R.id.g5));
        green.add(findViewById(R.id.g6));
        green.add(findViewById(R.id.g7));
        green.add(findViewById(R.id.g8));

        ArrayList<View> orange = new ArrayList<>(8);
        orange.add(findViewById(R.id.o1));
        orange.add(findViewById(R.id.o2));
        orange.add(findViewById(R.id.o3));
        orange.add(findViewById(R.id.o4));
        orange.add(findViewById(R.id.o5));
        orange.add(findViewById(R.id.o6));
        orange.add(findViewById(R.id.o7));
        orange.add(findViewById(R.id.o8));

        ArrayList<View> white = new ArrayList<>(8);
        white.add(findViewById(R.id.w1));
        white.add(findViewById(R.id.w2));
        white.add(findViewById(R.id.w3));
        white.add(findViewById(R.id.w4));
        white.add(findViewById(R.id.w5));
        white.add(findViewById(R.id.w6));
        white.add(findViewById(R.id.w7));
        white.add(findViewById(R.id.w8));

        onScreenCube.add(red);
        onScreenCube.add(orange);
        onScreenCube.add(yellow);
        onScreenCube.add(green);
        onScreenCube.add(blue);
        onScreenCube.add(white);
    }

    private void updateColours(ArrayList<ArrayList<Character>> cube){
        for(int i=0; i<6; i++){
            List<View> onScreenSide = onScreenCube.get(i);
            List<Character> cubeSide = cube.get(i);
            for(int j=0; j<8; j++){
                View onScreenSquare = onScreenSide.get(j);
                Character squareColour = cubeSide.get(j);

                double[] rgbVals = ColourDetector.getRGB(squareColour);
                onScreenSquare.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
            }
        }
    }
}
