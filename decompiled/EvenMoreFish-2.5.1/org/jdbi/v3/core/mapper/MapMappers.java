package org.jdbi.v3.core.mapper;

import java.util.function.UnaryOperator;
import org.jdbi.v3.core.config.JdbiConfig;

public class MapMappers implements JdbiConfig<MapMappers> {
   private UnaryOperator<String> caseChange;

   public MapMappers() {
      this.caseChange = CaseStrategy.LOCALE_LOWER;
   }

   private MapMappers(MapMappers that) {
      this.caseChange = that.caseChange;
   }

   public UnaryOperator<String> getCaseChange() {
      return this.caseChange;
   }

   public MapMappers setCaseChange(UnaryOperator<String> caseChange) {
      this.caseChange = caseChange;
      return this;
   }

   public MapMappers createCopy() {
      return new MapMappers(this);
   }
}
