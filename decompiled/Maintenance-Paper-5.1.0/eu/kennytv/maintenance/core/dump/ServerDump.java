package eu.kennytv.maintenance.core.dump;

import java.util.List;

public record ServerDump(String pluginVersion, String platform, String serverVersion, List<String> maintenance) {
}
