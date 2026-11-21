package net.thenextlvl.protect.schematic;

import com.sk89q.worldedit.WorldEditException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.Contract;

public interface SchematicHolder {
   @Deprecated(
      forRemoval = true,
      since = "3.4.0"
   )
   @Contract(
      pure = true
   )
   default File getSchematic() {
      return this.getSchematicFile().toFile();
   }

   @Contract(
      pure = true
   )
   Path getSchematicFile();

   @Contract(
      mutates = "io"
   )
   boolean deleteSchematic();

   @Contract(
      mutates = "io"
   )
   void saveSchematic() throws IOException, WorldEditException;

   boolean loadSchematic() throws IOException, WorldEditException;
}
