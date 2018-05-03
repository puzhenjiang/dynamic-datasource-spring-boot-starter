/**
 * Copyright © 2018 TaoYu (tracy5546@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Slf4j
public abstract class AbstractDynamicDataSourceProvider implements DynamicDataSourceProvider {

  protected DataSource createDataSource(DataSourceProperties properties) {
    Class<? extends DataSource> type = properties.getType();
    if (type == null) {
      try {
        Class.forName("com.alibaba.druid.pool.DruidDataSource");
        return createDruidDataSource(properties);
      } catch (ClassNotFoundException e) {
        log.debug("dynamic not found DruidDataSource");
      }
      try {
        Class.forName("com.zaxxer.hikari.HikariDataSource");
        return createHikariDataSource(properties);
      } catch (ClassNotFoundException e) {
        log.debug("dynamic not found HikariDataSource");
      }
      throw new RuntimeException(
          "please set master and slave type like spring.dynamic.datasource.master.type");
    } else {
      if ("com.alibaba.druid.pool.DruidDataSource".equals(type.getName())) {
        return createDruidDataSource(properties);
      } else {
        return properties.initializeDataSourceBuilder().build();
      }
    }
  }

  private DataSource createHikariDataSource(DataSourceProperties properties) {
    properties.setType(HikariDataSource.class);
    return properties.initializeDataSourceBuilder().build();
  }

  private DataSource createDruidDataSource(DataSourceProperties properties) {
    DruidDataSource druidDataSource = new DruidDataSource();
    druidDataSource.setUrl(properties.getUrl());
    druidDataSource.setUsername(properties.getUsername());
    druidDataSource.setPassword(properties.getPassword());
    druidDataSource.setDriverClassName(properties.getDriverClassName());
    try {
      druidDataSource.setFilters("stat,wall");
      druidDataSource.init();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return druidDataSource;
  }

}
