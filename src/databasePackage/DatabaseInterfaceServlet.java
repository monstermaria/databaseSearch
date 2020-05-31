package databasePackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
//import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DatabaseInterfaceServlet
 */
@WebServlet("/DatabaseInterfaceServlet")
public class DatabaseInterfaceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DatabaseInterfaceServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Servlet get method");
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		String search = request.getParameter("search");
		String searchResult = "";
				
		System.out.println("Servlet post method");
		System.out.println("Search: " + search);

		try {
			searchResult = SQLUtilities.getSearchResults(search);
			System.out.println("searchResult returned " + searchResult);
		} catch (Exception e) {
			searchResult = "<p>" + e.toString() + "</p>";
		    System.out.println("doPost: " + e);
		}		
		
		request.setAttribute("searchResult", searchResult);
		
		RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
		rd.forward(request, response);
	}
	
	protected void doPostFirstVersion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		
		String search = request.getParameter("search");
		String searchResult = "";
		
		System.out.println("Servlet post method");
		System.out.println("Search: " + search);
		
		try {
			searchResult = SQLUtilities.getSearchResults(search);
		} catch (SQLException e) {
			searchResult = e.getMessage();
		    System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		
//		ArrayList<Integer> searchResultRowIndexes;
//		try {
//			searchResultRowIndexes = SearchUtilities.search(search);
//			for (int i = 0; i < searchResultRowIndexes.size(); i++) {
//				out.append(String.valueOf(searchResultRowIndexes.get(i))).append("<br>");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		out.append(searchResult);
		out.append("End of result");
	}

}
