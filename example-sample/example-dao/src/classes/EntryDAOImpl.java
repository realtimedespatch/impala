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

package classes;

import java.util.Collection;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import interfaces.EntryDAO;

public class EntryDAOImpl extends HibernateDaoSupport implements EntryDAO {

    public void save(Entry entry) {
        getHibernateTemplate().save(entry);
    }

    @SuppressWarnings("unchecked")
    public Collection<Entry> getEntriesWithCount(int count) {
        List<?> result = getHibernateTemplate().find("from Entry entry where entry.count = ?", count);
		return (Collection<Entry>) result;
    }

    public void update(Entry entry) {
        getHibernateTemplate().update(entry);
    }

    public Entry findById(long id) {
        return (Entry) getHibernateTemplate().get(Entry.class, id);
    }

}
