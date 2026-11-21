package com.github.luben.zstd.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public enum Native {
   private static final String nativePathOverride = "ZstdNativePath";
   private static final String libnameShort = "zstd-jni";
   private static final String libname = "libzstd-jni";
   private static final String errorMsg = "Unsupported OS/arch, cannot find "
      + resourceName()
      + " or load "
      + "zstd-jni"
      + " from system libraries. Please try building from source the jar or providing "
      + "libzstd-jni"
      + " in your system.";
   private static boolean loaded = false;

   private static String osName() {
      String var0 = System.getProperty("os.name").toLowerCase().replace(' ', '_');
      if (var0.startsWith("win")) {
         return "win";
      } else {
         return var0.startsWith("mac") ? "darwin" : var0;
      }
   }

   private static String osArch() {
      return System.getProperty("os.arch");
   }

   private static String libExtension() {
      if (osName().contains("os_x") || osName().contains("darwin")) {
         return "dylib";
      } else {
         return osName().contains("win") ? "dll" : "so";
      }
   }

   private static String resourceName() {
      return "/" + osName() + "/" + osArch() + "/" + "libzstd-jni" + "." + libExtension();
   }

   public static synchronized boolean isLoaded() {
      return loaded;
   }

   public static synchronized void load() {
      load(null);
   }

   public static synchronized void load(File var0) {
      if (!loaded) {
         String var1 = resourceName();
         String var2 = System.getProperty("ZstdNativePath");
         if (var2 != null) {
            System.load(var2);
         }

         try {
            Class.forName("org.osgi.framework.BundleEvent");
            System.loadLibrary("libzstd-jni");
            loaded = true;
         } catch (Throwable var28) {
            InputStream var3 = Native.class.getResourceAsStream(var1);
            if (var3 == null) {
               try {
                  System.loadLibrary("zstd-jni");
                  loaded = true;
               } catch (UnsatisfiedLinkError var22) {
                  UnsatisfiedLinkError var29 = new UnsatisfiedLinkError(var22.getMessage() + "\n" + errorMsg);
                  var29.setStackTrace(var22.getStackTrace());
                  throw var29;
               }
            } else {
               File var4 = null;
               FileOutputStream var5 = null;

               try {
                  var4 = File.createTempFile("libzstd-jni", "." + libExtension(), var0);
                  var4.deleteOnExit();
                  var5 = new FileOutputStream(var4);
                  byte[] var6 = new byte[4096];

                  while (true) {
                     int var30 = var3.read(var6);
                     if (var30 == -1) {
                        try {
                           var5.flush();
                           var5.close();
                           var5 = null;
                        } catch (IOException var25) {
                        }

                        try {
                           System.load(var4.getAbsolutePath());
                        } catch (UnsatisfiedLinkError var24) {
                           try {
                              System.loadLibrary("zstd-jni");
                           } catch (UnsatisfiedLinkError var23) {
                              UnsatisfiedLinkError var9 = new UnsatisfiedLinkError(var24.getMessage() + "\n" + var23.getMessage() + "\n" + errorMsg);
                              var9.setStackTrace(var23.getStackTrace());
                              throw var9;
                           }
                        }

                        loaded = true;
                        return;
                     }

                     var5.write(var6, 0, var30);
                  }
               } catch (IOException var26) {
                  ExceptionInInitializerError var7 = new ExceptionInInitializerError("Cannot unpack libzstd-jni: " + var26.getMessage());
                  var7.setStackTrace(var26.getStackTrace());
                  throw var7;
               } finally {
                  try {
                     var3.close();
                     if (var5 != null) {
                        var5.close();
                     }

                     if (var4 != null && var4.exists()) {
                        var4.delete();
                     }
                  } catch (IOException var21) {
                  }
               }
            }
         }
      }
   }
}
