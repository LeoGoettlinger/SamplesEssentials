package me.chancesd.pvpmanager.sdutils.library.libby.classloader;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import me.chancesd.pvpmanager.sdutils.library.libby.Library;
import me.chancesd.pvpmanager.sdutils.library.libby.LibraryManager;
import me.chancesd.pvpmanager.sdutils.library.libby.Util;
import me.chancesd.pvpmanager.sdutils.library.libby.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

public abstract class ClassLoaderHelper {
   public static final String SYSTEM_PROPERTY_DISABLE_UNSAFE = "libby.classloaders.unsafeDisabled";
   public static final String SYSTEM_PROPERTY_DISABLE_JAVA_AGENT = "libby.classloaders.javaAgentDisabled";
   public static final String ENV_VAR_DISABLE_UNSAFE = "LIBBY_CLASSLOADERS_UNSAFE_DISABLED";
   public static final String ENV_VAR_DISABLE_JAVA_AGENT = "LIBBY_CLASSLOADERS_JAVA_AGENT_DISABLED";
   private static final String BYTE_BUDDY_AGENT_CLASS = Util.replaceWithDots("net{}bytebuddy{}agent{}ByteBuddyAgent");
   private static final Method getModuleMethod;
   private static final Method addOpensMethod;
   private static final Method getNameMethod;
   private static final Unsafe theUnsafe;
   private static volatile Instrumentation cachedInstrumentation;
   protected final ClassLoader classLoader;

   public ClassLoaderHelper(ClassLoader classLoader) {
      this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
   }

   public abstract void addToClasspath(@NotNull URL var1);

   public void addToClasspath(@NotNull Path path) {
      try {
         this.addToClasspath(Objects.requireNonNull(path, "path").toUri().toURL());
      } catch (MalformedURLException var3) {
         throw new IllegalArgumentException(var3);
      }
   }

   protected void setMethodAccessible(
      LibraryManager libraryManager,
      Method method,
      String methodSignature,
      Consumer<MethodHandle> methodHandleConsumer,
      Consumer<Instrumentation> instrumentationConsumer
   ) {
      if (!Modifier.isPublic(method.getModifiers())) {
         try {
            openModule(method.getDeclaringClass());
         } catch (Exception var13) {
         }

         try {
            method.setAccessible(true);
         } catch (Exception var17) {
            Exception unsafeException;
            this.handleInaccessibleObjectException(var17, methodSignature);
            unsafeException = null;
            label66:
            if (theUnsafe != null && this.canUseUnsafe()) {
               MethodHandle methodHandle;
               try {
                  methodHandle = this.getPrivilegedMethodHandle(method).bindTo(this.classLoader);
               } catch (Exception var16) {
                  unsafeException = var16;
                  break label66;
               }

               methodHandleConsumer.accept(methodHandle);
               return;
            }

            Exception javaAgentException;
            javaAgentException = null;
            label59:
            if (this.canUseJavaAgent()) {
               Instrumentation instrumentation;
               try {
                  instrumentation = this.initInstrumentation(libraryManager);
               } catch (Exception var15) {
                  javaAgentException = var15;
                  break label59;
               }

               try {
                  instrumentationConsumer.accept(instrumentation);
                  return;
               } catch (Exception var14) {
                  this.handleInaccessibleObjectException(var14, methodSignature);
               }
            }

            Logger logger = libraryManager.getLogger();
            if (unsafeException != null) {
               logger.error("Cannot set accessible " + methodSignature + " using unsafe", unsafeException);
            }

            if (javaAgentException != null) {
               logger.error("Cannot set accessible " + methodSignature + " using java agent", javaAgentException);
            }

            String packageName = method.getDeclaringClass().getPackage().getName();
            String moduleName = null;

            try {
               moduleName = (String)getNameMethod.invoke(getModuleMethod.invoke(method.getDeclaringClass()));
            } catch (Exception var12) {
            }

            if (moduleName != null) {
               logger.error(
                  "Cannot set accessible "
                     + methodSignature
                     + ", if you are using Java 9+ try to add the following option to your java command: --add-opens "
                     + moduleName
                     + "/"
                     + packageName
                     + "=ALL-UNNAMED"
               );
            } else {
               logger.error("Cannot set accessible " + methodSignature);
            }

            throw new RuntimeException("Cannot set accessible " + methodSignature);
         }
      }
   }

