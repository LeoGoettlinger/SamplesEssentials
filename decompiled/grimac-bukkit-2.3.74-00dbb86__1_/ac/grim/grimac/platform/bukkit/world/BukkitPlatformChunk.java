package ac.grim.grimac.platform.bukkit.world;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.api.Platform;
import ac.grim.grimac.platform.api.world.PlatformChunk;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BukkitPlatformChunk implements PlatformChunk {
   private static final boolean CUSTOMIZABLE_WORLD_HEIGHT = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17);
   private static final Map<BlockData, Integer> blockDataToId = (Map<BlockData, Integer>)(GrimAPI.INSTANCE.getPlatform() == Platform.FOLIA
      ? new ConcurrentHashMap<>()
      : new HashMap<>());
   private static final boolean isFlat = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13);
   @NotNull
   private final Chunk chunk;

   @Override
   public int getBlockID(int x, int y, int z) {
      if (isIllegalPosition(this.chunk.getWorld(), x, y, z)) {
         return WrappedBlockState.getDefaultState(StateTypes.AIR).getGlobalId();
      } else {
         Block block = this.chunk.getBlock(x, y, z);
         return isFlat
            ? blockDataToId.computeIfAbsent(
               block.getBlockData(),
               data -> WrappedBlockState.getByString(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), data.getAsString(false))
                  .getGlobalId()
            )
            : getLegacyBlockID(block);
      }
   }

   private static int getLegacyBlockID(@NotNull Block block) {
      return block.getType().getId() << 4 | block.getData();
   }

   public static boolean isIllegalY(@NotNull World world, int y) {
      int minY = CUSTOMIZABLE_WORLD_HEIGHT ? world.getMinHeight() : 0;
      int maxY = CUSTOMIZABLE_WORLD_HEIGHT ? world.getMaxHeight() : 255;
      return minY > y || y > maxY;
   }

   public static boolean isIllegalPosition(World world, int x, int y, int z) {
      return isIllegalY(world, y) || x < 0 || x > 15 || z < 0 || z > 15;
   }

   @Generated
   public BukkitPlatformChunk(@NotNull Chunk chunk) {
      if (chunk == null) {
         throw new NullPointerException("chunk is marked non-null but is null");
      } else {
         this.chunk = chunk;
      }
   }
}
