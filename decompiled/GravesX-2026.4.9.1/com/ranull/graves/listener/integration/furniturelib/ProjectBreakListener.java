package com.ranull.graves.listener.integration.furniturelib;

import com.ranull.graves.integration.FurnitureLib;
import de.Ste3et_C0st.FurnitureLib.SchematicLoader.Events.ProjectBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProjectBreakListener implements Listener {
   private final FurnitureLib furnitureLib;

   public ProjectBreakListener(FurnitureLib furnitureLib) {
      this.furnitureLib = furnitureLib;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onProjectBreak(ProjectBreakEvent event) {
      if (this.isProjectAssociatedWithGrave(event)) {
         event.setCancelled(true);
      }
   }

   private boolean isProjectAssociatedWithGrave(ProjectBreakEvent event) {
      return event.getID().getUUID() != null && this.furnitureLib.getGrave(event.getLocation(), event.getID().getUUID()) != null;
   }
}
