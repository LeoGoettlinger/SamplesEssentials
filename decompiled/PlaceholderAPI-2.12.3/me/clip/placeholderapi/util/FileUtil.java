package me.clip.placeholderapi.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileUtil {
   @Nullable
   public static <T> Class<? extends T> findClass(@NotNull File file, @NotNull Class<T> clazz) throws IOException, ClassNotFoundException {
      if (!file.exists()) {
         return null;
      } else {
         URL jar = file.toURI().toURL();
         URLClassLoader loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader());
         List<String> matches = new ArrayList<>();
         List<Class<? extends T>> classes = new ArrayList<>();
         JarInputStream stream = new JarInputStream(jar.openStream());

         try {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
               String name = entry.getName();
               if (!name.isEmpty() && name.endsWith(".class")) {
                  matches.add(name.substring(0, name.lastIndexOf(46)).replace('/', '.'));
               }
            }

            for (String match : matches) {
               try {
                  Class<?> loaded = loader.loadClass(match);
                  if (clazz.isAssignableFrom(loaded)) {
                     classes.add(loaded.asSubclass(clazz));
                  }
               } catch (NoClassDefFoundError var12) {
               }
            }
         } catch (Throwable var13) {
            try {
               stream.close();
            } catch (Throwable var11) {
               var13.addSuppressed(var11);
            }

            throw var13;
         }

         stream.close();
         if (classes.isEmpty()) {
            loader.close();
            return null;
         } else {
            return classes.get(0);
         }
      }
   }
}
