package net.luckperms.api.platform;

import java.util.Map;

public interface Health {
   boolean isHealthy();

   Map<String, Object> getDetails();
}
