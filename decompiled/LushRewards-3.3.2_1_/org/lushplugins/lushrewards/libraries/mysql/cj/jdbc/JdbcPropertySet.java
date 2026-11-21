package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.List;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;

public interface JdbcPropertySet extends PropertySet {
   List<DriverPropertyInfo> exposeAsDriverPropertyInfo() throws SQLException;
}
