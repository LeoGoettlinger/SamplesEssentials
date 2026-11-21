package net.luckperms.api.actionlog.filter;

import java.util.UUID;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ActionFilterFactory {
   ActionFilter any();

   ActionFilter source(UUID var1);

   ActionFilter user(UUID var1);

   ActionFilter group(String var1);

   ActionFilter track(String var1);

   ActionFilter search(String var1);
}
