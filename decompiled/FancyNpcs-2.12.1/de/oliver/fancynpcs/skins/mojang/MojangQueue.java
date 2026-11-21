package de.oliver.fancynpcs.skins.mojang;

import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.skins.SkinGeneratedEvent;
import de.oliver.fancynpcs.skins.SkinGenerationQueue;
import de.oliver.fancynpcs.skins.SkinGenerationRequest;
import de.oliver.fancynpcs.skins.SkinManagerImpl;
import de.oliver.fancynpcs.skins.mineskin.RatelimitException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MojangQueue implements SkinGenerationQueue {
   private static MojangQueue INSTANCE;
   private final Queue<SkinGenerationRequest> queue;
   private final MojangAPI api;
   private ScheduledFuture<?> scheduler;
   private long nextRequestTime = System.currentTimeMillis();

   private MojangQueue() {
      this.queue = new LinkedList<>();
      this.api = new MojangAPI(SkinManagerImpl.EXECUTOR);
      this.run();
   }

   public static MojangQueue get() {
      if (INSTANCE == null) {
         INSTANCE = new MojangQueue();
      }

      return INSTANCE;
   }

   @Override
   public void run() {
      this.scheduler = SkinManagerImpl.EXECUTOR.scheduleWithFixedDelay(this::pollMany, 5L, 1L, TimeUnit.SECONDS);
   }

   private void pollMany() {
      for (int i = 0; i < 5; i++) {
         this.poll();
      }
   }

   private void poll() {
      if (!this.queue.isEmpty()) {
         if (System.currentTimeMillis() < this.nextRequestTime) {
            FancyNpcs.getInstance()
               .getFancyLogger()
               .debug("Retrying to fetch skin from Mojang in " + (this.nextRequestTime - System.currentTimeMillis()) + "ms");
         } else {
            SkinGenerationRequest req = this.queue.poll();
            if (req != null) {
               try {
                  FancyNpcs.getInstance().getFancyLogger().debug("Fetching skin from Mojang: " + req.getID());
                  SkinData skinData = this.api.fetchSkin(req.getID(), req.getVariant());
                  new SkinGeneratedEvent(req.getID(), skinData).callEvent();
               } catch (RatelimitException var3) {
                  this.nextRequestTime = var3.getNextRequestTime();
                  this.queue.add(req);
                  FancyNpcs.getInstance()
                     .getFancyLogger()
                     .debug("Failed to generate skin: ratelimited by Mojang, retrying in " + (this.nextRequestTime - System.currentTimeMillis()) + "ms");
                  return;
               }

               this.nextRequestTime = System.currentTimeMillis();
            }
         }
      }
   }

   @Override
   public void add(SkinGenerationRequest req) {
      for (SkinGenerationRequest r : this.queue) {
         if (r.getID().equals(req.getID())) {
            return;
         }
      }

      this.queue.add(req);
   }

   @Override
   public void clear() {
      this.queue.clear();
   }

   @Override
   public ScheduledFuture<?> getScheduler() {
      return this.scheduler;
   }
}
