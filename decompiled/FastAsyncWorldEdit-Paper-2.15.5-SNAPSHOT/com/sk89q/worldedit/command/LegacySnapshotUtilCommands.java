package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.InvalidSnapshotException;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import com.sk89q.worldedit.world.snapshot.SnapshotRestore;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.MissingWorldException;
import java.io.File;
import java.io.IOException;

class LegacySnapshotUtilCommands {
   private final WorldEdit we;

   LegacySnapshotUtilCommands(WorldEdit we) {
      this.we = we;
   }

   void restore(Actor actor, World world, LocalSession session, EditSession editSession, String snapshotName, boolean restoreBiomes, boolean restoreEntities) throws WorldEditException {
      LocalConfiguration config = this.we.getConfiguration();
      Region region = session.getSelection(world);
      Snapshot snapshot;
      if (snapshotName != null) {
         try {
            snapshot = config.snapshotRepo.getSnapshot(snapshotName);
         } catch (InvalidSnapshotException var28) {
            actor.print(Caption.of("worldedit.restore.not-available"));
            return;
         }
      } else {
         snapshot = session.getSnapshot();
      }

      if (snapshot == null) {
         try {
            snapshot = config.snapshotRepo.getDefaultSnapshot(world.getName());
            if (snapshot == null) {
               actor.print(Caption.of("worldedit.restore.none-found-console"));
               File dir = config.snapshotRepo.getDirectory();

               try {
                  WorldEdit.logger.info("WorldEdit found no snapshots: looked in: " + dir.getCanonicalPath());
               } catch (IOException var24) {
                  WorldEdit.logger.info("WorldEdit found no snapshots: looked in (NON-RESOLVABLE PATH - does it exist?): " + dir.getPath());
               }

               return;
            }
         } catch (MissingWorldException var27) {
            actor.print(Caption.of("worldedit.restore.none-for-world"));
            return;
         }
      }

      ChunkStore chunkStore;
      try {
         chunkStore = snapshot.getChunkStore();
         actor.print(Caption.of("worldedit.restore.loaded", TextComponent.of(snapshot.getName())));
      } catch (IOException | DataException var26) {
         actor.print(Caption.of("worldedit.restore.failed", TextComponent.of(var26.getMessage())));
         return;
      }

      try {
         SnapshotRestore restore = new SnapshotRestore(chunkStore, editSession, region, restoreBiomes, restoreEntities);
         restore.restore();
         if (restore.hadTotalFailure()) {
            String error = restore.getLastErrorMessage();
            if (!restore.getMissingChunks().isEmpty()) {
               actor.print(Caption.of("worldedit.restore.chunk-not-present"));
            } else if (error != null) {
               actor.print(Caption.of("worldedit.restore.block-place-failed"));
               actor.print(Caption.of("worldedit.restore.block-place-error", TextComponent.of(error)));
            } else {
               actor.print(Caption.of("worldedit.restore.chunk-load-failed"));
            }
         } else {
            actor.print(
               Caption.of("worldedit.restore.restored", TextComponent.of(restore.getMissingChunks().size()), TextComponent.of(restore.getErrorChunks().size()))
            );
         }
      } finally {
         try {
            chunkStore.close();
         } catch (IOException var23) {
         }
      }
   }
}
