package net.thenextlvl.protect.area;

import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.NBTInputStream;
import net.thenextlvl.nbt.NBTOutputStream;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.event.AreaLoadEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CraftAreaProvider implements AreaProvider {
   private final Map<World, Set<Area>> areas = new HashMap<>();
   private final ProtectPlugin plugin;

   public CraftAreaProvider(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public Stream<Area> getAreas() {
      return this.areas.values().stream().flatMap(Collection::stream);
   }

   @Override
   public Stream<Area> getAreas(World world) {
      Set<Area> areas = this.areas.get(world);
      return areas != null ? areas.stream() : Stream.empty();
   }

   @Override
   public Stream<Area> getAreas(Location location) {
      return this.getAreas(location.getWorld()).filter(area -> area.contains(location));
   }

   @Override
   public Area getArea(Location location) {
      return this.getAreas(location).max(Comparator.comparingInt(Area::getPriority)).orElseGet(() -> this.getArea(location.getWorld()));
   }

   @Override
   public Optional<Area> getArea(String name) {
      return this.getAreas().filter(area -> area.getName().equals(name)).findAny();
   }

   @Override
   public GlobalArea getArea(World world) {
      return this.getAreas(world)
         .filter(area -> area instanceof GlobalArea)
         .map(area -> (GlobalArea)area)
         .findAny()
         .orElseThrow(() -> new IllegalStateException("No global area found for " + world.key().asString()));
   }

   public void load(World world) {
      Path areaFolder = ProtectPlugin.getDataFolder(world);

      try {
         try (Stream<Path> files = !Files.isDirectory(areaFolder)
               ? Stream.empty()
               : Files.list(areaFolder).filter(path -> path.getFileName().toString().endsWith(".dat"))) {
            files.forEach(file -> this.load(world, file));
            if (!this.getAreas(world).anyMatch(area -> area instanceof GlobalArea)) {
               this.persist(new CraftGlobalArea(this.plugin, world));
               return;
            }
         }
      } catch (Exception var8) {
         this.plugin.getComponentLogger().error("Failed to load areas for {}", world.key().asString(), var8);
         this.plugin
            .getComponentLogger()
            .error(
               "Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml"
            );
         ProtectPlugin.ERROR_TRACKER.trackError(var8);
      }
   }

   private void load(World world, Path file) {
      try {
         Area loaded = this.read(world, null, file);
         new AreaLoadEvent(loaded).callEvent();
         this.memoize(world, loaded);
      } catch (EOFException var4) {
         this.plugin.getComponentLogger().error("The area file {} is irrecoverably broken", file);
      } catch (Exception var5) {
         this.plugin.getComponentLogger().error("Failed to load area from {}", file, var5);
         this.plugin
            .getComponentLogger()
            .error(
               "Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml"
            );
         ProtectPlugin.ERROR_TRACKER.trackError(var5);
      }
   }

   public void persist(Area area) throws IOException {
      this.memoize(area.getWorld(), this.read(area.getWorld(), area, area.getDataFile()));
   }

   private void memoize(World world, Area area) {
      Set<Area> areas = this.areas.computeIfAbsent(world, k -> new HashSet<>());
      areas.stream()
         .filter(area::equals)
         .findAny()
         .ifPresentOrElse(
            io -> this.plugin.getComponentLogger().warn("Ignoring duplicate area {}: {}", area.getName(), area.getDataFile()), () -> areas.add(area)
         );
   }

   private Area read(World world, @Nullable Area area, Path file) throws IOException {
      if (area == null) {
         return this.read(file, world);
      } else if (Files.isRegularFile(file)) {
         return this.read(file, area);
      } else {
         this.save(area);
         return area;
      }
   }

   private Area read(Path file, World world) throws IOException {
      try {
         Area var13;
         try (NBTInputStream inputStream = NBTInputStream.create(file)) {
            var13 = this.read(world, inputStream);
         }

         return var13;
      } catch (Exception var12) {
         Path backup = file.resolveSibling(file.getFileName() + "_old");
         if (!Files.isRegularFile(backup)) {
            throw var12;
         } else {
            this.plugin.getComponentLogger().warn("Failed to load area from {}", file);
            this.plugin.getComponentLogger().warn("Falling back to {}", backup);

            Area var6;
            try (NBTInputStream inputStream = NBTInputStream.create(backup)) {
               var6 = this.read(world, inputStream);
            }

            return var6;
         }
      }
   }

   private Area read(World world, NBTInputStream inputStream) throws IOException {
      Entry<String, CompoundTag> entry = inputStream.readNamedTag();
      CompoundTag tag = entry.getValue();
      String name = entry.getKey();
      Key type = this.plugin.nbt.deserialize(tag.get("type"), Key.class);
      return this.plugin.areaService().getAdapter(type).construct(world, name, tag);
   }

   private Area read(Path file, Area area) throws IOException {
      Area var4;
      try (NBTInputStream inputStream = NBTInputStream.create(file)) {
         area.deserialize(inputStream.readTag());
         var4 = area;
      }

      return var4;
   }

   public void save(World world) {
      Set<Area> areas = this.areas.get(world);
      if (areas != null) {
         areas.forEach(this::save);
      }
   }

   private void save(Area area) {
      try {
         Path file = area.getDataFile();
         if (Files.isRegularFile(file)) {
            Files.move(file, area.getBackupFile(), StandardCopyOption.REPLACE_EXISTING);
         } else {
            Files.createDirectories(file.toAbsolutePath().getParent());
         }

         try (NBTOutputStream outputStream = NBTOutputStream.create(file)) {
            outputStream.writeTag(area.getName(), area.serialize());
         }
      } catch (Exception var8) {
         this.plugin.getComponentLogger().error("Failed to save area {}", area.getName(), var8);
         this.plugin
            .getComponentLogger()
            .error(
               "Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml"
            );
         ProtectPlugin.ERROR_TRACKER.trackError(var8);
      }
   }

   public void unload(World world) {
      this.areas.remove(world);
   }

   public boolean delete(Area area) {
      try {
         if (!(Files.deleteIfExists(area.getDataFile()) | Files.deleteIfExists(area.getBackupFile()))) {
            return false;
         } else {
            this.areas.computeIfPresent(area.getWorld(), (world, areas) -> {
               areas.remove(area);
               return areas.isEmpty() ? null : areas;
            });
            return true;
         }
      } catch (IOException var3) {
         this.plugin.getComponentLogger().warn("Failed to delete area {}", area.getName());
         this.plugin
            .getComponentLogger()
            .warn("Please look for similar issues or report this on GitHub: {}", "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml");
         ProtectPlugin.ERROR_TRACKER.trackError(var3);
         return false;
      }
   }
}
