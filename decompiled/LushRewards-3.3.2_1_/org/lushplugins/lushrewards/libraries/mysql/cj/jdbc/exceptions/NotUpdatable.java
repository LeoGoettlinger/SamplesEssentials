package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.exceptions;

import java.sql.SQLException;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;

public class NotUpdatable extends SQLException {
   private static final long serialVersionUID = 6004153665887216929L;

   public NotUpdatable(String reason) {
      super(reason + Messages.getString("NotUpdatable.1"), "S1000");
   }
}
