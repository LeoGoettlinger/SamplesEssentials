package eu.kennytv.maintenance.paper.util;

import eu.kennytv.maintenance.core.util.Task;
import org.bukkit.Bukkit;

public final class PaperTask implements Task {
   private final int id;

   public PaperTask(int id) {
      this.id = id;
   }

   @Override
   public void cancel() {
      Bukkit.getScheduler().cancelTask(this.id);
   }
}
