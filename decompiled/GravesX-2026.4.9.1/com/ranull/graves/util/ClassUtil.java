package com.ranull.graves.util;

public class ClassUtil {
   private ClassUtil() {
   }

   public static void loadClass(String className) {
      if (className != null && !className.isBlank()) {
         ClassLoader ctx = Thread.currentThread().getContextClassLoader();

         try {
            Class.forName(className, true, ctx != null ? ctx : ClassLoader.getSystemClassLoader());
         } catch (ClassNotFoundException var4) {
            try {
               Class.forName(className);
            } catch (LinkageError | ClassNotFoundException var3) {
               var3.printStackTrace();
            }
         } catch (LinkageError var5) {
            var5.printStackTrace();
         }
      }
   }
}
