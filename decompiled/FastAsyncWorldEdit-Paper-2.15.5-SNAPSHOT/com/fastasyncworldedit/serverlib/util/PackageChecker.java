package com.fastasyncworldedit.serverlib.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public final class PackageChecker {
   private static final Logger LOGGER = Logger.getLogger(PackageChecker.class.getName());
   private static final String CLASS_EXTENSION = ".class";
   private static final Set<String> KNOWN_ENTRIES;

   private PackageChecker() {
   }

   private static void handleResource(Builder<String> builder, String resource) {
      if (resource.endsWith(".class")) {
         int slash = resource.lastIndexOf("/");
         if (slash == -1) {
            return;
         }

         builder.add(resource.substring(0, slash));
      } else if (resource.endsWith("/")) {
         builder.add(resource.substring(0, resource.length() - 1));
      }
   }

   public static boolean packageExists(String name) {
      String path = name.replace(".", "/");

      for (String knownEntry : KNOWN_ENTRIES) {
         if (knownEntry.startsWith(path)) {
            return true;
         }
      }

      return false;
   }

   static {
      Builder<String> builder = ImmutableSet.builder();

      try {
         Enumeration<URL> urls = ClassLoader.getSystemClassLoader().getResources("META-INF");

         while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof JarURLConnection) {
               JarURLConnection connection = (JarURLConnection)urlConnection;
               JarFile jar = connection.getJarFile();

               try {
                  Enumeration<JarEntry> entries = jar.entries();

                  while (entries.hasMoreElements()) {
                     ZipEntry entry = entries.nextElement();
                     String resource = entry.toString();
                     if (resource.startsWith("/")) {
                        handleResource(builder, resource.substring(1));
                     } else {
                        handleResource(builder, resource);
                     }
                  }
               } catch (Throwable var10) {
                  if (jar != null) {
                     try {
                        jar.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (jar != null) {
                  jar.close();
               }
            }
         }
      } catch (IOException var11) {
         LOGGER.log(Level.WARNING, "Couldn't scan for known JarEntries.", (Throwable)var11);
      }

      KNOWN_ENTRIES = builder.build();
   }
}
