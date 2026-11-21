package org.jdbi.v3.core.argument;

import java.util.Objects;
import org.jdbi.v3.core.config.ConfigRegistry;

public class CharSequenceArgumentFactory extends AbstractArgumentFactory<CharSequence> {
   public CharSequenceArgumentFactory() {
      super(12);
   }

   protected Argument build(CharSequence value, ConfigRegistry config) {
      return (position, statement, ctx) -> statement.setString(position, Objects.toString(value, null));
   }
}
