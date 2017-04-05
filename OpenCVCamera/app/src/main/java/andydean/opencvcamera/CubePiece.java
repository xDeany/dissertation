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
        for(char c : bS)
            match = match && stickers.contains(c);
        return match;
    }
}
