package com.fastasyncworldedit.core.internal.exception;

import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Locale;

public class FaweException extends RuntimeException {
   public static final FaweException _enableQueue = new FaweException("enableQueue");
   public static final FaweException _disableQueue = new FaweException("disableQueue");
   private final Component message;
   private final FaweException.Type type;
   private final boolean ignorable;

   public FaweException(String reason) {
      this(TextComponent.of(reason));
   }

   public FaweException(Component reason) {
      this(reason, FaweException.Type.OTHER);
   }

   public FaweException(Component reason, FaweException.Type type) {
      this(reason, type, false);
   }

   public FaweException(Component reason, FaweException.Type type, boolean ignorable) {
      this.message = reason;
      this.type = type;
      this.ignorable = ignorable;
   }

   @Override
   public String getMessage() {
      return WorldEditText.reduceToText(this.message, Locale.ROOT);
   }

   public Component getComponent() {
      return this.message;
   }

   public FaweException.Type getType() {
      return this.type;
   }

   public boolean ignorable() {
      return this.ignorable;
   }

   public static FaweException get(Throwable e) {
      if (e instanceof FaweException) {
         return (FaweException)e;
      } else {
         Throwable cause = e.getCause();
         return cause == null ? null : get(cause);
      }
   }

   @Override
   public Throwable fillInStackTrace() {
      return this;
   }

   public static enum Type {
      MANUAL,
      NO_REGION,
      OUTSIDE_REGION,
      OUTSIDE_SAFE_REGION,
      MAX_CHECKS,
      MAX_CHANGES,
      LOW_MEMORY,
      MAX_ENTITIES,
      MAX_TILES,
      MAX_ITERATIONS,
      BLOCK_BAG,
      CHUNK,
      PLAYER_ONLY,
      ACTOR_REQUIRED,
      CLIPBOARD,
      HISTORY,
      OTHER;
   }
}
