package com.viaversion.viaversion.protocol.shared_registration;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

public final class SharedRegistrations {
   private static final SharedRegistrations DEFAULT_REGISTRATIONS = new SharedRegistrations();
   private final EnumMap<VersionType, List<VersionedTemplateGroup>> versionedTemplates = new EnumMap<>(VersionType.class);

   public static SharedRegistrations defaultRegistrations() {
      return DEFAULT_REGISTRATIONS;
   }

   public static SharedRegistrations create() {
      return new SharedRegistrations();
   }

   public RegistrationBuilder registrations() {
      return new RegistrationBuilder(this);
   }

   public void applyMatching(AbstractProtocol<?, ?, ?, ?> protocol) {
      if (!protocol.isBaseProtocol()) {
         ProtocolVersion version = protocol.getServerVersion();
         if (version.getVersionType() == protocol.getClientVersion().getVersionType()) {
            List<VersionedTemplateGroup> groups = this.versionedTemplates.get(version.getVersionType());
            if (groups != null) {
               for (VersionedTemplateGroup group : groups) {
                  if (group.min().newerThan(version)) {
                     break;
                  }

                  if (group.max() == null || !version.newerThanOrEqualTo(group.max())) {
                     RegistrationContext<?, ?> context = new RegistrationContext<>(protocol, group.min(), group.max());
                     group.action().accept(context);
                  }
               }
            }
         }
      }
   }

   void register(VersionType versionType, List<VersionedTemplateGroup> toAdd) {
      List<VersionedTemplateGroup> groups = this.versionedTemplates.computeIfAbsent(versionType, $ -> new ArrayList<>());
      groups.addAll(toAdd);
      groups.sort(Comparator.comparing(VersionedTemplateGroup::min, Comparator.nullsFirst(Comparator.naturalOrder())));
   }

   @FunctionalInterface
   public interface RegistrationAction<CU extends ClientboundPacketType, SU extends ServerboundPacketType> {
      void accept(RegistrationContext<CU, SU> var1);
   }
}
