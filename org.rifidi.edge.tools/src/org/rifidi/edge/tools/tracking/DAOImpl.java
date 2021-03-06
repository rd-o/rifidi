/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
package org.rifidi.edge.tools.tracking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * An implementation of the ProductsDAO and the LogicalReadersDAO
 * 
 * @author Kyle Neumeier - kyle@pramari.comF
 * 
 */
public class DAOImpl implements ProductsDAO, LogicalReadersDAO {

	/** The Spring datasource object */
	private SimpleDriverDataSource datasource = null;
	private static final Log logger = LogFactory.getLog(DAOImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.tools.tracking.dao.ProductsDAO#getProductName(java.lang
	 * .String)
	 */
	@Override
	public String getProductName(String ID) {
		if (datasource == null)
			return ID;
		try {
			Connection conn = datasource.getConnection();
			if (conn == null) {
				return ID;
			}
			Statement statement = conn.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT name FROM products where tagid = "
							+ ID.toUpperCase());
			while (rs.next()) {
				String name = rs.getString("name");
				if (name != null) {
					return name;
				}
			}
		} catch (SQLException e) {
			logger.debug(e.getMessage());
			// ignore the SQL exception
		}

		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.tools.tracking.dao.LogicalReadersDAO#getLogicaReaderName
	 * (java.lang.String, int)
	 */
	@Override
	public String getLogicaReaderName(String readerID, int antenna) {
		if (datasource == null)
			return new String(readerID + ":" + antenna);

		try {
			Connection conn = datasource.getConnection();
			if (conn == null) {
				return new String(readerID + ":" + antenna);
			}
			Statement statement = conn.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT name FROM logicalreaders where readerid = "
							+ readerID);
			while (rs.next()) {
				// For some reason, I couldn't get AND operator to work in where
				// clause. must filter antenna here.
				int rowAntenna = rs.getInt("antenna");
				if (rowAntenna == antenna) {
					String name = rs.getString("name");
					if (name != null) {
						return name;
					}
				}

			}
		} catch (SQLException e) {
			logger.debug("SQLException: " + e.getMessage());
		}

		return new String(readerID + ":" + antenna);
	}

	/**
	 * Set by spring
	 * 
	 * @param datasource
	 *            the datasource to set
	 */
	public void setDatasource(SimpleDriverDataSource datasource) {
		this.datasource = datasource;
	}

}
