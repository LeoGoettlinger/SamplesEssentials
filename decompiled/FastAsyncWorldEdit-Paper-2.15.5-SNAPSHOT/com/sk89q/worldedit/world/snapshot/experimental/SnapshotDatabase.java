package com.sk89q.worldedit.world.snapshot.experimental;

import com.sk89q.worldedit.util.collection.MoreStreams;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

public interface SnapshotDatabase {
   String getScheme();

   Optional<Snapshot> getSnapshot(URI name) throws IOException;

   Stream<Snapshot> getSnapshots(String worldName) throws IOException;

   default Stream<Snapshot> getSnapshotsNewestFirst(String worldName) throws IOException {
      return this.getSnapshots(worldName).sorted(SnapshotComparator.getInstance().reversed());
   }

   default Stream<Snapshot> getSnapshotsOldestFirst(String worldName) throws IOException {
      return this.getSnapshots(worldName).sorted(SnapshotComparator.getInstance());
   }

   default Stream<Snapshot> getSnapshotsBefore(String worldName, ZonedDateTime date) throws IOException {
      return MoreStreams.takeWhile(this.getSnapshotsOldestFirst(worldName), snap -> snap.getInfo().getDateTime().isBefore(date));
   }

   default Stream<Snapshot> getSnapshotsAfter(String worldName, ZonedDateTime date) throws IOException {
      return MoreStreams.takeWhile(this.getSnapshotsNewestFirst(worldName), snap -> snap.getInfo().getDateTime().isAfter(date));
   }
}
