package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.Closeable;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;

public interface ClipboardReader extends Closeable {
   default Clipboard read() throws IOException {
      return this.read(UUID.randomUUID());
   }

   default OptionalInt getDataVersion() {
      return OptionalInt.empty();
   }

   default Clipboard read(UUID uuid) throws IOException {
      return this.read(uuid, dimensions -> Clipboard.create(new CuboidRegion(null, BlockVector3.ZERO, dimensions.subtract(BlockVector3.ONE), false), uuid));
   }

   default Clipboard read(UUID uuid, Function<BlockVector3, Clipboard> createOutput) throws IOException {
      return this.read();
   }
}
