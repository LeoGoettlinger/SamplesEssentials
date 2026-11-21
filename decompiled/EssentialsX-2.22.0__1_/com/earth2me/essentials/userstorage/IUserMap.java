package com.earth2me.essentials.userstorage;

import com.earth2me.essentials.User;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface IUserMap {
   Set<UUID> getAllUserUUIDs();

   long getCachedCount();

   boolean isCached(final UUID uuid);

   int getUserCount();

   User getUser(final UUID uuid);

   User getUser(final Player base);

   User getUser(final String name);

   User loadUncachedUser(final Player base);

   User loadUncachedUser(final UUID uuid);

   Map<String, UUID> getNameCache();
}
