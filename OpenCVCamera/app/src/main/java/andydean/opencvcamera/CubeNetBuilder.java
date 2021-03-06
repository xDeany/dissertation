package andydean.opencvcamera;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

     Index in single list form index = (faceNum * 8) + stickerNum
                       |--------------|
                       | 16 | 17 | 18 |
                       | 23 | Y  | 19 |
                       | 22 | 21 | 20 |
        |--------------|--------------|--------------|--------------|
        | 32 | 33 | 34 | 0  | 1  | 2  | 24 | 25 | 26 | 8  | 9  | 10 |
        | 39 | B  | 35 | 7  | R  | 3  | 31 | G  | 27 | 15 | O  | 11 |
        | 38 | 37 | 36 | 6  | 5  | 4  | 30 | 29 | 28 | 14 | 13 | 12 |
        |--------------|--------------|--------------|--------------|
                       | 40 | 41 | 42 |
                       | 47 | W  | 43 |
                       | 46 | 45 | 44 |
                       |--------------|

        Index in cubePiece form
                          |-----------------|
                          | 14c | 13b | 12c |
                          | 10a | Y   | 9b  |
                          | 0b  | 1b  | 2b  |
        |-----------------|-----------------|-----------------|-----------------|
        | 14b | 10b | 0c  | 0a  | 1a  | 2a  | 2c  | 9a  | 12b | 12a | 13a | 14a |
        | 15b | B   | 7b  | 7a  | R   | 3a  | 3b  | G   | 19b | 19a | O   | 15a |
        | 16c | 11a | 6b  | 6a  | 5a  | 4a  | 4c  | 8b  | 18b | 18a | 17a | 16a |
        |-----------------|-----------------|-----------------|-----------------|
                          | 6c  | 5b  | 4b  |
                          | 11b | W   | 8a  |
                          | 16b | 17b | 18c |
                          |-----------------|

                          ArrayList<Integer> red = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7));
        ArrayList<Integer> orange = new ArrayList<>(Arrays.asList(12,13,14,15,16,17,18,19));
        ArrayList<Integer> yellow = new ArrayList<>(Arrays.asList(14,13,12,9,2,1,0,10));
        ArrayList<Integer> green = new ArrayList<>(Arrays.asList(2,9,12,19,18,8,4,3));
        ArrayList<Integer> blue = new ArrayList<>(Arrays.asList(14,10,0,7,6,11,16,15));
        ArrayList<Integer> white = new ArrayList<>(Arrays.asList(6,5,4,8,18,17,16,11));
        allPieceIndexesA = new ArrayList<>(Arrays.asList(red, orange, yellow, green, blue, white));

        red = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0));
        orange = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0));
        yellow = new ArrayList<>(Arrays.asList(2,1,2,1,1,1,1,0));
        green = new ArrayList<>(Arrays.asList(2,0,1,1,1,1,2,1));
        blue = new ArrayList<>(Arrays.asList(1,1,2,1,1,0,2,1));
        white = new ArrayList<>(Arrays.asList(2,1,1,0,2,1,1,1));
        allPieceIndexesB = new ArrayList<>(Arrays.asList(red, orange, yellow, green, blue, white));
         */
    //Faces stored in ROYGBW order for consistency
    //Connection to the net displayed on screen
    List<List<View>> onScreenCube = new ArrayList<>();
    TextView runTimeBruteForce;
    TextView runTimeImproved;
    final String BRUTE_FORCE_SUFFIX = "Time taken to build net with brute force algorithm - ";
    final String IMPROVED_SUFFIX = "Time taken to build net with improved algorithm - ";

    //Faces supplied by activity caller
    ArrayList<ArrayList<Character>> faces;
    ArrayList<Integer> netLocation;

    //Index arrays storing the cubePiece net, A is the piece number and B is the sticker num (a/b/c)
    //Allows for simpler translation from net -> cube
    private static List<ArrayList<Integer>> allPieceIndexesA = new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7)),
                                                                                new ArrayList<>(Arrays.asList(12,13,14,15,16,17,18,19)),
                                                                                new ArrayList<>(Arrays.asList(14,13,12,9,2,1,0,10)),
                                                                                new ArrayList<>(Arrays.asList(2,9,12,19,18,8,4,3)),
                                                                                new ArrayList<>(Arrays.asList(14,10,0,7,6,11,16,15)),
                                                                                new ArrayList<>(Arrays.asList(6,5,4,8,18,17,16,11))));

    private static List<ArrayList<Integer>> allPieceIndexesB = new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0)),
                                                                                new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0)),
                                                                                new ArrayList<>(Arrays.asList(2,1,2,1,1,1,1,0)),
                                                                                new ArrayList<>(Arrays.asList(2,0,1,1,1,1,2,1)),
                                                                                new ArrayList<>(Arrays.asList(1,1,2,1,1,0,2,1)),
                                                                                new ArrayList<>(Arrays.asList(2,1,1,0,2,1,1,1))));

    //The blank net
    final ArrayList<Character> BLANK_ROW = new ArrayList<>(Arrays.asList('X','X','X','X','X','X','X','X'));
    final ArrayList<ArrayList<Character>> BLANK_NET = new ArrayList<>(Arrays.asList(BLANK_ROW, BLANK_ROW, BLANK_ROW, BLANK_ROW, BLANK_ROW, BLANK_ROW));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_net_builder);
        faces = (ArrayList<ArrayList<Character>>) getIntent().getSerializableExtra("faces");
        netLocation = new ArrayList<>(Arrays.asList(0,0,0,0,0,-1));

        runTimeBruteForce = (TextView) findViewById(R.id.bruteForceText);
        runTimeImproved = (TextView) findViewById(R.id.fasterText);

        runTimeBruteForce.setText(BRUTE_FORCE_SUFFIX);
        runTimeImproved.setText(IMPROVED_SUFFIX);

        //Set all of the globals
        //setPieceIndexes();

        //Connect the array for storing the on screen net with the net in XML
        connectScreenCube();

        //Sets the centre squares of each face on screen to the colours used for ROYGBW stickers
        resetOnScreenCentres();

        //Set initial net to be the face given
        updateColours(faces);




        //Yellow square in the top right to signify busy
        double[] rgbVals = ColourDetector.getRGB('Y');
        findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        //Create and use a copy of the inital net
        //ArrayList<ArrayList<Character>> copy = cloneNet(faces);

        //Fit the faces together, drawing the new net if found
        long t1 = SystemClock.currentThreadTimeMillis();
        Pair<ArrayList<ArrayList<Character>>, ArrayList<Integer>> result = improvedSearch(netToCube(BLANK_NET), new ArrayList<>(Arrays.asList(0,0,0,0,0,0)), netLocation, faces);
        long t2 = SystemClock.currentThreadTimeMillis() - t1;
        long mill = t2 % 1000;
        long seconds = (t2 / 1000) % 60;
        //boolean valid = dfs(faces, 0);
        String str = IMPROVED_SUFFIX + String.valueOf(seconds) + "." + String.valueOf(mill) + " sec";
        runTimeImproved.setText(str);
        //boolean valid = dfs(faces, 0);

        if(result != null && isValid(result.first)) {
            rgbVals = ColourDetector.getRGB('G');
            updateColours(result.first);
            netLocation = result.second;
        }
        else
            rgbVals = ColourDetector.getRGB('R');

        findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

        Button efficientCubeFitButton = (Button) findViewById(R.id.start_cube_fit);

        //Buttons to reset the net, and try both algorithms
        efficientCubeFitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double[] rgbVals = ColourDetector.getRGB('Y');
                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

                //ArrayList<ArrayList<Character>> copy = cloneNet(faces);
                long t1 = SystemClock.currentThreadTimeMillis();
                Pair<ArrayList<ArrayList<Character>>, ArrayList<Integer>> result = improvedSearch(netToCube(BLANK_NET),new ArrayList<>(Arrays.asList(0,0,0,0,0,0)), netLocation, faces);
                long t2 = SystemClock.currentThreadTimeMillis() - t1;
                long mill = t2 % 1000;
                long seconds = (t2 / 1000) % 60;
                //boolean valid = dfs(faces, 0);
                String str = IMPROVED_SUFFIX + String.valueOf(seconds) + "." + String.valueOf(mill) + " sec";
                runTimeImproved.setText(str);
                if(result != null) {
                    rgbVals = ColourDetector.getRGB('G');
                    updateColours(result.first);
                    netLocation = result.second;
                }
                else
                    rgbVals = ColourDetector.getRGB('R');

                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
            }
        });

        Button bruteForceButton = (Button) findViewById(R.id.brute_force);

        bruteForceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double[] rgbVals = ColourDetector.getRGB('Y');
                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));

                //ArrayList<ArrayList<Character>> copy = cloneNet(faces);
                long t1 = SystemClock.currentThreadTimeMillis();
                ArrayList<ArrayList<Character>> valid = dfs(faces, 0);
                long t2 = SystemClock.currentThreadTimeMillis();

                long diff = t2 - t1;
                long mill = diff % 1000;
                long seconds = (diff / 1000) % 60;
                //boolean valid = dfs(faces, 0);
                String str = BRUTE_FORCE_SUFFIX + String.valueOf(seconds) + "." + String.valueOf(mill) + " sec";
                runTimeBruteForce.setText(str);

                if(valid != null) {
                    rgbVals = ColourDetector.getRGB('G');
                    updateColours(valid);
                }
                else
                    rgbVals = ColourDetector.getRGB('R');

                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
            }
        });

        Button resetNet = (Button) findViewById(R.id.reset_net);
        resetNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateColours(faces);
                double[] rgbVals = ColourDetector.getRGB('X');
                findViewById(R.id.valid).setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
                netLocation = new ArrayList<>(Arrays.asList(0,0,0,0,0,-1));
            }
        });

    }

    /**
     * Reccursive function to try and fit the faces of the cube together
     * @param currentCube **The current state of the cube**
     * @param currentRotation **The current position in the tree search**
     * @param targetRotation **The starting position in the tree search**
     * @param facesLeft **The faces still to add to the net**
     * @return newNet **The new net created, if all sides are added, else null**
     */
    public static Pair<ArrayList<ArrayList<Character>>, ArrayList<Integer>> improvedSearch(List<CubePiece> currentCube,
                                                                                           ArrayList<Integer> currentRotation,
                                                                                           ArrayList<Integer> targetRotation,
                                                                                           ArrayList<ArrayList<Character>> facesLeft) {

        ArrayList<ArrayList<Character>> facesLeftCopy = cloneNet(facesLeft);
        //Unlink the face to be added in
        ArrayList<Character> face = facesLeftCopy.remove(0);
        int faceNum = 5-facesLeftCopy.size();
        int initRotation = 0;

        //Check if it is needing to skip to a later part in the tree
        //Increment the white side to go to the next branch
        if(lessEqual(currentRotation, targetRotation)) {
            initRotation = targetRotation.get(faceNum);
            if(faceNum == 5)
                initRotation++;
        }

        //Attempt to add in the face with each possible rotation
        //If added, try to add the next face. If the next face/s fail, try the next rotation until all tried
        for(int rotation=0; rotation<4; rotation++){

            //Skip to turning the face if needing to skip to later branch in the tree
            if(rotation >= initRotation) {
                //Attempt to add the face into the net
                ArrayList<CubePiece> newCube = addSide(currentCube, face, faceNum);
                //Go to next rotation if it failed
                if (newCube != null) {
                    //Set the current location in the tree
                    currentRotation.set(faceNum, rotation);
                    //Check if any more faces need to be added
                    if (facesLeftCopy.size() > 0) {

                        //Run the next layer of the tree
                        Pair<ArrayList<ArrayList<Character>>, ArrayList<Integer>> result = improvedSearch(newCube,
                                                                                                currentRotation,
                                                                                                targetRotation,
                                                                                                facesLeftCopy);
                        //Check next rotation if lower layers return null
                        if (result != null)
                            return result;
                    } else
                        //Base case, return resulting net and the location in the tree
                        return new Pair<>(cubeToNet(newCube), currentRotation);
                }
            }
            //Rotate the face and try again
            Collections.rotate(face,2);
        }
        //All rotations failed, either at this level, or all lower levels
        return null;
    }

    /**
     * Converts the locations/rotations from base 4 to base 10 and runs standard <= operator
     * @param currentRotation **The left side of the operation**
     * @param targetRotation **The right side of the operation**
     * @return left <= right
     */
    private static boolean lessEqual(ArrayList<Integer> currentRotation, ArrayList<Integer> targetRotation) {
        int currentRot = 0;
        int targetRot = 0;
        for(int i=0; i<6; i++){
            int curDigit = currentRotation.get(5-i);
            int tarDigit = targetRotation.get(5-i);
            currentRot += curDigit * (4^i);
            targetRot += tarDigit * (4^i);
        }
        return currentRot <= targetRot;
    }

    /**
     * Will add a face to a net iff certain cube conditions are satisfied.
     * The new face must not create pieces that have duplicate stickers on (i.e a yellow-yellow piece is not valid)
     * The pieces created by the new face must be valid pieces
     * All pieces on the cube must be unique
     * @param cube **The current cube**
     * @param face **The face to be added**
     * @param faceNum **The position to add the face to**
     * @return newNet if successful, else null
     */
    private static ArrayList<CubePiece> addSide(List<CubePiece> cube, ArrayList<Character> face, int faceNum){
        //Copy the cube
        ArrayList<CubePiece> cubeClone = new ArrayList<>();
        for(CubePiece c : cube)
            cubeClone.add(c.clone());

        //List to contain copies of the new pieces created
        ArrayList<CubePiece> alteredPieces = new ArrayList<>();
        boolean allAdded = true;

        //Add face to cube, allAdded checks for any failed pieces
        for(int i=0; i<8; i++){
            Character c = face.get(i);
            boolean added = cubeClone.get(allPieceIndexesA.get(faceNum).get(i)).setSticker(c,allPieceIndexesB.get(faceNum).get(i));
            alteredPieces.add(cubeClone.get(allPieceIndexesA.get(faceNum).get(i)).clone());
            allAdded = allAdded && added;
        }

        if(!allAdded)
            return null;

        //Check that all full altered pieces are unique
        for(CubePiece ap : alteredPieces) {
            int numMatches = 0;
            if (ap.isFull())
                for (CubePiece cp2 : cubeClone)
                    if (ap.equals(cp2))
                        numMatches++;
            if(numMatches > 1)
                return null;
        }

        return cubeClone;
    }

    /**
     * Depth first search way of fitting the net together. Recursively tries all permutations of the net
     * @param net **The current net of the cube**
     * @param level **Level of recursion**
     * @return newNet if successful, else null
     */
    public static ArrayList<ArrayList<Character>> dfs(ArrayList<ArrayList<Character>> net, int level) {
        ArrayList<ArrayList<Character>> copy = cloneNet(net);
        //Try each rotation of this level, return net if successful, else try all permutations of the next layer
        for (int rotations = 0; rotations < 4; rotations++) {

            if (isValid(copy))
                return copy;

            if(level < 5) {
                ArrayList<ArrayList<Character>> newNet = dfs(copy, level + 1);
                if (newNet != null)
                    return newNet;
            }

            Collections.rotate(copy.get(level), 2);
        }
        return null;
    }

    /**
     * Checks if the net is a valid net of a cube
     * @param net **Net to be checked**
     * @return valid or not valid
     */
    private static boolean isValid(ArrayList<ArrayList<Character>> net){
        List<CubePiece> validPieces = generateAllValidPieces();
        //Generate a cube from the net, check that this cube contains the same pieces as the list of valid pieces (no more, no less)
        List<CubePiece> cube = netToCube(net);
        for(CubePiece c : cube)
            if(validPieces.size() > 1) {
                Iterator<CubePiece> plItr = validPieces.iterator();
                int sze = validPieces.size();
                while (plItr.hasNext() && validPieces.size() == sze) {
                    CubePiece pl = plItr.next();
                    if(!c.hasDuplicate() && pl.equals(c))
                        validPieces.remove(pl);
                }
            }else
                if(validPieces.get(0).equals(c))
                    validPieces.remove(0);


        return validPieces.isEmpty();
    }

    /**
     * Converst a cube back into it's net form
     * @param cube **The cube to convert**
     * @return net of the cube
     */
    public static ArrayList<ArrayList<Character>> cubeToNet(List<CubePiece> cube){
        ArrayList<ArrayList<Character>> net = new ArrayList<>();
        for(int i=0; i<6; i++){
            ArrayList<Character> face = new ArrayList<>();
            for(int j=0; j<8; j++){
                Character c = cube.get(allPieceIndexesA.get(i).get(j)).getStickers().get(allPieceIndexesB.get(i).get(j));
                face.add(c);
            }
            net.add(face);
        }
        return net;
    }

    /**
     * Converts a net of a cube into a List of CubePiece's
     * @param net **Net to be converted**
     * @return cube form of the net
     */
    public static List<CubePiece> netToCube(ArrayList<ArrayList<Character>> net){
        ArrayList<Character> n = new ArrayList<>();
        for(ArrayList<Character> l : net)
            n.addAll(l);

        List<CubePiece> cube = new ArrayList<>();

        //Red/top face in sticker order
        cube.add(new CubePiece(n.get(0), n.get(22), n.get(34)));
        cube.add(new CubePiece(n.get(1), n.get(21)));
        cube.add(new CubePiece(n.get(2), n.get(20), n.get(24)));
        cube.add(new CubePiece(n.get(3), n.get(31)));
        cube.add(new CubePiece(n.get(4), n.get(42), n.get(30)));
        cube.add(new CubePiece(n.get(5), n.get(41)));
        cube.add(new CubePiece(n.get(6), n.get(36), n.get(40)));
        cube.add(new CubePiece(n.get(7), n.get(35)));

        //Edges, middle row
        cube.add(new CubePiece(n.get(43), n.get(29)));
        cube.add(new CubePiece(n.get(25), n.get(19)));
        cube.add(new CubePiece(n.get(23), n.get(33)));
        cube.add(new CubePiece(n.get(37), n.get(47)));

        //Orange/bottom face in sticker order
        cube.add(new CubePiece(n.get(8), n.get(26), n.get(18)));
        cube.add(new CubePiece(n.get(9), n.get(17)));
        cube.add(new CubePiece(n.get(10), n.get(32), n.get(16)));
        cube.add(new CubePiece(n.get(11), n.get(39)));
        cube.add(new CubePiece(n.get(12), n.get(46), n.get(38)));
        cube.add(new CubePiece(n.get(13), n.get(45)));
        cube.add(new CubePiece(n.get(14), n.get(28), n.get(44)));
        cube.add(new CubePiece(n.get(15), n.get(27)));

        return cube;
    }

    /**
     * Generates a clone of a net
     * @param oldNet **Net to clone**
     * @return clone of the net
     */
    public static ArrayList<ArrayList<Character>> cloneNet(ArrayList<ArrayList<Character>> oldNet){
        ArrayList<ArrayList<Character>> newNet = new ArrayList<>();
        for(ArrayList<Character> nL : oldNet){
            ArrayList<Character> cL = new ArrayList<>();
            for(Character c : nL){
                cL.add(c);
            }
            newNet.add(cL);
        }
        return newNet;
    }

    /**
     * Generates a list of all the valid combinations of pieces
     * @return Technically, a fully solved cube
     */
    public static List<CubePiece> generateAllValidPieces(){
        List<CubePiece> validPieces = new ArrayList<>();
        validPieces.add(new CubePiece('R', 'Y', 'B'));
        validPieces.add(new CubePiece('R', 'Y'));
        validPieces.add(new CubePiece('R', 'G', 'Y'));
        validPieces.add(new CubePiece('R', 'G'));
        validPieces.add(new CubePiece('R', 'W', 'G'));
        validPieces.add(new CubePiece('R', 'W'));
        validPieces.add(new CubePiece('R', 'B', 'W'));
        validPieces.add(new CubePiece('R', 'B'));


        validPieces.add(new CubePiece('W', 'G'));
        validPieces.add(new CubePiece('Y', 'G'));
        validPieces.add(new CubePiece('W', 'B'));
        validPieces.add(new CubePiece('Y', 'B'));


        validPieces.add(new CubePiece('O', 'G', 'Y'));
        validPieces.add(new CubePiece('O', 'Y'));
        validPieces.add(new CubePiece('O', 'Y', 'B'));
        validPieces.add(new CubePiece('O', 'B'));
        validPieces.add(new CubePiece('O', 'B', 'W'));
        validPieces.add(new CubePiece('O', 'W'));
        validPieces.add(new CubePiece('O', 'W', 'G'));
        validPieces.add(new CubePiece('O', 'G'));

        return validPieces;
    }

    /**
     * Resets the rgb values of the background of the centres of each face of the on screen net
     */
    private void resetOnScreenCentres(){
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

    /**
     * Populates the List of Views with the views they refer to
     */
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

    /**
     * Updates the on screen net to the one supplied
     * @param net **Net to be drawn**
     */
    private void updateColours(ArrayList<ArrayList<Character>> net){
        for(int i=0; i<6; i++){
            List<View> onScreenSide = onScreenCube.get(i);
            List<Character> cubeSide = net.get(i);
            for(int j=0; j<8; j++){
                View onScreenSquare = onScreenSide.get(j);
                Character squareColour = cubeSide.get(j);

                double[] rgbVals = ColourDetector.getRGB(squareColour);
                onScreenSquare.setBackgroundColor(Color.rgb((int) rgbVals[0], (int) rgbVals[1], (int) rgbVals[2]));
            }
        }
    }
}
