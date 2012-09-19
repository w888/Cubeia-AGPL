/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.admin.db;

import java.util.List;

import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentConfiguration;
import com.cubeia.games.poker.tournament.configuration.SitAndGoConfiguration;
import org.apache.log4j.Logger;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.cubeia.games.poker.tournament.configuration.TournamentConfiguration;


/**
 * Base class for handling persistence.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
@Transactional
public class AbstractDAO extends JpaDaoSupport implements AdminDAO {

    @SuppressWarnings("unused")
    private static transient Logger log = Logger.getLogger(AbstractDAO.class);

    /* (non-Javadoc)
     * @see com.cubeia.games.poker.admin.db.AdminDAO#getItem(java.lang.Class, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public <T> T getItem(Class<T> class1, Integer id) {
        return (T) getJpaTemplate().find(class1, id);
    }
    
    /* (non-Javadoc)
     * @see com.cubeia.games.poker.admin.db.AdminDAO#persist(java.lang.Object)
     */
    public void persist(Object entity) {
        getJpaTemplate().persist(entity);
    }

    public void save(Object entity) {
        getJpaTemplate().merge(entity);
    }

    @Override
    public List<SitAndGoConfiguration> getSitAndGoConfigurations() {
        return getJpaTemplate().find("from SitAndGoConfiguration");
    }

    @Override
    public List<ScheduledTournamentConfiguration> getScheduledTournamentConfigurations() {
        return getJpaTemplate().find("from ScheduledTournamentConfiguration");
    }


}
