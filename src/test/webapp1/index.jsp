<%--

    Copyright (C) 2011 Ovea <dev@ovea.com>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
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
