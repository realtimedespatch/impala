/*
 * Copyright 2007-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package test;

import javax.sql.DataSource;

import org.impalaframework.facade.Impala;
import org.impalaframework.module.ModuleDefinitionSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class BaseDataTest extends BaseIntegrationTest implements ModuleDefinitionSource {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        final DataSource dataSource = Impala.getBean("dataSource", DataSource.class);

        try {
        PlatformTransactionManager bean = Impala.getBean("transactionManager", PlatformTransactionManager.class);
		new TransactionTemplate(bean).execute(new TransactionCallback<Object>() {

            public Object doInTransaction(TransactionStatus status) {
                new JdbcTemplate(dataSource).execute("delete from example_entry");
                return status;
            }
        }); } catch (Exception e) {}
    }
}
