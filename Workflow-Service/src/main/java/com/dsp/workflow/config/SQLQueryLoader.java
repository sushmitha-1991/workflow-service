/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 */
public class SQLQueryLoader {

	// Assuming you're using a properties file to load queries
	private static final String SQL_PROPERTIES_FILE = "sql.properties";
	private Properties sqlProperties;

	public SQLQueryLoader() {
		sqlProperties = new Properties();
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SQL_PROPERTIES_FILE)) {
			if (inputStream != null) {
				sqlProperties.load(inputStream);
			} else {
				throw new IOException("SQL Properties file not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Handle the exception as appropriate
		}
	}

	// Method to load a query by name from the properties file
	public String getQuery(String queryName) {
		return sqlProperties.getProperty(queryName);
	}
}
