package de.oliver.plugintests;

import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public record FPTestClass(Class<?> testClass, Method beforeEach, Method afterEach, List<Method> testMethods) {
   private static final Logger logger = Logger.getLogger(FPTestClass.class.getName());

   public static FPTestClass fromClass(Class<?> testClass) {
      Method beforeEach = null;
      Method afterEach = null;
      List<Method> testMethods = new ArrayList<>();

      for (Method method : testClass.getDeclaredMethods()) {
         if (method.isAnnotationPresent(FPTest.class)) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Player.class) {
               testMethods.add(method);
            }
         } else if (method.isAnnotationPresent(FPBeforeEach.class)) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Player.class) {
               beforeEach = method;
            }
         } else if (method.isAnnotationPresent(FPAfterEach.class) && method.getParameterCount() == 1 && method.getParameterTypes()[0] == Player.class) {
            afterEach = method;
         }
      }

      return new FPTestClass(testClass, beforeEach, afterEach, testMethods);
   }

   public boolean runTests(Player player) {
      logger.info("Running tests for " + this.testClass.getSimpleName());
      player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Running tests for " + this.testClass.getSimpleName()));

      for (Method testMethod : this.testMethods) {
         Object testClassObj;
         try {
            testClassObj = this.testClass.getDeclaredConstructor().newInstance();
         } catch (Exception var12) {
            logger.warning("Failed to create test class instance: " + var12.getMessage());
            return false;
         }

         FPTest fpTest = testMethod.getAnnotation(FPTest.class);
         if (fpTest.skip()) {
            logger.info("Skipping test " + this.displayName(testMethod));
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Skipping test " + this.displayName(testMethod)));
         } else {
            long testStart = System.currentTimeMillis();

            try {
               if (this.beforeEach != null) {
                  this.beforeEach.invoke(testClassObj, player);
               }

               testMethod.invoke(testClassObj, player);
               if (this.afterEach != null) {
                  this.afterEach.invoke(testClassObj, player);
               }
            } catch (InvocationTargetException var13) {
               logger.warning("Test " + this.displayName(testMethod) + " failed with exception: " + var13.getCause().getMessage());
               player.sendMessage(
                  MiniMessage.miniMessage()
                     .deserialize("<red>Test " + this.displayName(testMethod) + " failed with exception: " + var13.getCause().getMessage())
               );
               return false;
            } catch (Exception var14) {
               logger.warning("Unexpected exception in test " + fpTest.name() + ": " + var14.getMessage());
               return false;
            }

            long testEnd = System.currentTimeMillis();
            logger.info("Test " + this.displayName(testMethod) + " took " + (testEnd - testStart) + "ms");
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Test " + this.displayName(testMethod) + " took " + (testEnd - testStart) + "ms"));

            try {
               Thread.sleep(100L);
            } catch (InterruptedException var11) {
               logger.warning("Thread interrupted while waiting between tests: " + var11.getMessage());
            }
         }
      }

      return true;
   }

   public String displayName(Method m) {
      if (!m.isAnnotationPresent(FPTest.class)) {
         return this.testClass.getSimpleName() + "#" + m.getName();
      } else {
         FPTest fpTest = m.getAnnotation(FPTest.class);
         return this.testClass.getSimpleName() + "#" + m.getName() + " (" + fpTest.name() + ")";
      }
   }
}
