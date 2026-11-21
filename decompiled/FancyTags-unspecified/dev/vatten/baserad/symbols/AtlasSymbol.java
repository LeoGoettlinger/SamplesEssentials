package dev.vatten.baserad.symbols;

import de.exlll.configlib.Configuration;
import lombok.Generated;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

@Configuration
public class AtlasSymbol extends Symbol {
   private String atlas;
   private String sprite;

   public AtlasSymbol(String atlas, String sprite) {
      this.atlas = atlas;
      this.sprite = sprite;
   }

   @Override
   public Component serialize() {
      return Component.object(ObjectContents.sprite(Key.key(this.atlas), Key.key(this.sprite)));
   }

   @Generated
   public AtlasSymbol() {
   }
}
