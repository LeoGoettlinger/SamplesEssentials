package com.lishid.openinv.util.profile.sqlite;

import com.lishid.openinv.util.StringMetric;
import java.sql.SQLException;
import org.sqlite.Function;

public class JaroWinklerFunction extends Function {
   protected void xFunc() throws SQLException {
      if (this.args() != 2) {
         throw new SQLException("JaroWinkler(str, str) requires 2 arguments but got " + this.args());
      } else {
         String val1 = this.value_text(0);
         String val2 = this.value_text(1);
         this.result(StringMetric.compareJaroWinkler(val1 == null ? "" : val1, val2 == null ? "" : val2));
      }
   }
}
