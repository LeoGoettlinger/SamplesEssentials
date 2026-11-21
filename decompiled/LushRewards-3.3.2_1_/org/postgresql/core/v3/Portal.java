package org.postgresql.core.v3;

import java.lang.ref.PhantomReference;
import java.nio.charset.StandardCharsets;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ResultCursor;

class Portal implements ResultCursor {
   @Nullable
   private final SimpleQuery query;
   private final String portalName;
   private final byte[] encodedName;
   @Nullable
   private PhantomReference<?> cleanupRef;

   Portal(@Nullable SimpleQuery query, String portalName) {
      this.query = query;
      this.portalName = portalName;
      this.encodedName = portalName.getBytes(StandardCharsets.UTF_8);
   }

   @Override
   public void close() {
      PhantomReference<?> cleanupRef = this.cleanupRef;
      if (cleanupRef != null) {
         cleanupRef.clear();
         cleanupRef.enqueue();
         this.cleanupRef = null;
      }
   }

   String getPortalName() {
      return this.portalName;
   }

   byte[] getEncodedPortalName() {
      return this.encodedName;
   }

   @Nullable
   SimpleQuery getQuery() {
      return this.query;
   }

   void setCleanupRef(PhantomReference<?> cleanupRef) {
      this.cleanupRef = cleanupRef;
   }

   @Override
   public String toString() {
      return this.portalName;
   }
}
