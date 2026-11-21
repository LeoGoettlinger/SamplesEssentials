package su.samplecraft.essentials.modules.rtp;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Candidate coordinate strategies, ported 1:1 from
 * {@code com.skyblockexp.ezrtp.teleport.search.*} of EzRTP-3.4.3.jar.
 * <p>
 * Config value {@code search-pattern}: random (uniform), circle, square,
 * triangle, diamond.
 */
public enum SearchPattern {

    /** UniformSearchStrategy: random angle + uniform distance in [min,max]. */
    UNIFORM("random") {
        @Override
        public int[] candidate(int centerX, int centerZ, int minRadius, int maxRadius) {
            int radius = randomIntInclusive(minRadius, maxRadius);
            double angle = randomAngle();
            return new int[]{
                    centerX + (int) Math.round(Math.cos(angle) * radius),
                    centerZ + (int) Math.round(Math.sin(angle) * radius)};
        }
    },

    /** CircularSearchStrategy ("circle"): random angle, random radius. */
    CIRCLE("circle") {
        @Override
        public int[] candidate(int centerX, int centerZ, int minRadius, int maxRadius) {
            int effectiveMin = Math.min(minRadius, maxRadius);
            int effectiveMax = Math.max(minRadius, maxRadius);
            double radius = randomDouble() * (effectiveMax - effectiveMin) + effectiveMin;
            double angle = randomAngle();
            return new int[]{
                    centerX + (int) Math.round(Math.cos(angle) * radius),
                    centerZ + (int) Math.round(Math.sin(angle) * radius)};
        }
    },

    /** SquareSearchStrategy: a point on the edge of the square at a random radius. */
    SQUARE("square") {
        @Override
        public int[] candidate(int centerX, int centerZ, int minRadius, int maxRadius) {
            int effectiveMin = Math.min(minRadius, maxRadius);
            int effectiveMax = Math.max(minRadius, maxRadius);
            if (effectiveMax <= 0 && effectiveMin <= 0) {
                return new int[]{centerX, centerZ};
            }
            int radius = ThreadLocalRandom.current().nextInt(effectiveMax - effectiveMin + 1) + effectiveMin;
            int edge = ThreadLocalRandom.current().nextInt(4);
            int offset = ThreadLocalRandom.current().nextInt(radius * 2 + 1);
            int localX = 0;
            int localZ = 0;
            switch (edge) {
                case 0 -> {
                    localX = -radius + offset;
                    localZ = -radius;
                }
                case 1 -> {
                    localX = radius;
                    localZ = -radius + offset;
                }
                case 2 -> {
                    localX = radius - offset;
                    localZ = radius;
                }
                default -> {
                    localX = -radius;
                    localZ = radius - offset;
                }
            }
            return new int[]{centerX + localX, centerZ + localZ};
        }
    },

    /** TriangleSearchStrategy: a point on the edge of an equilateral triangle. */
    TRIANGLE("triangle") {
        @Override
        public int[] candidate(int centerX, int centerZ, int minRadius, int maxRadius) {
            int effectiveMin = Math.min(minRadius, maxRadius);
            int effectiveMax = Math.max(minRadius, maxRadius);
            if (effectiveMax <= 0 && effectiveMin <= 0) {
                return new int[]{centerX, centerZ};
            }
            int radius = effectiveMin == effectiveMax ? effectiveMin : randomIntInclusive(effectiveMin, effectiveMax);
            double[] vx = new double[3];
            double[] vz = new double[3];
            for (int i = 0; i < 3; i++) {
                double angle = (-Math.PI / 2) + i * 2.0 * Math.PI / 3.0;
                vx[i] = Math.cos(angle) * radius;
                vz[i] = Math.sin(angle) * radius;
            }
            int edge = ThreadLocalRandom.current().nextInt(3);
            int next = (edge + 1) % 3;
            double t = randomDouble();
            double px = (1.0 - t) * vx[edge] + t * vx[next];
            double pz = (1.0 - t) * vz[edge] + t * vz[next];
            return new int[]{centerX + (int) Math.round(px), centerZ + (int) Math.round(pz)};
        }
    },

    /** DiamondSearchStrategy: a point on the edge of a diamond (8 unit edges). */
    DIAMOND("diamond") {
        private static final double[] UNIT_X = {0.0, 0.5, 1.0, 0.5, 0.0, -0.5, -1.0, -0.5};
        private static final double[] UNIT_Z = {-1.0, -0.5, 0.0, 0.5, 1.0, 0.5, 0.0, -0.5};
        private static final double UNIT_EDGE_LENGTH = Math.hypot(0.5, 0.5);

        @Override
        public int[] candidate(int centerX, int centerZ, int minRadius, int maxRadius) {
            int effectiveMin = Math.min(minRadius, maxRadius);
            int effectiveMax = Math.max(minRadius, maxRadius);
            if (effectiveMax <= 0 && effectiveMin <= 0) {
                return new int[]{centerX, centerZ};
            }
            int radius = effectiveMin == effectiveMax ? effectiveMin : randomIntInclusive(effectiveMin, effectiveMax);
            if (radius <= 0) {
                return new int[]{centerX, centerZ};
            }
            int edgeSteps = Math.max(1, (int) Math.round(radius * UNIT_EDGE_LENGTH));
            int totalSteps = 8 * edgeSteps;
            int idx = ThreadLocalRandom.current().nextInt(totalSteps);
            int chosenEdge = idx / edgeSteps;
            int chosenStep = idx % edgeSteps;
            int nextEdge = (chosenEdge + 1) % 8;
            double x1 = UNIT_X[chosenEdge];
            double z1 = UNIT_Z[chosenEdge];
            double dx = UNIT_X[nextEdge] - x1;
            double dz = UNIT_Z[nextEdge] - z1;
            double t = (double) chosenStep / edgeSteps;
            return new int[]{
                    centerX + (int) Math.round((x1 + dx * t) * radius),
                    centerZ + (int) Math.round((z1 + dz * t) * radius)};
        }
    };

    private final String configKey;

    SearchPattern(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }

    public abstract int[] candidate(int centerX, int centerZ, int minRadius, int maxRadius);

    public static SearchPattern parse(String raw) {
        if (raw == null) {
            return UNIFORM;
        }
        for (SearchPattern pattern : values()) {
            if (pattern.configKey.equalsIgnoreCase(raw)) {
                return pattern;
            }
        }
        return UNIFORM;
    }

    static int randomIntInclusive(int min, int max) {
        return min >= max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    static double randomAngle() {
        return ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
    }

    static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
