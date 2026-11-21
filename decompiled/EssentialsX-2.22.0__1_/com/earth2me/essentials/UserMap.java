package com.earth2me.essentials;

import com.earth2me.essentials.api.UserDoesNotExistException;
import com.earth2me.essentials.userstorage.ModernUserMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@Deprecated
public class UserMap {
   private final transient ModernUserMap userMap;
   private final transient UUIDMap uuidMap;

   public UserMap(final ModernUserMap userMap) {
      this.userMap = userMap;
      this.uuidMap = new UUIDMap();
   }

   public User getUser(final String name) {
      return this.userMap.getUser(name);
   }

   public User getUser(final UUID uuid) {
      return this.userMap.getUser(uuid);
   }

   public void trackUUID(final UUID uuid, final String name, final boolean replace) {
   }

   public User load(final String stringUUID) throws Exception {
      return this.userMap.load(UUID.fromString(stringUUID));
   }

   public User load(final OfflinePlayer player) throws UserDoesNotExistException {
      Player userPlayer;
      if (player instanceof Player) {
         userPlayer = (Player)player;
      } else {
         OfflinePlayerStub essPlayer = new OfflinePlayerStub(player.getUniqueId(), Bukkit.getServer());
         essPlayer.setName(player.getName());
         userPlayer = essPlayer;
      }

      User user = this.userMap.getUser(userPlayer);
      if (user == null) {
         throw new UserDoesNotExistException("User not found");
      } else {
         return user;
      }
   }

   public Set<UUID> getAllUniqueUsers() {
      return this.userMap.getAllUserUUIDs();
   }

   public int getUniqueUsers() {
      return this.userMap.getUserCount();
   }

   protected ConcurrentSkipListMap<String, UUID> getNames() {
      return new ConcurrentSkipListMap<>(this.userMap.getNameCache());
   }

   public UUIDMap getUUIDMap() {
      return this.uuidMap;
   }
}
