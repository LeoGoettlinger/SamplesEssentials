package com.sk89q.worldedit.internal.command.exception;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;

public abstract class ExceptionConverterHelper implements ExceptionConverter {
   private final List<ExceptionConverterHelper.ExceptionHandler> handlers;

   public ExceptionConverterHelper() {
      List<ExceptionConverterHelper.ExceptionHandler> handlers = new ArrayList<>();

      for (Method method : this.getClass().getMethods()) {
         if (method.getAnnotation(ExceptionMatch.class) != null) {
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 1) {
               Class<?> cls = parameters[0];
               if (Throwable.class.isAssignableFrom(cls)) {
                  handlers.add(new ExceptionConverterHelper.ExceptionHandler((Class<? extends Throwable>)cls, method));
               }
            }
         }
      }

      Collections.sort(handlers);
      this.handlers = handlers;
   }

   @Override
   public void convert(Throwable t) throws CommandException {
      Class<?> throwableClass = t.getClass();

      for (ExceptionConverterHelper.ExceptionHandler handler : this.handlers) {
         if (handler.cls.isAssignableFrom(throwableClass)) {
            try {
               handler.method.invoke(this, t);
            } catch (InvocationTargetException var6) {
               if (var6.getCause() instanceof CommandException) {
                  throw (CommandException)var6.getCause();
               }

               if (var6.getCause() instanceof com.sk89q.minecraft.util.commands.CommandException) {
                  throw new CommandExecutionException(var6.getCause(), ImmutableList.of());
               }

               throw new CommandExecutionException(var6, ImmutableList.of());
            } catch (IllegalAccessException | IllegalArgumentException var7) {
               throw new CommandExecutionException(var7, ImmutableList.of());
            }
         }
      }
   }

   private record ExceptionHandler(Class<? extends Throwable> cls, Method method) implements Comparable<ExceptionConverterHelper.ExceptionHandler> {
      public int compareTo(ExceptionConverterHelper.ExceptionHandler o) {
         if (this.cls.equals(o.cls)) {
            return 0;
         } else if (this.cls.isAssignableFrom(o.cls)) {
            return 1;
         } else {
            return o.cls.isAssignableFrom(this.cls) ? -1 : this.cls.getCanonicalName().compareTo(o.cls.getCanonicalName());
         }
      }
   }
}
