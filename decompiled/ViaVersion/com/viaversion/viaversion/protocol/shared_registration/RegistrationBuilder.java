package com.viaversion.viaversion.protocol.shared_registration;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RegistrationBuilder {
   private final List<VersionedTemplateGroup> groups = new ArrayList<>();
   private final SharedRegistrations registrations;
   private VersionType versionType;

   RegistrationBuilder(SharedRegistrations registrations) {
      this.registrations = registrations;
   }

   public RegistrationBuilder since(ProtocolVersion min, SharedRegistrations.RegistrationAction<?, ?> action) {
      this.checkVersionType(min);
      this.groups.add(new VersionedTemplateGroup(action, min, null));
      return this;
   }

   public RegistrationBuilder range(ProtocolVersion min, ProtocolVersion max, SharedRegistrations.RegistrationAction<?, ?> action) {
      this.checkVersionType(min);
      this.checkVersionType(max);
      this.groups.add(new VersionedTemplateGroup(action, min, max));
      return this;
   }

   public <CU extends ClientboundPacketType, SU extends ServerboundPacketType> RegistrationBuilder ranges(
      ProtocolVersion min, Consumer<RegistrationBuilder.RangesBuilder<CU, SU>> consumer
   ) {
      consumer.accept(new RegistrationBuilder.RangesBuilder<>(min));
      return this;
   }

   public <CU extends ClientboundPacketType, SU extends ServerboundPacketType, R> RegistrationBuilder ranges(
      Function<RegistrationContext<CU, SU>, R> adapter, ProtocolVersion min, Consumer<RegistrationBuilder.TypedRangesBuilder<CU, SU, R>> consumer
   ) {
      consumer.accept(new RegistrationBuilder.TypedRangesBuilder<>(adapter, min));
      return this;
   }

   public void register() {
      this.registrations.register(this.versionType, this.groups);
   }

   private void checkVersionType(ProtocolVersion version) {
      if (this.versionType == null) {
         this.versionType = version.getVersionType();
      } else if (this.versionType != version.getVersionType()) {
         throw new IllegalArgumentException("Cannot mix different version types in the same registration builder");
      }
   }

   public class RangesBuilder<CU extends ClientboundPacketType, SU extends ServerboundPacketType> {
      private ProtocolVersion currentMin;
      private boolean completed;

      private RangesBuilder(ProtocolVersion min) {
         this.currentMin = min;
      }

      public RegistrationBuilder.RangesBuilder<CU, SU> to(ProtocolVersion max, SharedRegistrations.RegistrationAction<CU, SU> action) {
         Preconditions.checkState(!this.completed, "Range chain already completed");
         RegistrationBuilder.this.range(this.currentMin, max, action);
         this.currentMin = max;
         return this;
      }

      public RegistrationBuilder since(SharedRegistrations.RegistrationAction<CU, SU> action) {
         Preconditions.checkState(!this.completed, "Range chain already completed");
         this.completed = true;
         return RegistrationBuilder.this.since(this.currentMin, action);
      }
   }

   public final class TypedRangesBuilder<CU extends ClientboundPacketType, SU extends ServerboundPacketType, R>
      extends RegistrationBuilder.RangesBuilder<CU, SU> {
      private final Function<RegistrationContext<CU, SU>, R> adapter;

      private TypedRangesBuilder(Function<RegistrationContext<CU, SU>, R> adapter, ProtocolVersion min) {
         super(min);
         this.adapter = adapter;
      }

      public RegistrationBuilder.TypedRangesBuilder<CU, SU, R> to(ProtocolVersion max, RegistrationBuilder.TypedRegistrationAction<CU, SU, R> action) {
         this.to(max, this.wrapAction(action));
         return this;
      }

      public RegistrationBuilder.TypedRangesBuilder<CU, SU, R> since(RegistrationBuilder.TypedRegistrationAction<CU, SU, R> action) {
         this.since(this.wrapAction(action));
         return this;
      }

      private SharedRegistrations.RegistrationAction<CU, SU> wrapAction(RegistrationBuilder.TypedRegistrationAction<CU, SU, R> action) {
         return ctx -> {
            R rewriter = this.adapter.apply(ctx);
            if (rewriter != null) {
               action.accept(ctx, rewriter);
            }
         };
      }
   }

   @FunctionalInterface
   public interface TypedRegistrationAction<CU extends ClientboundPacketType, SU extends ServerboundPacketType, R> {
      void accept(RegistrationContext<CU, SU> var1, R var2);
   }
}
