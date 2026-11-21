package me.lucko.luckperms.common.loader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class JarInJarClassLoader extends URLClassLoader {
   private List<String> priorityPackagePrefixes = null;

   public JarInJarClassLoader(ClassLoader loaderClassLoader, String jarResourcePath) throws LoadingException {
      super(new URL[]{extractJar(loaderClassLoader, jarResourcePath)}, loaderClassLoader);
   }

   public void addJarToClasspath(URL url) {
      this.addURL(url);
   }

   public void setPriorityPackagePrefixes(List<String> priorityPackagePrefixes) {
      this.priorityPackagePrefixes = priorityPackagePrefixes;
   }

   public void deleteJarResource() {
      URL[] urls = this.getURLs();
      if (urls.length != 0) {
         try {
            Path path = Paths.get(urls[0].toURI());
            Files.deleteIfExists(path);
         } catch (Exception var3) {
         }
      }
   }

   public <T> LoaderBootstrap instantiatePlugin(String bootstrapClass, Class<T> loaderPluginType, T loaderPlugin) throws LoadingException {
      Class<? extends LoaderBootstrap> plugin;
      try {
         plugin = this.loadClass(bootstrapClass).asSubclass(LoaderBootstrap.class);
      } catch (ReflectiveOperationException var9) {
         throw new LoadingException("Unable to load bootstrap class", var9);
      }

      Constructor<? extends LoaderBootstrap> constructor;
      try {
         constructor = plugin.getConstructor(loaderPluginType);
      } catch (ReflectiveOperationException var8) {
         throw new LoadingException("Unable to get bootstrap constructor", var8);
      }

      try {
         return constructor.newInstance(loaderPlugin);
      } catch (ReflectiveOperationException var7) {
         throw new LoadingException("Unable to create bootstrap plugin instance", var7);
      }
   }

   private static URL extractJar(ClassLoader loaderClassLoader, String jarResourcePath) throws LoadingException {
      URL jarInJar = loaderClassLoader.getResource(jarResourcePath);
      if (jarInJar == null) {
         throw new LoadingException("Could not locate jar-in-jar");
      } else {
         Path path;
         try {
            path = Files.createTempFile("luckperms-jarinjar", ".jar.tmp");
         } catch (IOException var9) {
            throw new LoadingException("Unable to create a temporary file", var9);
         }

         path.toFile().deleteOnExit();

         try (InputStream in = jarInJar.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException var11) {
            throw new LoadingException("Unable to copy jar-in-jar to temporary path", var11);
         }

         try {
            return path.toUri().toURL();
         } catch (MalformedURLException var8) {
            throw new LoadingException("Unable to get URL from path", var8);
         }
      }
   }

   private boolean shouldLoadFromUrlsFirst(String name) {
      if (this.priorityPackagePrefixes == null) {
         return false;
      } else {
         for (String prefix : this.priorityPackagePrefixes) {
            if (name.startsWith(prefix)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (!this.shouldLoadFromUrlsFirst(name)) {
         return super.loadClass(name, resolve);
      } else {
         synchronized (this.getClassLoadingLock(name)) {
            Class<?> clazz = this.findLoadedClass(name);
            if (clazz != null) {
               return clazz;
            } else {
               try {
                  clazz = this.findClass(name);
               } catch (ClassNotFoundException var7) {
                  clazz = super.loadClass(name, false);
               }

               if (resolve) {
                  this.resolveClass(clazz);
               }

               return clazz;
            }
         }
      }
   }

   static {
      ClassLoader.registerAsParallelCapable();
   }
}
