package dev.cwhead.GravesX.exception;

import org.bukkit.event.Event;

public class GravesXNullPointerException extends NullPointerException {
   private static final long serialVersionUID = 1L;

   public GravesXNullPointerException(Event event, String cause) {
      super(event.getEventName() + " tried to find " + cause + ", but returned null.");
   }

   public GravesXNullPointerException(String message) {
      super(message);
   }
}
