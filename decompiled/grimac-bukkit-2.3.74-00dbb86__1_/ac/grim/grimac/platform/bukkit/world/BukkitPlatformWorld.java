package ac.grim.grimac.platform.bukkit.world;

import ac.grim.grimac.platform.api.world.PlatformChunk;
import ac.grim.grimac.platform.api.world.PlatformWorld;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.util.SpigotConversionUtil;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

public record BukkitPlatformWorld(@NotNull World bukkitWorld) implements PlatformWorld {
   private static final boolean LEGACY_SERVER_VERSION = PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2);

   @Override
   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      return this.bukkitWorld.isChunkLoaded(chunkX, chunkZ);
   }

   @Override
   public WrappedBlockState getBlockAt(int x, int y, int z) {
      if (LEGACY_SERVER_VERSION) {
         Block block = this.bukkitWorld.getBlockAt(x, y, z);
         int blockId = block.getType().getId() << 4 | block.getData();
         return WrappedBlockState.getByGlobalId(blockId);
      } else {
         return BukkitPlatformChunk.isIllegalY(this.bukkitWorld, y)
            ? WrappedBlockState.getDefaultState(StateTypes.AIR)
            : SpigotConversionUtil.fromBukkitBlockData(this.bukkitWorld.getBlockAt(x, y, z).getBlockData());
      }
   }

   @Override
   public String getName() {
      return this.bukkitWorld.getName();
   }

   @NotNull
   @Override
   public UUID getUID() {
      return this.bukkitWorld.getUID();
   }

   @Override
   public PlatformChunk getChunkAt(int currChunkX, int currChunkZ) {
      return new BukkitPlatformChunk(this.bukkitWorld.getChunkAt(currChunkX, currChunkZ));
   }

   @Override
   public boolean isLoaded() {
      return Bukkit.getWorld(this.bukkitWorld.getUID()) != null;
   }
}
