package databasePackage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SearchUtilities {
	
	static ArrayList<Integer> search(String search) throws SQLException {
		
		ArrayList<String> tableNames = SQLUtilities.getTableNames();
		System.out.println("Tables: " + tableNames);
		ArrayList<Integer> resultList = new ArrayList<>();
		ResultSet resultSet = null;
		
		if (tableNames != null && tableNames.size() > 0) {
			String query = "SELECT * FROM " + tableNames.get(0) + ";";			
			resultSet = SQLUtilities.query(query);
		}

		if (resultSet != null) {
			try {
//				System.out.println(resultSet.getMetaData());
//				int numberOfColumns = resultSet.getMetaData().getColumnCount();
//				for (int i = 1; i <= numberOfColumns; i++) {
//					System.out.println(resultSet.getMetaData().getColumnClassName(i).equals("java.lang.String"));
//				}
				
				while (resultSet.next()) {
					int rowNumber = resultSet.getRow();
					String row = "";
					for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i ++) {
						row += resultSet.getString(i) + " ";
					}
					System.out.println(row);
					if (row.toLowerCase().contains(search.toLowerCase())) {
						resultList.add(rowNumber);
					}
					if (rowNumber > 10) {
						break;
					}
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resultList;
	}
	

}
