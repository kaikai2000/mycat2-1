/**
 * Copyright (C) <2019>  <chen junwen>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */
package io.mycat.datasource.jdbc.datasource;

public interface TransactionSession {

  void setTransactionIsolation(int transactionIsolation);

  void begin();

  void commit();

  void rollback();

  boolean isInTransaction();

  void beforeDoAction();

  void afterDoAction();

  void setAutocommit(boolean autocommit);

  DsConnection getConnection(JdbcDataSource jdbcDataSource);

}