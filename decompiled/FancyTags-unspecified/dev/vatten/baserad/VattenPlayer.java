package dev.vatten.baserad;

import java.util.Collections;
import java.util.UUID;
import lombok.Generated;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

public class VattenPlayer implements ForwardingAudience {
   private final UUID uuid;
   private final String name;
   private final Audience audience;

   VattenPlayer(UUID uuid, String name, Audience audience) {
      this.uuid = uuid;
      this.name = name;
      this.audience = audience;
   }

   @NotNull
   public Iterable<? extends Audience> audiences() {
      return Collections.singleton(this.audience);
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public String getName() {
      return this.name;
   }
}
