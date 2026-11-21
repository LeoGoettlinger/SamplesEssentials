package com.sk89q.worldedit.command.util.annotation;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.util.task.InterruptableCondition;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.command.CommandArgParser;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.exception.StopExecutionException;
import org.enginehub.piston.inject.InjectAnnotation;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MemoizingValueAccess;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@InjectAnnotation
public @interface Confirm {
   Confirm.Processor value() default Confirm.Processor.ALWAYS;

   public static enum Processor {
      NULLABLE_REGION {
         @Override
         public boolean passes(Actor actor, InjectedValueAccess context, double value) {
            if (this.checkExisting(context)) {
               return true;
            } else {
               Region region = context.injectedValue(Key.of(Region.class, Selection.class)).orElse(null);
               return region == null ? true : Confirm.Processor.handleRegion(region, actor, context, value);
            }
         }
      },
      REGION {
         @Override
         public boolean passes(Actor actor, InjectedValueAccess context, double value) {
            if (this.checkExisting(context)) {
               return true;
            } else {
               Region region = context.injectedValue(Key.of(Region.class, Selection.class)).orElseThrow(IncompleteRegionException::new);
               return Confirm.Processor.handleRegion(region, actor, context, value);
            }
         }
      },
      RADIUS {
         @Override
         public boolean passes(Actor actor, InjectedValueAccess context, double value) {
            if (this.checkExisting(context)) {
               return true;
            } else {
               int max = actor.getLimit().MAX_RADIUS;
               if (max != -1 && value > max) {
                  actor.print(Caption.of("fawe.cancel.reason.confirm.radius", value, max, Confirm.Processor.getArgs(context)));
                  return Confirm.Processor.confirm(actor, context);
               } else {
                  return true;
               }
            }
         }
      },
      LIMIT {
         @Override
         public boolean passes(Actor actor, InjectedValueAccess context, double value) {
            if (this.checkExisting(context)) {
               return true;
            } else {
               int max = 50;
               if (max != -1 && value > max) {
                  actor.print(Caption.of("fawe.cancel.reason.confirm.limit", value, max, Confirm.Processor.getArgs(context)));
                  return Confirm.Processor.confirm(actor, context);
               } else {
                  return true;
               }
            }
         }
      },
      ALWAYS {
         @Override
         public boolean passes(Actor actor, InjectedValueAccess context, double value) {
            if (this.checkExisting(context)) {
               return true;
            } else {
               actor.print(Caption.of("fawe.cancel.reason.confirm", Confirm.Processor.getArgs(context)));
               return Confirm.Processor.confirm(actor, context);
            }
         }
      };

      private static final Logger LOGGER = LogManagerCompat.getLogger();

      public boolean passes(Actor actor, InjectedValueAccess context, double value) {
         return true;
      }

      public <T extends Number> T check(Actor actor, InjectedValueAccess context, T value) {
         boolean isSuggestion = context.injectedValue(Key.of(boolean.class)).orElse(false);
         if (isSuggestion) {
            return value;
         } else if (this.checkExisting(context)) {
            return value;
         } else if (!this.passes(actor, context, value.doubleValue())) {
            throw new StopExecutionException(TextComponent.empty());
         } else {
            return value;
         }
      }

      private static String getArgs(InjectedValueAccess context) {
         return context.injectedValue(Key.of(Arguments.class)).map(Arguments::get).get();
      }

      private static String getArg(InjectedValueAccess context, String def) {
         Stream<Substring> split = CommandArgParser.forArgString(getArgs(context)).parseArgs();
         Substring first = split.findFirst().orElse(null);
         if (first == null && def == null) {
            throw new StopExecutionException(TextComponent.of("No arguments"));
         } else {
            return first != null ? first.getSubstring() : def;
         }
      }

      private static int getInt(InjectedValueAccess context, String def) {
         return Integer.parseInt(getArg(context, def));
      }

      private static boolean confirm(Actor actor, InjectedValueAccess context) {
         Event event = context.injectedValue(Key.of(Event.class)).orElse(null);
         if (!(event instanceof CommandEvent)) {
            return true;
         } else {
            ReentrantLock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            InterruptableCondition wait = new InterruptableCondition(lock, condition, Thread.currentThread());

            try {
               lock.lock();
               actor.setMeta("cmdConfirm", wait);

               try {
                  if (!(context instanceof MemoizingValueAccess memoizingValueAccess)) {
                     if (!context.getClass().getSimpleName().contains("AutoValue_CommandParametersImpl")) {
                        return true;
                     }

                     memoizingValueAccess = (MemoizingValueAccess)Confirm.Reflect.injectedValues.get(context);
                  }

                  Map<Key<?>, Optional<?>> memory = (Map<Key<?>, Optional<?>>)Confirm.Reflect.memory.get(memoizingValueAccess);
                  memory.put(Key.of(InterruptableCondition.class), Optional.of(wait));
               } catch (IllegalAccessException var12) {
                  LOGGER.error("Error confirming command", var12);
               }

               return condition.await(15L, TimeUnit.SECONDS);
            } catch (InterruptedException var13) {
               return false;
            } finally {
               if (actor.getMeta("cmdConfirm") == wait) {
                  actor.deleteMeta("cmdConfirm");
               }
            }
         }
      }

      boolean checkExisting(InjectedValueAccess context) {
         Optional<InterruptableCondition> lock = context.injectedValue(Key.of(InterruptableCondition.class));
         return lock.isPresent();
      }

      private static boolean handleRegion(Region region, Actor actor, InjectedValueAccess context, double value) {
         BlockVector3 pos1 = region.getMinimumPoint();
         BlockVector3 pos2 = region.getMaximumPoint();
         long area = (pos2.x() - pos1.x()) * (pos2.z() - pos1.z() + 1) * (long)value;
         long max = 524288L;
         if (max != -1L && area > max) {
            actor.print(Caption.of("fawe.cancel.reason.confirm.region", pos1, pos2, getArgs(context), region.getHeight() * area));
            return confirm(actor, context);
         } else {
            return true;
         }
      }
   }

   public static class Reflect {
      private static final Logger LOGGER = LogManagerCompat.getLogger();
      static final Field memory;
      static final Field injectedValues;

      static {
         Field memoryField;
         try {
            memoryField = MemoizingValueAccess.class.getDeclaredField("memory");
            memoryField.setAccessible(true);
         } catch (NoSuchFieldException var4) {
            LOGGER.error("Could not find memory field", var4);
            memoryField = null;
         }

         memory = memoryField;

         Field injectedValuesField;
         try {
            Class<?> c = Class.forName("org.enginehub.piston.impl.AutoValue_CommandParametersImpl");
            injectedValuesField = c.getDeclaredField("injectedValues");
            injectedValuesField.setAccessible(true);
         } catch (ClassNotFoundException | NoSuchFieldException var3) {
            LOGGER.error("Could not find injectedValues field", var3);
            injectedValuesField = null;
         }

         injectedValues = injectedValuesField;
      }
   }
}
