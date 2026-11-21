package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ClipboardWriter extends Closeable {
   void write(Clipboard clipboard) throws IOException;

   default Tag writeVector(Vector3 vector) {
      List<DoubleTag> list = new ArrayList<>();
      list.add(new DoubleTag(vector.x()));
      list.add(new DoubleTag(vector.y()));
      list.add(new DoubleTag(vector.z()));
      return new ListTag(DoubleTag.class, list);
   }

   default Tag writeRotation(Location location) {
      List<FloatTag> list = new ArrayList<>();
      list.add(new FloatTag(location.getYaw()));
      list.add(new FloatTag(location.getPitch()));
      return new ListTag(FloatTag.class, list);
   }
}
