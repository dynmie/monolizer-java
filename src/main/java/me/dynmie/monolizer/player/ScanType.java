package me.dynmie.monolizer.player;

/**
 * @author dynmie
 */
public enum ScanType {
    PROGRESSIVE,
    INTERLACED,
    INTERLACED_NO_PREV;

    public ScanType next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
