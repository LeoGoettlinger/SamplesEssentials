package net.thenextlvl.protect.area;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.event.inheritance.AreaParentChangeEvent;
import net.thenextlvl.protect.area.event.region.AreaRedefineEvent;
import net.thenextlvl.protect.area.event.schematic.AreaSchematicDeleteEvent;
import net.thenextlvl.protect.area.event.schematic.AreaSchematicLoadEvent;
import net.thenextlvl.protect.area.event.schematic.AreaSchematicLoadedEvent;
import net.thenextlvl.protect.exception.CircularInheritanceException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class CraftRegionizedArea<T extends Region> extends CraftArea implements RegionizedArea<T> {
   private final Path dataPath = ProtectPlugin.getDataFolder(this.getWorld());
   private final Path backupFile = this.getDataPath().resolve(this.getName() + ".dat_old");
   private final Path dataFile = this.getDataPath().resolve(this.getName() + ".dat");
   private final Path schematic = this.plugin.schematicFolder().resolve(this.getName() + ".schem");
   @Nullable
   private String parent;
   private T region;

   public CraftRegionizedArea(ProtectPlugin plugin, AreaCreator<T> creator) throws CircularInheritanceException {
      super(plugin, creator.name(), creator.world(), creator.members(), creator.owner(), creator.flags(), creator.priority());
      this.parent = creator.parent();
      this.region = creator.region();
   }

   public CraftRegionizedArea(ProtectPlugin plugin, World world, String name, CompoundTag tag) {
      super(plugin, world, name, tag);
      this.parent = this.readParent(tag);
      this.region = this.readRegion(tag);
   }

   protected abstract T readRegion(CompoundTag var1);

   @Override
   public Location getLocation() {
      Vector3 center = this.getRegion().getCenter();
      Location location = new Location(this.getWorld(), center.x() + 0.5, this.getRegion().getMinimumY(), center.z() + 0.5);
      if (this.getRegion().getLength() < this.getRegion().getWidth()) {
         location.setZ(location.getZ() - this.getRegion().getLength() / 2.0 - 0.5);
      } else {
         location.setX(location.getX() - this.getRegion().getWidth() / 2.0 - 0.5);
         location.setYaw(-90.0F);
      }

      location.setY(this.getSafeHeight(location.getBlock()));
      Block block = location.getBlock();
      BoundingBox box = block.getCollisionShape().getBoundingBoxes().stream().max(Comparator.comparingDouble(BoundingBox::getMaxY)).orElse(null);
      location.setY(box != null ? block.getY() + box.getMaxY() : block.getY());
      return location;
   }

   private int getSafeHeight(Block block) {
      if (block.isPassable()) {
         for (int y = block.getY(); y >= block.getWorld().getMinHeight(); y--) {
            if (!block.getWorld().getBlockAt(block.getX(), y, block.getZ()).isPassable()) {
               return y + 1;
            }
         }
      } else {
         for (int yx = block.getY(); yx <= block.getWorld().getMaxHeight(); yx++) {
            if (block.getWorld().getBlockAt(block.getX(), yx, block.getZ()).isPassable()) {
               return yx;
            }
         }
      }

      return block.getWorld().getHighestBlockYAt(block.getX(), block.getZ());
   }

   @Override
   public Optional<Area> getParent() {
      return this.parent().flatMap(this.plugin.areaProvider()::getArea);
   }

   @Override
   public Path getDataPath() {
      return this.dataPath;
   }

   @Override
   public Path getDataFile() {
      return this.dataFile;
   }

   @Override
   public Path getBackupFile() {
      return this.backupFile;
   }

   public Optional<String> parent() {
      return Optional.ofNullable(this.parent);
   }

   @Override
   public RegionSelector getRegionSelector() {
      return new CuboidRegionSelector(this.getRegion().getWorld(), this.getRegion().getMinimumPoint(), this.getRegion().getMaximumPoint());
   }

   @Override
   public T getRegion() {
      return this.region;
   }

   @Override
   public boolean setParent(@Nullable Area parent) throws CircularInheritanceException {
      if (parent != null && parent.getName().equals(this.parent)) {
         return false;
      } else {
         if (parent != null) {
            this.checkCircularInheritance(parent);
         }

         AreaParentChangeEvent event = new AreaParentChangeEvent(this, parent);
         if (!event.callEvent()) {
            return false;
         } else {
            if (!Objects.equals(parent, event.getParent()) && event.getParent() != null) {
               this.checkCircularInheritance(event.getParent());
            }

            this.parent = event.getParent() != null ? event.getParent().getName() : null;
            return true;
         }
      }
   }

   public void checkCircularInheritance(Area parent) throws CircularInheritanceException {
      LinkedList<Area> path = new LinkedList<>();
      path.add(this);
      path.add(parent);

      while (parent != this) {
         parent = parent.getParent().orElse(null);
         if (parent != null) {
            path.add(parent);
         }

         if (parent == null) {
            return;
         }
      }

      throw new CircularInheritanceException("Circular inheritance detected: " + path.stream().map(Area::getName).collect(Collectors.joining(" -> ")));
   }

   @Override
   public boolean setRegion(T region) {
      AreaRedefineEvent<T> event = new AreaRedefineEvent<>(this, (T)region.clone());
      if (event.callEvent()) {
         this.region = event.getRegion();
      }

      return !event.isCancelled();
   }

   @Override
   public boolean canInteract(Area area) {
      return this.equals(area)
         || area.getParent().map(this::equals).orElse(false)
         || this.getOwner().map(owner -> area instanceof RegionizedArea<?> regionized && regionized.getOwner().map(owner::equals).orElse(false)).orElse(false);
   }

   @Override
   public boolean isTooBig() {
      return this.getRegion().getVolume() >= 1000000000L;
   }

   @Override
   public boolean contains(Location location) {
      return this.getWorld().equals(location.getWorld()) && this.getRegion().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
   }

   @Override
   public Path getSchematicFile() {
      return this.schematic;
   }

   @Override
   public boolean deleteSchematic() {
      try {
         return Files.isRegularFile(this.getSchematicFile()) && new AreaSchematicDeleteEvent(this).callEvent() && Files.deleteIfExists(this.getSchematicFile());
      } catch (IOException var2) {
         this.plugin.getComponentLogger().warn("Failed to delete schematic for area {}", this.getName(), var2);
         this.plugin
            .getComponentLogger()
            .warn("Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml");
         ProtectPlugin.ERROR_TRACKER.trackError(var2);
         return false;
      }
   }

   @Override
   public void saveSchematic() throws IOException, WorldEditException {
      Files.createDirectories(this.schematic.toAbsolutePath().getParent());
      EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(this.getWorld()));

      try {
         ClipboardWriter writer = BuiltInClipboardFormat.FAST_V3
            .getWriter(Files.newOutputStream(this.schematic, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

         try {
            BlockArrayClipboard clipboard = new BlockArrayClipboard(this.getRegion());
            ForwardExtentCopy extent = new ForwardExtentCopy(editSession, this.getRegion(), clipboard, this.getRegion().getMinimumPoint());
            extent.setCopyingBiomes(this.getRegion() instanceof FlatRegion);
            extent.setCopyingEntities(true);
            Operations.complete(extent);
            writer.write(clipboard);
         } catch (Throwable var7) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (Throwable var8) {
         if (editSession != null) {
            try {
               editSession.close();
            } catch (Throwable var5) {
               var8.addSuppressed(var5);
            }
         }

         throw var8;
      }

      if (editSession != null) {
         editSession.close();
      }
   }

   @Override
   public boolean loadSchematic() throws IOException, WorldEditException {
      if (!Files.isRegularFile(this.getSchematicFile())) {
         return false;
      } else {
         AreaSchematicLoadEvent event = new AreaSchematicLoadEvent(this);
         if (!event.callEvent()) {
            return false;
         } else {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(this.getWorld());
            EditSession editSession = WorldEdit.getInstance().newEditSession(world);

            boolean var6;
            try {
               Clipboard clipboard = BuiltInClipboardFormat.FAST_V3.getReader(Files.newInputStream(this.getSchematicFile())).read();
               world.getEntities(this.getRegion()).forEach(Entity::remove);
               Operation operation = new ClipboardHolder(clipboard)
                  .createPaste(editSession)
                  .to(this.getRegion().getMinimumPoint())
                  .copyBiomes(true)
                  .copyEntities(true)
                  .ignoreAirBlocks(false)
                  .build();
               Operations.complete(operation);
               var6 = new AreaSchematicLoadedEvent(this).callEvent();
            } catch (Throwable var8) {
               if (editSession != null) {
                  try {
                     editSession.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (editSession != null) {
               editSession.close();
            }

            return var6;
         }
      }
   }

   @Override
   public List<org.bukkit.entity.Entity> getEntities() {
      return this.getWorld().getEntities().stream().filter(this::contains).collect(Collectors.toCollection(ArrayList::new));
   }

   @Override
   public List<Player> getPlayers() {
      return this.getWorld().getPlayers().stream().filter(this::contains).collect(Collectors.toCollection(ArrayList::new));
   }

   @Override
   public List<org.bukkit.entity.Entity> getHighestEntities() {
      return this.getWorld()
         .getEntities()
         .stream()
         .filter(player -> this.plugin.areaProvider().getArea(player).equals(this))
         .collect(Collectors.toCollection(ArrayList::new));
   }

   @Override
   public List<Player> getHighestPlayers() {
      return this.getWorld()
         .getPlayers()
         .stream()
         .filter(player -> this.plugin.areaProvider().getArea(player).equals(this))
         .collect(Collectors.toCollection(ArrayList::new));
   }

   @Override
   public CompoundTag serialize() {
      CompoundTag.Builder tag = super.serialize().toBuilder();
      if (this.parent != null) {
         tag.put("parent", this.parent);
      }

      tag.put("region", this.plugin.nbt.serialize(this.region));
      return tag.build();
   }

   @Nullable
   private String readParent(CompoundTag tag) {
      return tag.<Tag>optional("parent").map(Tag::getAsString).orElse(null);
   }
}
