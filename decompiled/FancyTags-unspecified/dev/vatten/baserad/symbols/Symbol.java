package dev.vatten.baserad.symbols;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.Polymorphic;
import net.kyori.adventure.text.Component;

@Polymorphic
@Configuration
public abstract class Symbol {
   public abstract Component serialize();
}
