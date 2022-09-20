package jp.tengopapa.algo.solvers.hash;

public enum TableType {
    LINEAR("Lineáris próba", LinearHandler.class), SQUARE("Négyzetes próba", SquareHandler.class), DOUBLE("Kettős hasítás", DoubleHandler.class);

    public final String friendlyName;
    public final Class<? extends HashTableHandler> impl;

    TableType(String friendlyName, Class<? extends HashTableHandler> impl) {
        this.friendlyName = friendlyName;
        this.impl = impl;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}
