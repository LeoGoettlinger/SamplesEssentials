package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.elitemobs.api.mind.EliteMindSnapshot;
import com.magmaguy.elitemobs.magmacore.ai.MindHandle;
import com.magmaguy.elitemobs.magmacore.ai.MindSnapshot;
import com.magmaguy.elitemobs.magmacore.ai.MobBody;
import com.magmaguy.elitemobs.magmacore.ai.StateTransfer;
import com.magmaguy.elitemobs.magmacore.ai.SwapReceipt;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

final class EliteMindBinding implements AutoCloseable {
   private final MindHandle handle;
   private EliteMindProgramEntry activeEntry;
   private EliteMindProgramEntry pendingEntry;
   private long pendingGeneration = -1L;
   private boolean closed;
   private boolean paused;
   private boolean aiPaused;

   EliteMindBinding(EliteMindProgramEntry entry, MindHandle handle, MobBody body) {
      this.activeEntry = Objects.requireNonNull(entry, "entry");
      this.handle = Objects.requireNonNull(handle, "handle");
      this.attachBody(body);
   }

   boolean isOwnedBy(Plugin owner) {
      this.refreshCommittedEntry();
      return this.activeEntry.owner() == owner || this.pendingEntry != null && this.pendingEntry.owner() == owner;
   }

   void replace(EliteMindProgramEntry entry, StateTransfer transfer) {
      this.requireOpen();
      this.refreshCommittedEntry();
      Plugin expectedOwner = this.pendingEntry == null ? this.activeEntry.owner() : this.pendingEntry.owner();
      if (entry.owner() != expectedOwner) {
         throw new IllegalArgumentException("A Mind program cannot transfer actor ownership implicitly");
      } else {
         SwapReceipt receipt = this.handle.replace(entry.instantiate(), Objects.requireNonNull(transfer, "transfer"));
         if (receipt.queued()) {
            this.pendingEntry = entry;
            this.pendingGeneration = receipt.requestedGeneration();
         } else {
            this.activeEntry = entry;
            this.pendingEntry = null;
            this.pendingGeneration = -1L;
         }
      }
   }

   Plugin ownerForGeneration(long generation) {
      this.requireOpen();
      if (generation < 1L) {
         throw new IllegalArgumentException("generation must be positive");
      } else {
         return this.pendingEntry != null && generation >= this.pendingGeneration ? this.pendingEntry.owner() : this.activeEntry.owner();
      }
   }

   void attachBody(MobBody body) {
      this.requireOpen();
      this.handle.attachBody(Objects.requireNonNull(body, "body"));
   }

   void suspendBody() {
      this.requireOpen();
      this.handle.suspendBody();
   }

   void setPaused(boolean paused) {
      this.requireOpen();
      this.handle.setPaused(paused || this.aiPaused);
      this.paused = paused;
   }

   void setAiPaused(boolean paused) {
      this.requireOpen();
      this.handle.setPaused(this.paused || paused);
      this.aiPaused = paused;
   }

   boolean isPaused() {
      return this.paused || this.aiPaused;
   }

   boolean isExplicitlyPaused() {
      return this.paused;
   }

   EliteMindSnapshot snapshot(EliteEntity eliteEntity) {
      this.requireOpen();
      MindSnapshot snapshot = this.handle.inspect();
      this.refreshCommittedEntry(snapshot);
      NamespacedKey currentKey = NamespacedKey.fromString(snapshot.programIdentifier());
      if (currentKey == null) {
         throw new IllegalStateException("Native mind returned an invalid program key: " + snapshot.programIdentifier());
      } else {
         EliteMindProgram program = this.activeEntry.descriptor();
         if (program.key().equals(currentKey) && program.revision() == snapshot.programRevision()) {
            return new EliteMindSnapshot(
               eliteEntity.getEliteUUID(),
               currentKey,
               snapshot.programRevision(),
               snapshot.generation(),
               snapshot.activeBehaviors(),
               snapshot.bodyEntityId().isPresent(),
               snapshot.paused(),
               program.resolvedModules(),
               program.compositionFingerprint()
            );
         } else {
            throw new IllegalStateException("Native mind inspection does not match the EliteMobs program catalog");
         }
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;

         try {
            this.handle.detachBody();
         } finally {
            this.handle.close();
         }
      }
   }

   private void requireOpen() {
      if (this.closed) {
         throw new IllegalStateException("Elite mind binding is closed");
      }
   }

   private void refreshCommittedEntry() {
      if (this.pendingEntry != null) {
         this.refreshCommittedEntry(this.handle.inspect());
      }
   }

   private void refreshCommittedEntry(MindSnapshot snapshot) {
      if (this.pendingEntry != null && snapshot.generation() >= this.pendingGeneration) {
         this.activeEntry = this.pendingEntry;
         this.pendingEntry = null;
         this.pendingGeneration = -1L;
      }
   }
}
