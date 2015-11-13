package services;

import java.util.Random;

public class RandomProviderImpl implements RandomProvider {
    private static Random random = new Random();

    @Override
    public int provideNextRandom(int bound) {
        return random.nextInt(bound);
    }
}
