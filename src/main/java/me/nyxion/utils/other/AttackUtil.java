package me.nyxion.utils.other;

import java.util.Random;

public class AttackUtil {
    private static final Random random = new Random();
    private static double lastRandom = 0;
    private static int patternIndex = 0;
    private static final double[] pattern1 = {1.1, 0.9, 1.0, 0.9, 1.1, 1.0};
    private static final double[] pattern2 = {0.8, 1.2, 1.0, 1.1, 0.9, 1.0};

    public static double getOldRandomization(int cps, float randomization) {
        return cps + (random.nextDouble() * 2.0 - 1.0) * randomization;
    }

    public static double getNewRandomization(int cps, float randomization) {
        double nextRandom = cps + (random.nextDouble() * 2.0 - 1.0) * randomization;
        double result = (nextRandom + lastRandom) / 2.0;
        lastRandom = nextRandom;
        return result;
    }

    public static double getExtraRandomization(int cps, float randomization) {
        double baseRandom = getNewRandomization(cps, randomization);
        return baseRandom * (0.95 + random.nextDouble() * 0.1);
    }

    public static double getPattern1Randomization(int cps, float randomization) {
        double result = cps * pattern1[patternIndex % pattern1.length];
        patternIndex++;
        return result + (random.nextDouble() * 2.0 - 1.0) * (randomization * 0.5);
    }

    public static double getPattern2Randomization(int cps, float randomization) {
        double result = cps * pattern2[patternIndex % pattern2.length];
        patternIndex++;
        return result + (random.nextDouble() * 2.0 - 1.0) * (randomization * 0.5);
    }
}