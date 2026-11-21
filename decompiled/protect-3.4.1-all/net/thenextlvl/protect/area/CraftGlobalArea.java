package net.thenextlvl.protect.area;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.protect.ProtectPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CraftGlobalArea extends CraftArea implements GlobalArea {
   private final Path dataFolder = ProtectPlugin.getDataFolder(this.getWorld());
   private final Path fallbackFile = this.dataFolder.resolve(this.getName() + ".dat_old");
   private final Path file = this.dataFolder.resolve(this.getName() + ".dat");

   public CraftGlobalArea(ProtectPlugin plugin, World world) {
      super(plugin, "@" + world.getName(), world, Set.of(), null, Map.of(), -1);
   }

   public CraftGlobalArea(ProtectPlugin plugin, World world, CompoundTag tag) {
      super(plugin, world, "@" + world.getName(), tag);
   }

   @Override
   public Optional<Area> getParent() {
      return Optional.empty();
   }

   @Override
   public Path getDataPath() {
      return this.dataFolder;
   }

   @Override
   public Path getDataFile() {
      return this.file;
   }

   @Override
   public Path getBackupFile() {
      return this.fallbackFile;
   }

   @Override
   public boolean canInteract(Area area) {
      return this.equals(area) || area.getParent().map(this::equals).orElse(false);
   }

   @Override
   public List<Entity> getEntities() {
      return this.getWorld().getEntities();
   }

   @Override
   public List<Player> getPlayers() {
      return this.getWorld().getPlayers();
   }

   @Override
   public List<Entity> getHighestEntities() {
      return this.getEntities();
   }

   @Override
   public List<Player> getHighestPlayers() {
      return this.getPlayers();
   }

   @Override
   public boolean contains(Location location) {
      return this.getWorld().equals(location.getWorld());
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         CraftGlobalArea area = (CraftGlobalArea)o;
         return Objects.equals(this.getWorld(), area.getWorld());
      }
   }

   @Override
   public int hashCode() {
      return this.getWorld().hashCode();
   }

   @Override
   public String toString() {
      return "CraftGlobalArea{world=" + this.getWorld() + "}";
   }
}
