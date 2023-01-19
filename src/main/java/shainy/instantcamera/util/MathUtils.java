package shainy.instantcamera.util;

public class MathUtils {
    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        else if (value > max) return max;
        return value;
    }

    public static long clamp(long value, long min, long max) {
        if (value < min) return min;
        else if (value > max) return max;
        return value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        else if (value > max) return max;
        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        else if (value > max) return max;
        return value;
    }
}
