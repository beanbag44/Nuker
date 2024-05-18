package me.beanbag.utils;

public class RotationsManager {
    static int rotators = 0;
    public static void rotate(int yaw, int pitch) {
        rotators++;
    }
}
