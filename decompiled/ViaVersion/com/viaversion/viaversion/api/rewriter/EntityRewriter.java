package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EntityRewriter<T extends Protocol<?, ?, ?, ?>> extends Rewriter<T> {
   EntityType typeFromId(int var1);

   @Nullable
   EntityType typeFromId(String var1);

   default EntityType objectTypeFromId(int type, int data) {
      return this.typeFromId(type);
   }

   int newEntityId(int var1);

   String mappedEntityIdentifier(String var1);

   void handleEntityData(int var1, List<EntityData> var2, UserConnection var3);

   default <E extends EntityTracker> E tracker(UserConnection connection) {
      return connection.getEntityTracker(this.protocol());
   }
}
