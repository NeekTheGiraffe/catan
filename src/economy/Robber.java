package economy;

import board.Hex;

public class Robber {

    private Hex hex;

    public Robber(Hex hex) {
        this.hex = hex;
    }

    public Hex getHex() {
        return hex;
    }

    public void moveTo(Hex h) {
        this.hex = h;
    }
}
