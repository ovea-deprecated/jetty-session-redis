<%@ page import="java.util.Enumeration" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>web-1</title>
</head>
<body>

<p>Context path: <%=request.getContextPath()%></p>

<%
    if(request.getParameter("key") != null)
        request.getSession().setAttribute(String.valueOf(request.getParameter("key")), request.getParameter("val"));

    if(request.getParameter("invalidate") != null)
        request.getSession().invalidate();
%>

<ul>
<%
    if(request.getParameter("session") != null) {
        Enumeration<String> e = request.getSession().getAttributeNames();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String val = String.valueOf(request.getSession().getAttribute(key));
%>
<li><%=key%> = <%=val%></li>
<%      }
    }
%>
</ul>

</body>
</html>
