package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.annotation.SynchronousSettingExpected;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotRestore;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class SnapshotUtilCommands {
   private final WorldEdit we;
   private final LegacySnapshotUtilCommands legacy;

   public SnapshotUtilCommands(WorldEdit we) {
      this.we = we;
      this.legacy = new LegacySnapshotUtilCommands(we);
   }

   @Command(
      name = "restore",
      aliases = {"/restore"},
      desc = "Restore the selection from a snapshot"
   )
   @Logging(Logging.LogMode.REGION)
   @CommandPermissions({"worldedit.snapshots.restore"})
   @SynchronousSettingExpected
   public void restore(
      Actor actor,
      World world,
      LocalSession session,
      EditSession editSession,
      @Arg(name = "snapshot",desc = "The snapshot to restore",def = {""}) String snapshotName,
      @Switch(name = 'b',desc = "If biomes should be restored. If restoring from pre-1.15 to 1.15+, biomes may not be exactly the same due to 3D biomes.") boolean restoreBiomes,
      @Switch(name = 'e',desc = "If entities should be restored. Will cause issues with duplicate entities if all original entities were not removed.") boolean restoreEntities
   ) throws WorldEditException, IOException {
      LocalConfiguration config = this.we.getConfiguration();
      SnapshotCommands.checkSnapshotsConfigured(config);
      if (config.snapshotRepo != null) {
         this.legacy.restore(actor, world, session, editSession, snapshotName, restoreBiomes, restoreEntities);
      } else {
         Region region = session.getSelection(world);
         Snapshot snapshot;
         if (snapshotName != null) {
            URI uri = SnapshotCommands.resolveSnapshotName(config, snapshotName);
            Optional<Snapshot> snapOpt = config.snapshotDatabase.getSnapshot(uri);
            if (snapOpt.isEmpty()) {
               actor.print(Caption.of("worldedit.restore.not-available"));
               return;
            }

            snapshot = snapOpt.get();
         } else {
            snapshot = session.getSnapshotExperimental();
         }

         if (snapshot == null) {
            try (Stream<Snapshot> snapshotStream = config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
               snapshot = snapshotStream.findFirst().orElse(null);
            }

            if (snapshot == null) {
               actor.print(Caption.of("worldedit.restore.none-for-specific-world", TextComponent.of(world.getName())));
               return;
            }
         }

         actor.print(Caption.of("worldedit.restore.loaded", TextComponent.of(snapshot.getInfo().getDisplayName())));

         try {
            SnapshotRestore restore = new SnapshotRestore(snapshot, editSession, region, restoreBiomes, restoreEntities);
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
                  Caption.of(
                     "worldedit.restore.restored", TextComponent.of(restore.getMissingChunks().size()), TextComponent.of(restore.getErrorChunks().size())
                  )
               );
            }
         } finally {
            try {
               snapshot.close();
            } catch (IOException var21) {
            }
         }
      }
   }
}
