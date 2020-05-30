<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Database Interface</title>
<link rel="stylesheet" href="style.css">
</head>
<body>
	<form action="DatabaseInterfaceServlet" method="post">
		Search: <input type="text" name="search"/><br>
		<input type="submit" value="Search">
	</form>
	
	<%
		String searchResult = (String) request.getAttribute("searchResult");
	
		if (searchResult == null) {
			searchResult = "";
		}
		
		out.print(searchResult);
	%>
</body>
</html>