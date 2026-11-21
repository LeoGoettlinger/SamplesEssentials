package org.jdbi.v3.core.mapper.reflect;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.mapper.CaseStrategy;
import org.jdbi.v3.meta.Alpha;

public class ReflectionMappers implements JdbiConfig<ReflectionMappers> {
   private List<ColumnNameMatcher> columnNameMatchers;
   private boolean strictMatching;
   private UnaryOperator<String> caseChange;
   private Consumer<AccessibleObject> makeAccessible;

   public ReflectionMappers() {
      this.columnNameMatchers = Arrays.asList(new CaseInsensitiveColumnNameMatcher(), new SnakeCaseColumnNameMatcher());
      this.strictMatching = false;
      this.caseChange = CaseStrategy.LOCALE_LOWER;
      this.makeAccessible = AccessibleObjectStrategy.FORCE_MAKE_ACCESSIBLE;
   }

   private ReflectionMappers(ReflectionMappers that) {
      this.columnNameMatchers = new ArrayList<>(that.columnNameMatchers);
      this.strictMatching = that.strictMatching;
      this.caseChange = that.caseChange;
      this.makeAccessible = that.makeAccessible;
   }

   public List<ColumnNameMatcher> getColumnNameMatchers() {
      return Collections.unmodifiableList(this.columnNameMatchers);
   }

   public ReflectionMappers setColumnNameMatchers(List<ColumnNameMatcher> columnNameMatchers) {
      this.columnNameMatchers = new ArrayList<>(columnNameMatchers);
      return this;
   }

   public boolean isStrictMatching() {
      return this.strictMatching;
   }

   public ReflectionMappers setStrictMatching(boolean strictMatching) {
      this.strictMatching = strictMatching;
      return this;
   }

   public UnaryOperator<String> getCaseChange() {
      return this.caseChange;
   }

   public ReflectionMappers setCaseChange(UnaryOperator<String> caseChange) {
      this.caseChange = caseChange;
      return this;
   }

   @Alpha
   public ReflectionMappers setAccessibleObjectStrategy(Consumer<AccessibleObject> makeAccessible) {
      this.makeAccessible = makeAccessible;
      return this;
   }

   @Alpha
   public ReflectionMappers disableAccessibleObjectStrategy() {
      this.makeAccessible = AccessibleObjectStrategy.DO_NOT_MAKE_ACCESSIBLE;
      return this;
   }

   @Alpha
   public <T extends AccessibleObject> T makeAccessible(T accessibleObject) {
      this.makeAccessible.accept(accessibleObject);
      return accessibleObject;
   }

   public ReflectionMappers createCopy() {
      return new ReflectionMappers(this);
   }
}
