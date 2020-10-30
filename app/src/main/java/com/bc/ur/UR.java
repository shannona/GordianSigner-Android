package com.bc.ur;

public class UR {

    public static UR create(int len, String seed) {
        return BCUR.UR_new(len, seed);
    }

    private final String type;

    private final byte[] cbor;

    public UR(String type, byte[] cbor) {
        this.type = type;
        this.cbor = cbor;
    }

    public byte[] getCbor() {
        return cbor;
    }

    public String getType() {
        return type;
    }
}
