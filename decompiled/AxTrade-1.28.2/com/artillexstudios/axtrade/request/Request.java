package com.artillexstudios.axtrade.request;

import org.bukkit.entity.Player;

public class Request {
   private final long time;
   private final Player sender;
   private final Player receiver;
   private boolean active = true;

   public Request(Player sender, Player receiver) {
      this.time = System.currentTimeMillis();
      this.sender = sender;
      this.receiver = receiver;
   }

   public long getTime() {
      return this.time;
   }

   public Player getReceiver() {
      return this.receiver;
   }

   public Player getSender() {
      return this.sender;
   }

   public boolean isActive() {
      return this.active;
   }

   public void deactivate() {
      this.active = false;
   }
}
