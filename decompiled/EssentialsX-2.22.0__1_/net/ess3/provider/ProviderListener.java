package net.ess3.provider;

import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public abstract class ProviderListener implements Provider, Listener {
   protected final Consumer<Event> function;

   public ProviderListener(final Consumer<Event> function) {
      this.function = function;
   }
}
