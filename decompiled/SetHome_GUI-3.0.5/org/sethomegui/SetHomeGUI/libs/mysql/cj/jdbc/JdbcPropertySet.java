package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.List;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;

public interface JdbcPropertySet extends PropertySet {
   List<DriverPropertyInfo> exposeAsDriverPropertyInfo() throws SQLException;
}
