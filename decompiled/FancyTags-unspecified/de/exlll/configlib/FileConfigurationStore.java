package de.exlll.configlib;

import java.nio.file.Path;

public interface FileConfigurationStore<T> {
   void save(T var1, Path var2);

   T load(Path var1);

   T update(Path var1);
}
