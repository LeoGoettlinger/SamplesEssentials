package de.oliver.fancyanalytics.sdk.logs;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record LogRecord(
   @SerializedName("project_id") String projectId, String service, Instant timestamp, String level, String message, Map<String, String> properties
) {
}
