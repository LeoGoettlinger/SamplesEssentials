package com.sk89q.worldedit.internal.util;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Throwables;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;

public class ErrorReporting {
   private ErrorReporting() {
   }

   public static void trigger(Actor actor, Throwable error) {
      actor.printError(Caption.of("worldedit.command.error.report"));
      TextComponent.Builder errorBuilder = TextComponent.builder(error.getClass().getName() + ": " + error.getMessage());
      if (actor.hasPermission("worldedit.error.detailed")) {
         errorBuilder = errorBuilder.hoverEvent(HoverEvent.showText(TextComponent.of(Throwables.getStackTraceAsString(error))));
      }

      actor.print(errorBuilder.build());
   }
}
