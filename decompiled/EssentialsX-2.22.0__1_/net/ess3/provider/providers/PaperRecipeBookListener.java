package net.ess3.provider.providers;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import java.util.function.Consumer;
import net.ess3.provider.ProviderListener;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

public class PaperRecipeBookListener extends ProviderListener {
   public PaperRecipeBookListener(final Consumer<Event> function) {
      super(function);
   }

   @EventHandler
   public void onPlayerRecipeBookClick(final PlayerRecipeBookClickEvent event) {
      this.function.accept(event);
   }
}
