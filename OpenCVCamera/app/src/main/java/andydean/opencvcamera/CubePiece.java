package andydean.opencvcamera;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 04/04/2017.
 */

public class CubePiece {

    private List<Character> stickers = new ArrayList<>();
    public CubePiece(char a, char b, char c) {
        stickers.add(a);
        stickers.add(b);
        stickers.add(c);
    }

    public CubePiece(char a, char b) {
        stickers.add(a);
        stickers.add(b);
    }

    public List<Character> getStickers() {
        return stickers;
    }

    public boolean setSticker(Character newSticker, int location){
        for(int i=0; i<location; i++)
            if(stickers.get(i).equals(newSticker))
                return false;
        if(location > stickers.size() || stickers.get(location) != 'X')
            return false;
        stickers.set(location, newSticker);
        return true;
    }

    public boolean equals(CubePiece b){
        List<Character> bS = b.getStickers();
        boolean match = true;
        ArrayList<Character> temp = new ArrayList<>();
        temp.addAll(stickers);
        for(char c : bS) {
            if(temp.contains(c)) {
                int i = temp.indexOf(c);
                temp.remove(i);
            }
            else
                match = false;
        }
        return match && temp.isEmpty();
    }

    public boolean hasDuplicate(){
        if(stickers.size() == 3)
            return stickers.get(0) == stickers.get(1) || stickers.get(0) == stickers.get(2) || stickers.get(1) == stickers.get(2);
        else
            return stickers.get(0) == stickers.get(1);
    }

    public boolean hasBlank(){
        for(Character c : stickers)
            if(c == 'X')
                return true;

        return false;
    }

    public CubePiece clone(){
        if(stickers.size() == 2)
            return new CubePiece(stickers.get(0), stickers.get(1));
        else
            return new CubePiece(stickers.get(0), stickers.get(1), stickers.get(2));
    }
}
