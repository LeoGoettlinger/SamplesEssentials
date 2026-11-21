package de.tr7zw.nbtapi.iface;

import java.io.File;
import java.io.IOException;

public interface NBTFileHandle extends ReadWriteNBT {
   void save() throws IOException;

   File getFile();
}