   private void handleInaccessibleObjectException(Exception exception, String methodSignature) {
      if (!exception.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
         throw new RuntimeException("Cannot set accessible " + methodSignature, exception);
      }
   }

   protected static void openModule(Class<?> toOpen) throws Exception {
      Object urlClassLoaderModule = getModuleMethod.invoke(toOpen);
      Object thisModule = getModuleMethod.invoke(ClassLoaderHelper.class);
      addOpensMethod.invoke(urlClassLoaderModule, toOpen.getPackage().getName(), thisModule);
   }

   protected MethodHandle getPrivilegedMethodHandle(Method method) {
      for (Field trustedLookup : Lookup.class.getDeclaredFields()) {
         if (trustedLookup.getType() == Lookup.class && Modifier.isStatic(trustedLookup.getModifiers()) && !trustedLookup.isSynthetic()) {
            try {
               Lookup lookup = (Lookup)theUnsafe.getObject(theUnsafe.staticFieldBase(trustedLookup), theUnsafe.staticFieldOffset(trustedLookup));
               return lookup.unreflect(method);
            } catch (Exception var7) {
            }
         }
      }

      throw new RuntimeException("Cannot get privileged method handle.");
   }

   protected Instrumentation initInstrumentation(LibraryManager libraryManager) throws Exception {
      Instrumentation instr = cachedInstrumentation;
      if (instr != null) {
         return instr;
      } else {
         IsolatedClassLoader isolatedClassLoader = new IsolatedClassLoader();

         Instrumentation var6;
         try {
            isolatedClassLoader.addPath(
               libraryManager.downloadLibrary(
                  Library.builder()
                     .groupId("net{}bytebuddy")
                     .artifactId("byte-buddy-agent")
                     .version("1.12.1")
                     .checksumFromBase64("mcCtBT9cljUEniB5ESpPDYZMfVxEs1JRPllOiWTP+bM=")
                     .repository("https://repo1.maven.org/maven2/")
                     .build()
               )
            );
            Class<?> byteBuddyAgent = isolatedClassLoader.loadClass(BYTE_BUDDY_AGENT_CLASS);
            Instrumentation instrumentation = (Instrumentation)byteBuddyAgent.getMethod("install").invoke(null);
            cachedInstrumentation = instrumentation;
            var6 = instrumentation;
         } finally {
            try {
               isolatedClassLoader.close();
            } catch (Exception var13) {
            }
         }

         return var6;
      }
   }

   protected boolean canUseUnsafe() {
      return !Boolean.parseBoolean(System.getProperty("libby.classloaders.unsafeDisabled"))
         && !Boolean.parseBoolean(System.getenv("LIBBY_CLASSLOADERS_UNSAFE_DISABLED"));
   }

   protected boolean canUseJavaAgent() {
      return !Boolean.parseBoolean(System.getProperty("libby.classloaders.javaAgentDisabled"))
         && !Boolean.parseBoolean(System.getenv("LIBBY_CLASSLOADERS_JAVA_AGENT_DISABLED"));
   }

   static {
      Method getModule = null;
      Method addOpens = null;
      Method getName = null;

      try {
         Class<?> moduleClass = Class.forName("java.lang.Module");
         getModule = Class.class.getMethod("getModule");
         addOpens = moduleClass.getMethod("addOpens", String.class, moduleClass);
         getName = moduleClass.getMethod("getName");
      } catch (Exception var10) {
      } finally {
         getModuleMethod = getModule;
         addOpensMethod = addOpens;
         getNameMethod = getName;
      }

      Unsafe unsafe = null;

      for (Field f : Unsafe.class.getDeclaredFields()) {
         try {
            if (f.getType() == Unsafe.class && Modifier.isStatic(f.getModifiers())) {
               f.setAccessible(true);
               unsafe = (Unsafe)f.get(null);
            }
         } catch (Exception var9) {
         }
      }

      theUnsafe = unsafe;
   }
}
