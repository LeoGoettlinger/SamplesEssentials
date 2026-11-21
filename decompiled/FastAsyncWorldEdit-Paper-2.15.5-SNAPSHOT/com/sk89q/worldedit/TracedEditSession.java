package com.sk89q.worldedit;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.util.ErrorReporting;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import javax.annotation.Nullable;

class TracedEditSession extends EditSession {
   private static final Cleaner cleaner = Cleaner.create();
   private final TracedEditSession.TraceRecord record;
   private final Cleanable cleanable;

   @Deprecated
   TracedEditSession(EventBus eventBus, @Nullable World world, int maxBlocks, @Nullable BlockBag blockBag, @Nullable Actor actor, boolean tracing) {
      super(new EditSessionBuilder(eventBus).world(world).maxBlocks(maxBlocks).blockBag(blockBag).actor(actor).tracing(tracing));
      this.record = new TracedEditSession.TraceRecord(actor);
      this.cleanable = cleaner.register(this, this.record);
   }

   @Override
   public void close() {
      try {
         super.close();
      } finally {
         this.record.committed = true;
         this.cleanable.clean();
      }
   }

   private static final class TraceRecord implements Runnable {
      private final Throwable stacktrace = new Throwable("An EditSession was not closed.");
      private final Actor actor;
      private volatile boolean committed = false;

      private TraceRecord(Actor actor) {
         this.actor = actor;
      }

      @Override
      public void run() {
         if (!this.committed) {
            WorldEdit.logger.warn("####### EDIT SESSION NOT CLOSED #######");
            WorldEdit.logger.warn("This means that some code did not close their EditSession.");
            WorldEdit.logger.warn("Here is a stacktrace from the creation of this EditSession:", this.stacktrace);
            ErrorReporting.trigger(this.actor, this.stacktrace);
         }
      }
   }
}
