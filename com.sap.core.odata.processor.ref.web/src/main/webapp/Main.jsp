<%@page import="com.sap.core.odata.processor.jpa.api.ODataJPAContext"%>
<%@page import="java.util.List"%>
<%@page import="javax.persistence.EntityManager"%>
<%@page import="javax.persistence.EntityManagerFactory"%>
<%@page import="javax.persistence.Persistence"%>
<%@page import="javax.persistence.Query"%>
<%@page	import="com.sap.core.odata.processor.ref.JPAReferenceServiceFactory"%>
<%@page import="com.sap.core.odata.processor.ref.util.DataGenerator"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to JPA implementation</title>
</head>
<body>
    <h1>SAP OData library</h1>
    <hr />
	<%
		String version = "gen/version.html";
	%>
	<%
		try {
	%>
	<jsp:include page='<%=version%>' />
	<%
		} catch (Exception e) {
	%>
	<p>IDE Build</p>
	<%
		}
	%>
	<hr />
	<h2>Reference Scenario</h2>
	<ul>
		<li><a href="" target="_blank">index page</a></li>
		<li><a href="" target="_blank">wadl</a></li><!-- ReferenceScenario.svc?_wadl -->
		<li><a href="" target="_blank">service document</a></li><!-- ReferenceScenario.svc/ -->
		<li><a href="" target="_blank">metadata</a></li><!-- ReferenceScenario.svc/$metadata -->
	</ul>
	<hr />
        <form name="form1" method="get">
            <input type="hidden" name="button" value="Generate">
            <input type="submit" value="Generate Data" width="100%">
        </form>
        <br>
        <form name="form2" method="get">
            <input type="hidden" name="button" value="Clean">
            <input type="submit" value="   Clean Data  " width="100%">
        </form>
        
        <% 
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("salesorderprocessing");
		EntityManager entityManager = entityManagerFactory.createEntityManager(); 
		DataGenerator dataGenerator = new DataGenerator(entityManager);

		Number result1 = null;
		Number existingCount = null;
		
		String msg = null;
       	if(request.getParameter("button") != null) {
       		if(request.getParameter("button").equalsIgnoreCase("Generate")){
        		Query q = entityManager.createQuery("SELECT COUNT(x) FROM SalesOrderHeader x");
				existingCount = (Number) q.getSingleResult();
				if (existingCount.intValue() == 0) { // Generate only if no data!
					dataGenerator.generate();
					result1 = (Number) q.getSingleResult();
					System.out.println("Data not existing earlier.... Generated number of Items - " + result1);
					msg = "Data not existing earlier.... Generated number of Items - " + result1;
				} else {
					System.err.println("Data already existing.... No Item generated by Data Generator !!");
					msg = "Data already existing.... No Item generated by Data Generator !!";
				}
       		} else{ //Clean
       			
       			// Check if data already exists
       			Query q = entityManager.createQuery("SELECT COUNT(x) FROM SalesOrderHeader x");
       			Number result = (Number) q.getSingleResult();
        		if (result.intValue() > 0) { // Generate only if no data!
        			dataGenerator.clean();
        		msg = "Data Cleaned. Number of items deleted - "+result;
        		}else{
        			msg = "No data existing.... Nothing to clean!!";
        		}
       		}
        %>
			<h3><%=(msg) %></h2>
        <%
            }
        %>
        
    </body>
</html>