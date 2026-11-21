package su.samplecraft.essentials.modules.rtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import su.samplecraft.essentials.SamplesEssentials;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Shared random-teleport engine used by the three RTP modules.
 * <p>
 * Paper-safe by design: candidate chunks are loaded asynchronously with
 * {@link World#getChunkAtAsync(int, int, boolean)} and the safety check runs
 * on the main thread inside the chunk callback, so no block lookups ever
 * happen off-thread. The algorithm mirrors the original plugins: pick a
 * random angle + distance, retry a configurable number of times, never land
 * in liquid, on top of a dangerous block or outside the world border.
 */
public final class RandomTeleportEngine {

    private RandomTeleportEngine() {
    }

    public record Settings(
            int minDistance,
            int maxDistance,
            int maxAttempts,
            int borderMargin,
            boolean avoidLiquid,
            boolean avoidDangerous,
            int maxY) {
    }

    public enum Result {
        SUCCESS,
        NO_SAFE_SPOT,
        CANCELLED
    }

    public interface Callback {
        void done(Result result, Location location);
    }

    public static void teleport(SamplesEssentials plugin, Player player, Settings settings, Callback callback) {
        World world = player.getWorld();
        Location origin = player.getLocation();
        tryNext(plugin, player, world, origin, settings, 0, callback);
    }

    private static void tryNext(SamplesEssentials plugin, Player player, World world, Location origin,
                                Settings settings, int attempt, Callback callback) {
        if (!player.isOnline()) {
            callback.done(Result.CANCELLED, null);
            return;
        }
        if (attempt >= Math.max(1, settings.maxAttempts())) {
            callback.done(Result.NO_SAFE_SPOT, null);
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(Math.PI * 2);
        int distance = random.nextInt(Math.max(1, settings.minDistance()), Math.max(settings.minDistance() + 1, settings.maxDistance()));
        int x = (int) Math.round(origin.getX() + Math.cos(angle) * distance);
        int z = (int) Math.round(origin.getZ() + Math.sin(angle) * distance);

        if (!insideBorder(world, x, z, settings.borderMargin())) {
            tryNext(plugin, player, world, origin, settings, attempt + 1, callback);
            return;
        }

        java.util.concurrent.CompletableFuture<org.bukkit.Chunk> chunkFuture = world.getChunkAtAsync(x >> 4, z >> 4, true);
        chunkFuture.thenAccept(chunk -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                callback.done(Result.CANCELLED, null);
                return;
            }
            Location safe = findSafe(world, x, z, settings);
            if (safe == null) {
                tryNext(plugin, player, world, origin, settings, attempt + 1, callback);
                return;
            }
            player.teleportAsync(safe).thenAccept(success ->
                    callback.done(success ? Result.SUCCESS : Result.CANCELLED, safe));
        }));
    }

    private static boolean insideBorder(World world, int x, int z, int margin) {
        if (world.getWorldBorder() == null) {
            return true;
        }
        Location center = world.getWorldBorder().getCenter();
        double half = world.getWorldBorder().getSize() / 2.0 - Math.max(0, margin);
        return Math.abs(x - center.getX()) <= half && Math.abs(z - center.getZ()) <= half;
    }

    /** Runs on the main thread; returns null when the column is not a safe landing spot. */
    private static Location findSafe(World world, int x, int z, Settings settings) {
        int y = world.getHighestBlockYAt(x, z);
        if (y <= world.getMinHeight() + 2 || y > Math.min(world.getMaxHeight() - 3, settings.maxY())) {
            return null;
        }
        Material ground = world.getBlockAt(x, y, z).getType();
        Material above = world.getBlockAt(x, y + 1, z).getType();
        Material above2 = world.getBlockAt(x, y + 2, z).getType();
        if (!ground.isSolid() || ground == Material.MAGMA_BLOCK || ground == Material.CACTUS) {
            return null;
        }
        if (settings.avoidLiquid() && (ground == Material.WATER || ground == Material.LAVA
                || above == Material.WATER || above == Material.LAVA)) {
            return null;
        }
        if (settings.avoidDangerous() && isDangerous(ground)) {
            return null;
        }
        if (!above.isAir() || !above2.isAir()) {
            return null;
        }
        return new Location(world, x + 0.5, y + 1.0, z + 0.5,
                ThreadLocalRandom.current().nextFloat() * 360f, 0f);
    }

    private static boolean isDangerous(Material material) {
        return switch (material) {
            case FIRE, SOUL_FIRE, LAVA, MAGMA_BLOCK, CACTUS, SWEET_BERRY_BUSH,
                 POWDER_SNOW, CAMPFIRE, SOUL_CAMPFIRE -> true;
            default -> false;
        };
    }
}
