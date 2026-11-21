package net.thenextlvl.protect.area;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.thenextlvl.nbt.serialization.TagSerializable;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.protect.flag.FlagProvider;
import org.bukkit.Server;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public interface Area extends Container, FlagProvider, Comparable<Area>, TagSerializable<CompoundTag>, DataContainer {
   @Contract(" -> new")
   LinkedHashSet<Area> getParents();

   @Contract(
      pure = true
   )
   Optional<Area> getParent();

   @Contract(
      pure = true
   )
   Optional<UUID> getOwner();

   @Contract(
      pure = true
   )
   Server getServer();

   @Contract(
      pure = true
   )
   @Unmodifiable
   Set<UUID> getMembers();

   @Contract(
      pure = true
   )
   String getName();

   @Contract(
      pure = true
   )
   World getWorld();

   @Deprecated(
      forRemoval = true,
      since = "3.4.0"
   )
   @Contract(
      pure = true
   )
   default File getDataFolder() {
      return this.getDataPath().toFile();
   }

   @Contract(
      pure = true
   )
   Path getDataPath();

   @Deprecated(
      forRemoval = true,
      since = "3.4.0"
   )
   @Contract(
      pure = true
   )
   default File getFile() {
      return this.getDataFile().toFile();
   }

   @Contract(
      pure = true
   )
   Path getDataFile();

   @Deprecated(
      forRemoval = true,
      since = "3.4.0"
   )
   @Contract(
      pure = true
   )
   default File getFallbackFile() {
      return this.getBackupFile().toFile();
   }

   @Contract(
      pure = true
   )
   Path getBackupFile();

   @Contract(
      mutates = "this"
   )
   boolean addMember(UUID var1);

   @Contract(
      pure = true
   )
   boolean canInteract(Area var1);

   @Contract(
      pure = true
   )
   boolean isMember(UUID var1);

   @Contract(
      pure = true
   )
   boolean isPermitted(UUID var1);

   @Contract(
      mutates = "this"
   )
   boolean removeMember(UUID var1);

   @Contract(
      mutates = "this"
   )
   boolean setOwner(@Nullable UUID var1);

   @Contract(
      mutates = "this"
   )
   boolean setPriority(int var1);

   @Contract(
      pure = true
   )
   int getPriority();

   @Contract(
      mutates = "this"
   )
   void setMembers(Set<UUID> var1);
}
