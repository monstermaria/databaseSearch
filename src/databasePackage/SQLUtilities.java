package databasePackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class SQLUtilities {
	static Connection connection;
	
	static boolean hasConnection() throws SQLException {
		
		// connection already established
		if (connection != null) {
			return true;
		}
		
		// trying to establish connection, throws SQLException on fail
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/dumble?serverTimezone=UTC", "root", "");
		System.out.println("Database connection established");
		return true;
	}
	
	static ResultSet query(String sqlStatement) throws SQLException {
		Statement statement;
		ResultSet resultSet = null;
		
		if (!hasConnection()) {
			System.out.println("query: Database connection error");

			return resultSet;
		}
		
		statement = connection.createStatement();
		resultSet = statement.executeQuery(sqlStatement);

		return resultSet;
	}
	
	static ArrayList<String> getTableNames() throws SQLException {
		ArrayList<String> tableNames = new ArrayList<>();
		ResultSet resultSet = query("SHOW TABLES");

		if (resultSet == null) {
			System.out.println("getTableNames: no result");

			return tableNames;
		}
		
		while (resultSet.next()) {
			tableNames.add(resultSet.getString(1));
		}

		return tableNames;
	}
	
	static ArrayList<String> getColumnNames(String tableName) throws SQLException {
		ArrayList<String> columnNames = new ArrayList<>();
		String q = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = N'" + tableName + "'";
		ResultSet resultSet = query(q);
		
		if (resultSet == null) {
			System.out.println("getColumnNames: no result");
			return columnNames;
		}
		
		while (resultSet.next()) {
			columnNames.add(resultSet.getString(1));
		}

		return columnNames;
	}
	
	static ResultSet searchTable(String tableName, String searchTerm) throws SQLException {

//		System.out.println("Searching table " + tableName);
		
		// get the column names for current table
		ArrayList<String> columnNames = getColumnNames(tableName);
		
//		System.out.println("Columns: " + columnNames);
		
		// put together primary search
		String searchString = "SELECT * FROM " + tableName + " WHERE ";
		
		if (columnNames.size() == 0) {
			return null;
		}
		
		searchString += tableName + "." + columnNames.get(0) + " LIKE '%" + searchTerm + "%'";
		
		for (int i = 1; i < columnNames.size(); i++) {
			searchString += " OR " + tableName + "." + columnNames.get(i) + " LIKE '%" + searchTerm + "%'";
		}
		
//		System.out.println(searchString);
		
		// execute search
		return query(searchString);
		
	}
	
	static ArrayList<ResultSet> searchAllTables(String searchTerm) throws SQLException {
		
		ArrayList<ResultSet> searchResults = new ArrayList<>();
		
		System.out.println("Search all tables for " + searchTerm);
		
		if (searchTerm.contentEquals("")) {
			System.out.println("searchAllTables: Search term can't be empty");
			return searchResults;
		}
		
		// get the names of all tables
		ArrayList<String> tableNames = getTableNames();
		
		// search all tables
		for (String tableName : tableNames) {
						
			//get search result for current table
			ResultSet resultSet = searchTable(tableName, searchTerm);
			
			if (resultSet != null) {
				searchResults.add(resultSet);
			}
		}
		
		System.out.println("Results ArrayList.size(): " + searchResults.size());
		
		return searchResults;
	}
	
	static String addHtmlTableFromResultSet(ResultSet resultSet, String htmlResult) throws SQLException {
		
		String tableName = resultSet.getMetaData().getTableName(1);
		
		System.out.println("Handling result set, table name = " + tableName);
					
		// make a html table for current result set
		htmlResult += "<table>";
		
		// make headers
		ArrayList<String> columnNames = getColumnNames(tableName);
		htmlResult += "<tr>";

		for (String columnName : columnNames) {
			
			htmlResult += "<th>";
			htmlResult += columnName;
			htmlResult += "</th>";
		}
		
		htmlResult += "</tr>";
		
		// add rows for results
		while (resultSet.next()) {
			
			// create the html row
			String htmlRow = "<tr>";

			for (String columnName : columnNames) {
				
				// add data for each column. if data is null, use an empty string.
				Object data = resultSet.getObject(columnName);
				String dataRepresentation;
				
				if (data == null) {
					dataRepresentation = "";
				} else {
					dataRepresentation = data.toString();
				}
				
				htmlRow += "<td>" + dataRepresentation + "</td>";
			}
			
			htmlRow += "</tr>";
			
			// add row if it isn't already in the result (in a previous table)
			// note: i planned on using RowId for this, but the JDBC driver does not support getRowId
			if (!htmlResult.contains(htmlRow)) {
				htmlResult += htmlRow;
			}
		}
		
		htmlResult += "</table>";
		
		return htmlResult;
	}
	
	static String getSearchResults(String searchTerm) throws SQLException {
		
		String htmlResult = "";
		
		// primary search
		System.out.println("getSearchResults: before primary search");
		ArrayList<ResultSet> resultSets = searchAllTables(searchTerm);
		System.out.println("getSearchResults: after primary search");
		
		// secondary search
		String[] words = searchTerm.split(" ");
		System.out.println(words.toString());
		
		for (String word : words) {
			// do not include short words in secondary search
			// they will usually produce irrelevant results
			if (word.length() > 3) {
				resultSets.addAll(searchAllTables(word));	
			}
		}

		// handle all result sets
		for (ResultSet resultSet : resultSets) {
			
			htmlResult = addHtmlTableFromResultSet(resultSet, htmlResult);
		}
		
		return htmlResult;
	}
}
