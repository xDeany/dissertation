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
        return match;
    }

    public boolean hasDuplicate(){
        if(stickers.size() == 3)
            return stickers.get(0) == stickers.get(1) || stickers.get(0) == stickers.get(2) || stickers.get(1) == stickers.get(2);
        else
            return stickers.get(0) == stickers.get(1);
    }
}
