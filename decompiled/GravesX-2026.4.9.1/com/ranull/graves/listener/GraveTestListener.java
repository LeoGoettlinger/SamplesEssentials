package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.event.GraveBreakEvent;
import dev.cwhead.GravesX.event.GraveCloseEvent;
import dev.cwhead.GravesX.event.GraveCreateEvent;
import dev.cwhead.GravesX.event.GraveExplodeEvent;
import dev.cwhead.GravesX.event.GraveOpenEvent;
import dev.cwhead.GravesX.event.GraveTimeoutEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GraveTestListener implements Listener {
   private final Graves plugin;

   public GraveTestListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onGraveCreate(GraveCreateEvent event) {
      this.plugin.testMessage(this.plugin.getEntityManager().getEntityName(event.getEntity()) + " created a grave");
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onGraveOpen(GraveOpenEvent event) {
      this.plugin.testMessage(event.getPlayer().getName() + " opened " + event.getGrave().getOwnerName() + "'s grave");
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onGraveClose(GraveCloseEvent event) {
      this.plugin.testMessage(event.getPlayer().getName() + " closed " + event.getGrave().getOwnerName() + "'s grave");
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onGraveBreak(GraveBreakEvent event) {
      this.plugin.testMessage(event.getPlayer().getName() + " broke " + event.getGrave().getOwnerName() + "'s grave");
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onGraveExplode(GraveExplodeEvent event) {
      this.plugin.testMessage(event.getGrave().getOwnerName() + "'s grave exploded");
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onGraveTimeout(GraveTimeoutEvent event) {
      this.plugin.testMessage(event.getGrave().getOwnerName() + "'s grave timed out");
      event.setLocation(event.getLocation());
   }
}
