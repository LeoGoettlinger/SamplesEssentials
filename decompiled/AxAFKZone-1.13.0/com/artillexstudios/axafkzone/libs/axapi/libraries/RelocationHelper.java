package com.artillexstudios.axafkzone.libs.axapi.libraries;

import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class RelocationHelper {
   private final RelocationClassLoader classLoader;
   private final Class<?> clazz;

   public RelocationHelper(LibraryDownloader downloader) {
      URL[] urls = new URL[3];
      Library asm = new Library("org.ow2.asm", "asm", "9.10.1", "", List.of());
      Library asmCommons = new Library("org.ow2.asm", "asm-commons", "9.10.1", "", List.of());
      Library jarRelocator = new Library("me.lucko", "jar-relocator", "1.7", "", List.of());

      try {
         urls[1] = downloader.addLibrary(asmCommons).toFile().toURI().toURL();
         urls[0] = downloader.addLibrary(asm).toFile().toURI().toURL();
         urls[2] = downloader.addLibrary(jarRelocator).toFile().toURI().toURL();
      } catch (MalformedURLException var9) {
         var9.printStackTrace();
         throw new RuntimeException(var9);
      }

      this.classLoader = new RelocationClassLoader(urls);

      Class<?> clazz1;
      try {
         clazz1 = this.classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
      } catch (ClassNotFoundException var8) {
         var8.printStackTrace();
         clazz1 = null;
      }

      this.clazz = clazz1;
   }

   public void relocate(Path path, Path relocated, Map<String, String> relocations) {
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Relocating from: {} to: {} with relocations: {}", path, relocated, relocations);
      }

      try {
         Constructor<?> declaredConstructor = this.clazz.getDeclaredConstructor(File.class, File.class, Map.class);
         declaredConstructor.setAccessible(true);
         Object object = declaredConstructor.newInstance(path.toFile(), relocated.toFile(), relocations);
         Method run = this.clazz.getDeclaredMethod("run");
         run.setAccessible(true);
         run.invoke(object);
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException var7) {
         LogUtils.error("An exception occurred while relocating!", var7);
         throw new RuntimeException(var7);
      }
   }
}
