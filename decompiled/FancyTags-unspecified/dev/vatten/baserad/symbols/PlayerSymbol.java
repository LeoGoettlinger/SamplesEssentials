package dev.vatten.baserad.symbols;

import de.exlll.configlib.Configuration;
import lombok.Generated;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

@Configuration
public class PlayerSymbol extends Symbol {
   private String value;
   private String signature;

   public PlayerSymbol(String value, String signature) {
      this.value = value;
      this.signature = signature;
   }

   @Override
   public Component serialize() {
      return Component.object(ObjectContents.playerHead().profileProperty(PlayerHeadObjectContents.property("textures", this.value, this.signature)).build());
   }

   @Generated
   public PlayerSymbol() {
   }
}
