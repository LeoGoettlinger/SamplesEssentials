package org.mineskin;

import java.util.logging.Logger;

public interface MineSkinClient {
   static ClientBuilder builder() {
      return ClientBuilder.create();
   }

   QueueClient queue();

   GenerateClient generate();

   SkinsClient skins();

   MiscClient misc();

   Logger getLogger();
}
