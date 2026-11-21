package eu.kennytv.maintenance.core.dump;

import java.util.List;

public record PluginDump(String name, String version, List<String> authors) {
   public PluginDump(final String name, final String version, final List<String> authors) {
      this.name = name;
      this.version = version;
      this.authors = authors != null && authors.isEmpty() ? null : authors;
   }
}
