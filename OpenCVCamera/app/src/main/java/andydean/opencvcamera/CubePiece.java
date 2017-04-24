package andydean.opencvcamera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Andy on 04/04/2017.
 */

public class CubePiece {

    private List<Character> stickers = new ArrayList<>();
    private boolean edgePiece;
    private boolean full;
    private boolean hasDuplicate;
    public CubePiece(char a, char b, char c) {
        stickers.add(a);
        stickers.add(b);
        stickers.add(c);
        edgePiece = false;
        hasDuplicate = checkDuplicates();
        full = checkFull();
    }

    public CubePiece(char a, char b) {
        stickers.add(a);
        stickers.add(b);
        edgePiece = true;
        hasDuplicate = checkDuplicates();
        full = checkFull();
    }

    public boolean isEdgePiece(){
        return edgePiece;
    }

    public List<Character> getStickers() {
        return stickers;
    }

    /**
     * Sets the colour of a sticker on the piece if validity check is satisfied
     * @param newSticker **Colour to be added**
     * @param location **Location to be added to**
     * @return validity of new piece
     */
    public boolean setSticker(Character newSticker, int location){
        if(location > stickers.size() || stickers.get(location) != 'X')
            return false;

        if(edgePiece) {
            if (stickers.get(1 - location).equals(newSticker) || ColourDetector.isOpposite(stickers.get(1 - location), newSticker))
                return false;
        }
        else
            for(int i =0; i<3 ;i++)
                if(location != i && (stickers.get(i).equals(newSticker) || ColourDetector.isOpposite(stickers.get(i), newSticker)) )
                        return false;


        stickers.set(location, newSticker);
        hasDuplicate = checkDuplicates();
        full = checkFull();
        return true;
    }

    public boolean equals(CubePiece b){
        List<Character> bS = b.getStickers();

        if(b.isEdgePiece() != edgePiece)
            return false;

        if(edgePiece)
            return bS.contains(stickers.get(0)) && bS.contains(stickers.get(1));
        else
            return bS.contains(stickers.get(0)) && bS.contains(stickers.get(1)) && bS.contains(stickers.get(2));

    }

    public boolean hasDuplicate(){
        return hasDuplicate;
    }

    public boolean isFull(){
        return full;
    }

    private boolean checkDuplicates(){
        if(edgePiece)
            return stickers.get(0) == stickers.get(1);
        else
            return stickers.get(0) == stickers.get(1) || stickers.get(0) == stickers.get(2) || stickers.get(1) == stickers.get(2);
    }

    private boolean checkFull(){
        if(edgePiece)
            return stickers.get(0) != 'X' && stickers.get(1) != 'X';
        else
            return stickers.get(0) != 'X' && stickers.get(1) != 'X' && stickers.get(2) != 'X';
    }

    public CubePiece clone(){
        if(stickers.size() == 2)
            return new CubePiece(stickers.get(0), stickers.get(1));
        else
            return new CubePiece(stickers.get(0), stickers.get(1), stickers.get(2));
    }

    public void randomise(){
        Collections.shuffle(stickers);
    }
}
