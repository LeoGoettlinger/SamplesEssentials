package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.DefaultPropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyDefinitions;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.RuntimeProperty;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class JdbcPropertySetImpl extends DefaultPropertySet implements JdbcPropertySet {
   private static final long serialVersionUID = -8223499903182568260L;

   @Override
   public void postInitialization() {
      if (this.getIntegerProperty(PropertyKey.maxRows).getValue() == 0) {
         super.<Integer>getProperty(PropertyKey.maxRows).setValue(-1, null);
      }

      String testEncoding = this.getStringProperty(PropertyKey.characterEncoding).getValue();
      if (testEncoding != null) {
         String testString = "abc";
         StringUtils.getBytes(testString, testEncoding);
      }

      if (this.getBooleanProperty(PropertyKey.useCursorFetch).getValue()) {
         super.<Boolean>getProperty(PropertyKey.useServerPrepStmts).setValue(true);
      }
   }

   @Override
   public List<DriverPropertyInfo> exposeAsDriverPropertyInfo() throws SQLException {
      return PropertyDefinitions.PROPERTY_KEY_TO_PROPERTY_DEFINITION
         .entrySet()
         .stream()
         .filter(e -> !e.getValue().getCategory().equals(PropertyDefinitions.CATEGORY_XDEVAPI))
         .map(Entry::getKey)
         .map(this::getProperty)
         .map(this::getAsDriverPropertyInfo)
         .collect(Collectors.toList());
   }

   private DriverPropertyInfo getAsDriverPropertyInfo(RuntimeProperty<?> pr) {
      PropertyDefinition<?> pdef = pr.getPropertyDefinition();
      DriverPropertyInfo dpi = new DriverPropertyInfo(pdef.getName(), null);
      dpi.choices = pdef.getAllowableValues();
      dpi.value = pr.getStringValue() != null ? pr.getStringValue() : null;
      dpi.required = false;
      dpi.description = pdef.getDescription();
      return dpi;
   }
}
