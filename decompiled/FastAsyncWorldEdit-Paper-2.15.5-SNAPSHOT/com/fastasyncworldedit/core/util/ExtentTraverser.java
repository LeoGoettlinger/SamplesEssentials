package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.World;
import java.lang.reflect.Field;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class ExtentTraverser<T extends Extent> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final T root;
   private final ExtentTraverser<T> parent;

   public ExtentTraverser(@Nonnull T root) {
      this(root, null);
   }

   public ExtentTraverser(@Nonnull T root, ExtentTraverser<T> parent) {
      this.root = root;
      this.parent = parent;
   }

   @Nullable
   public static World getWorldFromExtent(Extent extent) {
      if (extent.isWorld()) {
         return (World)extent;
      } else if (extent instanceof EditSession session) {
         return session.getWorld();
      } else if (extent instanceof SingleThreadQueueExtent stqe) {
         return stqe.getWorld();
      } else {
         return extent instanceof ParallelQueueExtent pqe
            ? ((SingleThreadQueueExtent)pqe.getExtent()).getWorld()
            : Optional.ofNullable(new ExtentTraverser<>(extent).findAndGet(World.class))
               .orElseGet(() -> Optional.ofNullable(new ExtentTraverser<>(extent).findAndGet(EditSession.class)).map(EditSession::getWorld).orElse(null));
      }
   }

   public boolean exists() {
      return this.root != null;
   }

   @Nullable
   public T get() {
      return this.root;
   }

   public boolean setNext(T next) {
      try {
         Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
         ReflectionUtils.setFailsafeFieldValue(field, this.root, next);
         return true;
      } catch (Throwable var3) {
         LOGGER.error(var3);
         return false;
      }
   }

   public ExtentTraverser<T> last() {
      ExtentTraverser<T> last = this;

      for (ExtentTraverser<T> traverser = this; traverser != null && traverser.get() instanceof AbstractDelegateExtent; traverser = traverser.next()) {
         last = traverser;
      }

      return last;
   }

   @Nullable
   public <U extends Extent> U findAndGet(Class<U> clazz) {
      ExtentTraverser<U> traverser = this.find(clazz);
      return traverser != null ? traverser.get() : null;
   }

   public <U extends Extent> ExtentTraverser<U> find(Class<U> clazz) {
      try {
         for (ExtentTraverser<T> value = this; value != null; value = value.next()) {
            if (clazz.isAssignableFrom(value.root.getClass())) {
               return value;
            }
         }

         return null;
      } catch (Throwable var3) {
         LOGGER.error(var3);
         return null;
      }
   }

   public <U extends Extent> ExtentTraverser<U> find(Object object) {
      try {
         for (ExtentTraverser<T> value = this; value != null; value = value.next()) {
            if (value.root == object) {
               return value;
            }
         }

         return null;
      } catch (Throwable var3) {
         LOGGER.error(var3);
         return null;
      }
   }

   public ExtentTraverser<T> previous() {
      return this.parent;
   }

   public ExtentTraverser<T> next() {
      try {
         if (this.root instanceof AbstractDelegateExtent abstractDelegateExtent) {
            T value = (T)abstractDelegateExtent.getExtent();
            return value == null ? null : new ExtentTraverser<>(value, this);
         } else {
            return null;
         }
      } catch (Throwable var3) {
         LOGGER.error(var3);
         return null;
      }
   }

   public static void printNestedExtents(Extent extent) {
      String nested = printNestedExtent(new StringBuilder("Extent tree:"), new ExtentTraverser<>(extent), 0).toString();
      LOGGER.info(nested);
   }

   private static StringBuilder printNestedExtent(StringBuilder builder, ExtentTraverser<?> traverser, int depth) {
      if (traverser != null && traverser.exists()) {
         String indent = "  ".repeat(Math.max(0, depth));
         Extent extent = traverser.get();
         builder.append("\n").append(indent).append("- ").append(extent.getClass().getSimpleName());
         printNestedExtent(builder, traverser.next(), depth + 1);
         return builder;
      } else {
         return builder;
      }
   }
}
