package me.nyxion.utils.other;

public class MathUtil {
    public static double interpolate(double start, double end, double percent) {
        return start + (end - start) * percent;
    }

    public static float interpolate(float start, float end, float percent) {
        return start + (end - start) * percent;
    }

    public static double roundToPlace(double value, int places) {
        double multiplier = Math.pow(10, places);
        return Math.round(value * multiplier) / multiplier;
    }

    public static float roundToPlace(float value, int places) {
        float multiplier = (float) Math.pow(10, places);
        return Math.round(value * multiplier) / multiplier;
    }
}