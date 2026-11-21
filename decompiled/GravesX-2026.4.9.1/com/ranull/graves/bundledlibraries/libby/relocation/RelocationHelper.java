package com.ranull.graves.bundledlibraries.libby.relocation;

import com.ranull.graves.bundledlibraries.libby.Library;
import com.ranull.graves.bundledlibraries.libby.LibraryManager;
import com.ranull.graves.bundledlibraries.libby.Util;
import com.ranull.graves.bundledlibraries.libby.classloader.IsolatedClassLoader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class RelocationHelper {
   private static final String JAR_RELOCATOR_CLASS = Util.replaceWithDots("me{}lucko{}jarrelocator{}JarRelocator");
   private static final String RELOCATION_CLASS = Util.replaceWithDots("me{}lucko{}jarrelocator{}Relocation");
   private final Constructor<?> jarRelocatorConstructor;
   private final Method jarRelocatorRunMethod;
   private final Constructor<?> relocationConstructor;

   public RelocationHelper(@NotNull LibraryManager libraryManager) {
      Objects.requireNonNull(libraryManager, "libraryManager");
      IsolatedClassLoader classLoader = new IsolatedClassLoader();
      classLoader.addPath(
         libraryManager.downloadLibrary(
            Library.builder()
               .groupId("org{}ow2{}asm")
               .artifactId("asm-commons")
               .version("9.7")
               .checksumFromBase64("OJvCR5WOBJ/JoECNOYySxtNwwYA1EgOV1Muh2dkwS3o=")
               .fallbackRepository("https://repo1.maven.org/maven2/")
               .build()
         )
      );
      classLoader.addPath(
         libraryManager.downloadLibrary(
            Library.builder()
               .groupId("org{}ow2{}asm")
               .artifactId("asm")
               .version("9.7")
               .checksumFromBase64("rfRtXjSUC98Ujs3Sap7o7qlElqcgNP9xQQZrPupcTp0=")
               .fallbackRepository("https://repo1.maven.org/maven2/")
               .build()
         )
      );
      classLoader.addPath(
         libraryManager.downloadLibrary(
            Library.builder()
               .groupId("me{}lucko")
               .artifactId("jar-relocator")
               .version("1.7")
               .checksumFromBase64("b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I=")
               .fallbackRepository("https://repo1.maven.org/maven2/")
               .build()
         )
      );

      try {
         Class<?> jarRelocatorClass = classLoader.loadClass(JAR_RELOCATOR_CLASS);
         Class<?> relocationClass = classLoader.loadClass(RELOCATION_CLASS);
         this.jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);
         this.jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");
         this.relocationConstructor = relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class);
      } catch (ReflectiveOperationException var5) {
         throw new RuntimeException(var5);
      }
   }

   public void relocate(@NotNull Path in, @NotNull Path out, @NotNull Collection<Relocation> relocations) {
      Objects.requireNonNull(in, "in");
      Objects.requireNonNull(out, "out");
      Objects.requireNonNull(relocations, "relocations");

      try {
         List<Object> rules = new LinkedList<>();

         for (Relocation relocation : relocations) {
            rules.add(
               this.relocationConstructor
                  .newInstance(relocation.getPattern(), relocation.getRelocatedPattern(), relocation.getIncludes(), relocation.getExcludes())
            );
         }

         this.jarRelocatorRunMethod.invoke(this.jarRelocatorConstructor.newInstance(in.toFile(), out.toFile(), rules));
      } catch (ReflectiveOperationException var7) {
         throw new RuntimeException(var7);
      }
   }
}
