package com.viaversion.viaversion.protocol.shared_registration;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

record VersionedTemplateGroup(SharedRegistrations.RegistrationAction<?, ?> action, ProtocolVersion min, ProtocolVersion max) {
   public boolean contains(ProtocolVersion version) {
      return version.newerThanOrEqualTo(this.min) && (this.max == null || version.olderThan(this.max));
   }
}
