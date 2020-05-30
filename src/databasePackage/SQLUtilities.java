package databasePackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class SQLUtilities {
	static Connection connection;
	
	static boolean hasConnection() {
		// connection already established
		if (connection != null) {
			return true;
		}
		
		// try to establish connection
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/dumble?serverTimezone=UTC", "root", "");
			System.out.println("Database connection established");
			return true;
		} catch (SQLException e) {
			System.out.println("Database connection error");
//			e.printStackTrace();
		    System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
			return false;
		}
	}
	
	static ResultSet query(String sqlStatement) throws SQLException {
		Statement statement;
		ResultSet resultSet = null;
		
		if (!hasConnection()) {
			return null;
		}
		
//		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlStatement);
//		} catch (SQLException e) {
////			e.printStackTrace();
//		    System.out.println("SQLException: " + e.getMessage());
//		    System.out.println("SQLState: " + e.getSQLState());
//		    System.out.println("VendorError: " + e.getErrorCode());
//		}

		return resultSet;
	}
	
	static ArrayList<String> getTableNames() throws SQLException {
		ArrayList<String> tableNames = new ArrayList<>();
		ResultSet resultSet = query("SHOW TABLES");

		while (resultSet.next()) {
			tableNames.add(resultSet.getString(1));
		}

		return tableNames;
	}
	
	static ArrayList<String> getColumnNames(String tableName) throws SQLException {
		ArrayList<String> columnNames = new ArrayList<>();
		String q = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = N'" + tableName + "'";
		ResultSet resultSet = query(q);
		
		while (resultSet.next()) {
			columnNames.add(resultSet.getString(1));
		}

		return columnNames;
	}
	
	static ArrayList<ResultSet> searchAllTables(String searchTerm) throws SQLException {
		
		ArrayList<ResultSet> primarySearchResults = new ArrayList<>();
		
		System.out.println("Search all tables");
		
		if (searchTerm.contentEquals("")) {
			System.out.println("Search term can't be empty");
			return primarySearchResults;
		}
		
		// get the names of all tables
		ArrayList<String> tableNames = getTableNames();
		
		// search all tables
		for (String tableName : tableNames) {
			
			System.out.println("Searching table " + tableName);
			
			// get the column names for current table
			ArrayList<String> columnNames = getColumnNames(tableName);
			
			System.out.println("Columns: " + columnNames);
			
			// put together primary search
			String primarySearch = "SELECT * FROM " + tableName + " WHERE ";
			
			if (columnNames.size() == 0) {
				break;
			}
			
			primarySearch += tableName + "." + columnNames.get(0) + " LIKE '%" + searchTerm + "%'";
			
			for (int i = 1; i < columnNames.size(); i++) {
				primarySearch += " OR " + tableName + "." + columnNames.get(i) + " LIKE '%" + searchTerm + "%'";
			}
			
			System.out.println(primarySearch);
			
			// execute primary search
			primarySearchResults.add(query(primarySearch));
		}
		
		System.out.println("Results ArrayList.size(): " + primarySearchResults.size());
		
		return primarySearchResults;
	}
	
	static String getSearchResults(String searchTerm) throws SQLException {
		
		String htmlResult = "";
		TreeMap<String, ArrayList<RowId>> rowIdsMap = new TreeMap<>();
		
		// primary search
		ArrayList<ResultSet> resultSets = searchAllTables(searchTerm);
		
		// handle all result sets
		for (ResultSet resultSet : resultSets) {
			
			String tableName = resultSet.getMetaData().getTableName(1);
			ArrayList<RowId> rowIds = new ArrayList<>();
			
			// get a list of row ids for the sql table this result set is from
			// make a mapping for sql row ids if one doesn't already exist
			if (rowIdsMap.containsKey(tableName)) {
				rowIds = rowIdsMap.get(tableName);
			} else {
				rowIdsMap.put(tableName, rowIds);
			}
			
			// make a html table for current result set
			htmlResult += "<table>";
			
			ArrayList<String> columnNames = getColumnNames(resultSet.getMetaData().getTableName(1));
			
			// make headers
			htmlResult += "<tr>";

			for (String columnName : columnNames) {
				
				htmlResult += "<th>";
				htmlResult += columnName;
				htmlResult += "</th>";
			}
			
			htmlResult += "</tr>";
			
			// add rows for results
			while (resultSet.next()) {
				
				// check if this result has already been added, and skip it if so
				RowId rowId = resultSet.getRowId(1);
				if (rowIds.contains(rowId)) {
					break;
				} else {
					rowIds.add(rowId);
				}
				
				// create the html row
				htmlResult += "<tr>";

				for (String columnName : columnNames) {
					
					// add data for each column. if data is null, use an empty string.
					Object data = resultSet.getObject(columnName);
					String dataRepresentation;
					
					if (data == null) {
						dataRepresentation = "";
					} else {
						dataRepresentation = data.toString();
					}
					
					htmlResult += "<td>" + dataRepresentation + "</td>";
				}
				
				htmlResult += "</tr>";
				
			}
			
			htmlResult += "</table>";
		}
		
		return htmlResult;
	}
}
