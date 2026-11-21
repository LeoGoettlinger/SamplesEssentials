package com.viaversion.viaversion.protocols.v1_8to1_9.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;

public class EntityIdProvider implements Provider {
   public int getEntityId(UserConnection user) throws Exception {
      return user.<EntityTracker>getEntityTracker(Protocol1_8To1_9.class).clientEntityId();
   }
}
