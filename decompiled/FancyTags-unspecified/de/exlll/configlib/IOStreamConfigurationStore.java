package de.exlll.configlib;

import java.io.InputStream;
import java.io.OutputStream;

public interface IOStreamConfigurationStore<T> {
   void write(T var1, OutputStream var2);

   T read(InputStream var1);
}
