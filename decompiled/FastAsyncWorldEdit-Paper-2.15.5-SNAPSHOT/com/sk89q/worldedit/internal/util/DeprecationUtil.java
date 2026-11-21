package com.sk89q.worldedit.internal.util;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class DeprecationUtil {
   private DeprecationUtil() {
   }

   public static void checkDelegatingOverride(Class<?> implementingClass) {
      StackTraceElement caller = (StackTraceElement)Throwables.lazyStackTrace(new Throwable()).get(1);
      Method callingMethod = getCallingMethod(caller);
      NonAbstractForCompatibility annotation = callingMethod.getAnnotation(NonAbstractForCompatibility.class);

      Method deprecatedMethod;
      try {
         deprecatedMethod = implementingClass.getMethod(annotation.delegateName(), annotation.delegateParams());
      } catch (NoSuchMethodException var6) {
         throw new AssertionError("Missing method referenced by " + NonAbstractForCompatibility.class, var6);
      }

      if (deprecatedMethod.getDeclaringClass().getName().equals(caller.getClassName())) {
         throw new IllegalStateException("Class " + implementingClass.getName() + " must override " + methodToString(callingMethod));
      }
   }

   private static Method getCallingMethod(StackTraceElement callerInfo) {
      Method[] declaredMethods;
      try {
         declaredMethods = Class.forName(callerInfo.getClassName()).getDeclaredMethods();
      } catch (ClassNotFoundException var6) {
         throw new AssertionError("Caller class missing?", var6);
      }

      for (Method declaredMethod : declaredMethods) {
         if (declaredMethod.isAnnotationPresent(NonAbstractForCompatibility.class) && declaredMethod.getName().equals(callerInfo.getMethodName())) {
            return declaredMethod;
         }
      }

      throw new IllegalStateException("Failed to find caller method " + callerInfo.getMethodName() + " annotated with " + NonAbstractForCompatibility.class);
   }

   private static String methodToString(Method method) {
      StringBuilder builder = new StringBuilder(method.getDeclaringClass().getCanonicalName()).append('.').append(method.getName()).append('(');
      Joiner.on(", ").appendTo(builder, Stream.of(method.getParameterTypes()).map(Class::getSimpleName).iterator());
      builder.append(')');
      return builder.toString();
   }

   public static boolean isSign(BlockType blockType) {
      BlockType sign = BlockTypes.SIGN;
      BlockType wallSign = BlockTypes.WALL_SIGN;
      return blockType == sign || blockType == wallSign || BlockCategories.SIGNS.contains(blockType);
   }

   public static String getHeadOwnerKey() {
      int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion();
      return dataVersion >= 2566 ? "SkullOwner" : "Owner";
   }
}
