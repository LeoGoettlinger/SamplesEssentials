package com.sk89q.worldedit.world.snapshot.experimental;

import com.google.common.collect.ComparisonChain;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class SnapshotInfo implements Comparable<SnapshotInfo> {
   private final URI name;
   private final ZonedDateTime dateTime;

   public static SnapshotInfo create(URI name, ZonedDateTime dateTime) {
      return new SnapshotInfo(name, dateTime);
   }

   private SnapshotInfo(URI name, ZonedDateTime dateTime) {
      this.name = name;
      this.dateTime = dateTime;
   }

   public URI getName() {
      return this.name;
   }

   public String getDisplayName() {
      return this.name.getScheme().equals("snapfs") ? this.name.getSchemeSpecificPart() : this.name.toString();
   }

   public ZonedDateTime getDateTime() {
      return this.dateTime;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SnapshotInfo that = (SnapshotInfo)o;
         return Objects.equals(this.name, that.name) && Objects.equals(this.dateTime, that.dateTime);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.dateTime);
   }

   @Override
   public String toString() {
      return "SnapshotInfo{name='" + this.name + "',date=" + this.dateTime + "}";
   }

   public int compareTo(SnapshotInfo o) {
      return ComparisonChain.start().compare(this.dateTime, o.dateTime).compare(this.name, o.name).result();
   }
}
