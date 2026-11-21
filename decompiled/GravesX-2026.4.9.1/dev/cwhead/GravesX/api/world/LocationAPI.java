package dev.cwhead.GravesX.api.world;

import com.ranull.graves.Graves;
import com.ranull.graves.util.BlockFaceUtil;
import com.ranull.graves.util.LocationUtil;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

public class LocationAPI {
   private final Graves plugin;

   public LocationAPI(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public Location roundLocation(@NotNull Location location) {
      return LocationUtil.roundLocation(location);
   }

   public String locationToString(@NotNull Location location) {
      return LocationUtil.locationToString(location);
   }

   public String chunkToString(@NotNull Location location) {
      return LocationUtil.chunkToString(location);
   }

   public Location chunkStringToLocation(@NotNull String string) {
      return LocationUtil.chunkStringToLocation(string);
   }

   public Location stringToLocation(@NotNull String string) {
      return LocationUtil.stringToLocation(string);
   }

   public Location getClosestLocation(@NotNull Location locationBase, @NotNull List<Location> locationList) {
      return LocationUtil.getClosestLocation(locationBase, locationList);
   }

   public BlockFace simplifyBlockFace(@NotNull BlockFace face) {
      return BlockFaceUtil.getSimpleBlockFace(face);
   }

   public Rotation getRotationFromBlockFace(@NotNull BlockFace face) {
      return BlockFaceUtil.getBlockFaceRotation(face);
   }
}
