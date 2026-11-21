package net.thenextlvl.version.modrinth;

import com.google.gson.annotations.SerializedName;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ModrinthVersion(
   @SerializedName("name") String name,
   @SerializedName("version_number") String versionNumber,
   @SerializedName("loaders") Set<String> loaders,
   @SerializedName("game_versions") Set<String> gameVersions
) {
}
