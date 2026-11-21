package com.sk89q.worldedit.util;

import java.util.UUID;

public interface Identifiable {
   UUID CONSOLE = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");
   UUID EVERYONE = UUID.fromString("1-1-3-3-7");

   UUID getUniqueId();
}
