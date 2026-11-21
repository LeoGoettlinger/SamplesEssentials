package com.fastasyncworldedit.bukkit.listener;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.util.FaweTimer;
import com.fastasyncworldedit.core.util.MathMan;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;

@Deprecated(
   since = "2.0.0"
)
public class ChunkListener9 extends ChunkListener {
   private Exception exception;
   private StackTraceElement[] elements;

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   @Override
   public void onPhysics(BlockPhysicsEvent event) {
      if (physicsFreeze) {
         event.setCancelled(true);
      } else {
         if (this.physCancel) {
            Block block = event.getBlock();
            long pair = MathMan.pairInt(block.getX() >> 4, block.getZ() >> 4);
            if (this.physCancelPair == pair) {
               event.setCancelled(true);
               return;
            }

            if (this.badChunks.containsKey(pair)) {
               this.physCancelPair = pair;
               event.setCancelled(true);
               return;
            }

            if (System.currentTimeMillis() - this.physStart > Settings.settings().TICK_LIMITER.PHYSICS_MS) {
               this.physCancelPair = pair;
               event.setCancelled(true);
               return;
            }
         }

         FaweTimer timer = Fawe.instance().getTimer();
         if (timer.getTick() != this.physTick) {
            this.physTick = timer.getTick();
            this.physStart = System.currentTimeMillis();
            this.physSkip = 0;
            this.physCancel = false;
         } else {
            if ((++this.physSkip & 1023) == 0 && System.currentTimeMillis() - this.physStart > Settings.settings().TICK_LIMITER.PHYSICS_MS) {
               Block blockx = event.getBlock();
               int cx = blockx.getX() >> 4;
               int cz = blockx.getZ() >> 4;
               this.physCancelPair = MathMan.pairInt(cx, cz);
               if (this.rateLimit <= 0) {
                  this.rateLimit = 20;
                  this.lastCancelPos = blockx.getLocation();
               }

               this.cancelNearby(cx, cz);
               event.setCancelled(true);
               this.physCancel = true;
            }
         }
      }
   }

   private StackTraceElement[] getElements(Exception ex) {
      if (this.elements == null || ex != this.exception) {
         this.exception = ex;
         this.elements = ex.getStackTrace();
      }

      return this.elements;
   }

   @Override
   protected int getDepth(Exception ex) {
      return this.getElements(ex).length;
   }

   @Override
   protected StackTraceElement getElement(Exception ex, int i) {
      StackTraceElement[] elems = this.getElements(ex);
      return elems.length > i ? elems[i] : null;
   }
}
