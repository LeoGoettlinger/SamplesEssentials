package de.theredend2000.advancedhunt.lib.xseries.reflection.minecraft;

import de.theredend2000.advancedhunt.lib.xseries.reflection.XReflection;
import de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.classes.PackageHandle;
import org.intellij.lang.annotations.Pattern;

public enum MinecraftPackage implements PackageHandle {
   NMS(XReflection.NMS_PACKAGE),
   CB(XReflection.CRAFTBUKKIT_PACKAGE),
   BUKKIT("org.bukkit"),
   SPIGOT("org.spigotmc");

   private final String packageId;

   private MinecraftPackage(String packageName) {
      this.packageId = packageName;
   }

   @Override
   public String packageId() {
      return this.name();
   }

   @Override
   public String getBasePackageName() {
      return this.packageId;
   }

   @Override
   public String getPackage(
      @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String packageName
   ) {
      if (!packageName.startsWith(".") && !packageName.endsWith(".")) {
         return packageName.isEmpty() || this == NMS && !XReflection.supports(17) ? this.packageId : this.packageId + '.' + packageName;
      } else {
         throw new IllegalArgumentException("Package name must not start or end with a dot: " + packageName + " (" + this + ')');
      }
   }
}
