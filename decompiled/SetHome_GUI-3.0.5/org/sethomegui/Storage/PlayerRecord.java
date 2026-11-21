package org.sethomegui.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerRecord {
   private final UUID uuid;
   private List<HomeEntry> homes = new ArrayList<>();
   private List<String> friends = new ArrayList<>();
   private String extraData;

   public PlayerRecord(UUID uuid) {
      this.uuid = uuid;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public List<HomeEntry> getHomes() {
      return this.homes;
   }

   public void setHomes(List<HomeEntry> homes) {
      this.homes = (List<HomeEntry>)(homes == null ? new ArrayList<>() : homes);
   }

   public List<String> getFriends() {
      return this.friends;
   }

   public void setFriends(List<String> friends) {
      this.friends = (List<String>)(friends == null ? new ArrayList<>() : friends);
   }

   public String getExtraData() {
      return this.extraData;
   }

   public void setExtraData(String extraData) {
      this.extraData = extraData;
   }

   public boolean isEmpty() {
      return this.homes.isEmpty() && this.friends.isEmpty() && (this.extraData == null || this.extraData.trim().isEmpty());
   }
}
