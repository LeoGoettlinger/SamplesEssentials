package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabase;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.exception.StopExecutionException;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class SnapshotCommands {
   private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
   private final WorldEdit we;
   private final LegacySnapshotCommands legacy;

   public SnapshotCommands(WorldEdit we) {
      this.we = we;
      this.legacy = new LegacySnapshotCommands(we);
   }

   static void checkSnapshotsConfigured(LocalConfiguration localConfiguration) {
      if (!localConfiguration.snapshotsConfigured) {
         throw new StopExecutionException(Caption.of("worldedit.restore.not-configured"));
      }
   }

   static URI resolveSnapshotName(LocalConfiguration config, String name) {
      if (!name.contains("://") && config.snapshotDatabase instanceof FileSystemSnapshotDatabase) {
         String newName = name;

         while (newName.startsWith("/")) {
            newName = newName.substring(1);
         }

         return FileSystemSnapshotDatabase.createUri(newName);
      } else {
         return URI.create(name);
      }
   }

   @Command(
      name = "list",
      desc = "List snapshots"
   )
   @CommandPermissions(
      value = {"worldedit.snapshots.list"},
      queued = false
   )
   void list(Actor actor, World world, @ArgFlag(name = 'p',desc = "Page of results to return",def = {"1"}) int page) throws WorldEditException, IOException {
      LocalConfiguration config = this.we.getConfiguration();
      checkSnapshotsConfigured(config);
      if (config.snapshotRepo != null) {
         this.legacy.list(actor, world, page);
      } else {
         List<Snapshot> snapshots;
         try (Stream<Snapshot> snapshotStream = config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshots = snapshotStream.collect(Collectors.toList());
         }

         if (!snapshots.isEmpty()) {
            actor.print(new SnapshotCommands.SnapshotListBox(world.getName(), snapshots).create(page));
         } else {
            actor.print(Caption.of("worldedit.restore.none-for-specific-world", TextComponent.of(world.getName())));
            if (config.snapshotDatabase instanceof FileSystemSnapshotDatabase db) {
               Path root = db.getRoot();
               if (Files.isDirectory(root)) {
                  WorldEdit.logger.info("No snapshots were found for world '" + world.getName() + "'; looked in " + root.toRealPath());
               } else {
                  WorldEdit.logger.info("No snapshots were found for world '" + world.getName() + "'; " + root.toRealPath() + " is not a directory");
               }
            }
         }
      }
   }

   @Command(
      name = "use",
      desc = "Choose a snapshot to use"
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   void use(Actor actor, World world, LocalSession session, @Arg(desc = "Snapshot to use") String name) throws IOException {
      LocalConfiguration config = this.we.getConfiguration();
      checkSnapshotsConfigured(config);
      if (config.snapshotRepo != null) {
         this.legacy.use(actor, world, session, name);
      } else {
         if (name.equalsIgnoreCase("latest")) {
            Snapshot snapshot;
            try (Stream<Snapshot> snapshotStream = config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
               snapshot = snapshotStream.findFirst().orElse(null);
            }

            if (snapshot != null) {
               if (session.getSnapshotExperimental() != null) {
                  session.getSnapshotExperimental().close();
               }

               session.setSnapshot(null);
               actor.print(Caption.of("worldedit.snapshot.use.newest"));
            } else {
               actor.print(Caption.of("worldedit.restore.none-for-world"));
            }
         } else {
            URI uri = resolveSnapshotName(config, name);
            Optional<Snapshot> snapshotx = config.snapshotDatabase.getSnapshot(uri);
            if (snapshotx.isPresent()) {
               if (session.getSnapshotExperimental() != null) {
                  session.getSnapshotExperimental().close();
               }

               session.setSnapshotExperimental(snapshotx.get());
               actor.print(Caption.of("worldedit.snapshot.use", TextComponent.of(name)));
            } else {
               actor.print(Caption.of("worldedit.restore.not-available"));
            }
         }
      }
   }

   @Command(
      name = "sel",
      desc = "Choose the snapshot based on the list id"
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   void sel(Actor actor, World world, LocalSession session, @Arg(desc = "The list ID to select") int index) throws IOException {
      LocalConfiguration config = this.we.getConfiguration();
      checkSnapshotsConfigured(config);
      if (config.snapshotRepo != null) {
         this.legacy.sel(actor, world, session, index);
      } else if (index < 1) {
         actor.print(Caption.of("worldedit.snapshot.index-above-0"));
      } else {
         List<Snapshot> snapshots;
         try (Stream<Snapshot> snapshotStream = config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshots = snapshotStream.collect(Collectors.toList());
         }

         if (snapshots.size() < index) {
            actor.print(Caption.of("worldedit.snapshot.index-oob", TextComponent.of(snapshots.size())));
         } else {
            Snapshot snapshot = snapshots.get(index - 1);
            if (snapshot == null) {
               actor.print(Caption.of("worldedit.restore.not-available"));
            } else {
               if (session.getSnapshotExperimental() != null) {
                  session.getSnapshotExperimental().close();
               }

               session.setSnapshotExperimental(snapshot);
               actor.print(Caption.of("worldedit.snapshot.use", TextComponent.of(snapshot.getInfo().getDisplayName())));
            }
         }
      }
   }

   @Command(
      name = "before",
      desc = "Choose the nearest snapshot before a date"
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   void before(Actor actor, World world, LocalSession session, @Arg(desc = "The soonest date that may be used") ZonedDateTime date) throws IOException {
      LocalConfiguration config = this.we.getConfiguration();
      checkSnapshotsConfigured(config);
      if (config.snapshotRepo != null) {
         this.legacy.before(actor, world, session, date);
      } else {
         Snapshot snapshot;
         try (Stream<Snapshot> snapshotStream = config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshot = snapshotStream.findFirst().orElse(null);
         }

         if (snapshot == null) {
            actor.print(Caption.of("worldedit.snapshot.none-before", TextComponent.of(dateFormat.withZone(session.getTimeZone()).format(date))));
         } else {
            if (session.getSnapshotExperimental() != null) {
               session.getSnapshotExperimental().close();
            }

            session.setSnapshotExperimental(snapshot);
            actor.print(Caption.of("worldedit.snapshot.use", TextComponent.of(snapshot.getInfo().getDisplayName())));
         }
      }
   }

   @Command(
      name = "after",
      desc = "Choose the nearest snapshot after a date"
   )
   @CommandPermissions({"worldedit.snapshots.restore"})
   void after(Actor actor, World world, LocalSession session, @Arg(desc = "The soonest date that may be used") ZonedDateTime date) throws IOException {
      LocalConfiguration config = this.we.getConfiguration();
      checkSnapshotsConfigured(config);
      if (config.snapshotRepo != null) {
         this.legacy.after(actor, world, session, date);
      } else {
         Snapshot snapshot;
         try (Stream<Snapshot> snapshotStream = config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshot = snapshotStream.findFirst().orElse(null);
         }

         if (snapshot == null) {
            actor.print(Caption.of("worldedit.snapshot.none-after", TextComponent.of(dateFormat.withZone(session.getTimeZone()).format(date))));
         } else {
            if (session.getSnapshotExperimental() != null) {
               session.getSnapshotExperimental().close();
            }

            session.setSnapshotExperimental(snapshot);
            actor.print(Caption.of("worldedit.snapshot.use", TextComponent.of(snapshot.getInfo().getDisplayName())));
         }
      }
   }

   private static class SnapshotListBox extends PaginationBox {
      private final List<Snapshot> snapshots;

      SnapshotListBox(String world, List<Snapshot> snapshots) {
         super("Snapshots for: " + world, "/snap list -p %page%");
         this.snapshots = snapshots;
      }

      @Override
      public Component getComponent(int number) {
         Snapshot snapshot = this.snapshots.get(number);
         String displayName = snapshot.getInfo().getDisplayName();
         return TextComponent.of(number + 1 + ". ", TextColor.GOLD)
            .append(
               TextComponent.builder(displayName, TextColor.LIGHT_PURPLE)
                  .hoverEvent(HoverEvent.showText(TextComponent.of("Click to use")))
                  .clickEvent(ClickEvent.runCommand("/snap use " + displayName))
            );
      }

      @Override
      public int getComponentsSize() {
         return this.snapshots.size();
      }
   }
}
