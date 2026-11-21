package dev.vatten.baserad;

import de.exlll.configlib.Configuration;
import dev.vatten.baserad.symbols.Symbol;
import java.util.List;
import lombok.Generated;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Configuration
public class Tag implements Renderable {
   private List<Symbol> symbols;
   private transient Component component;
   private transient String miniMessage;

   public Tag(List<Symbol> symbols) {
      this.symbols = symbols;
   }

   Component serialize(MiniMessage miniMessage) {
      Builder componentBuilder = Component.text();

      for (Symbol symbol : this.symbols) {
         componentBuilder.append(symbol.serialize());
      }

      this.component = componentBuilder.build();
      this.miniMessage = (String)miniMessage.serialize(this.component);
      return this.component;
   }

   @Override
   public Component asComponent() {
      return this.component;
   }

   public String asMiniMessage() {
      return this.miniMessage;
   }

   @Generated
   public Tag() {
   }
}
