package com.magmaguy.elitemobs.magmacore.scripting;

import java.util.List;

record LuaMindProgramMetadata(String identifier, long revision, List<String> modules) {
   LuaMindProgramMetadata(String identifier, long revision, List<String> modules) {
      modules = List.copyOf(modules);
      this.identifier = identifier;
      this.revision = revision;
      this.modules = modules;
   }
}
