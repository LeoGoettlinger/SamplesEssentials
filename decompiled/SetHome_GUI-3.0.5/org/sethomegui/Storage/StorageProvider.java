package org.sethomegui.Storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface StorageProvider {
   String getName();

   void initialize() throws Exception;

   void shutdown();

   PlayerRecord load(UUID var1) throws Exception;

   void save(UUID var1, PlayerRecord var2) throws Exception;

   void delete(UUID var1) throws Exception;

   Set<UUID> listPlayers() throws Exception;

   Map<UUID, Integer> countHomesByPlayer() throws Exception;
}
