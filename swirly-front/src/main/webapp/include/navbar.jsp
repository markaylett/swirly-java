<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>

   All rights reserved.
--%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
   final UserService userService = UserServiceFactory.getUserService();

   String homeTag = "<li>";
   String traderTag = "<li>";
   String contrTag = "<li>";
   String userTag = "<li>";
   String aboutTag = "<li>";
   String contactTag = "<li>";

   final String active = request.getParameter("active");
   if ("home".equals(active)) {
     homeTag = "<li class=\"active\">";
   } else if ("trader".equals(active)) {
     traderTag = "<li class=\"active\">";
   } else if ("contr".equals(active)) {
     contrTag = "<li class=\"active\">";
   } else if ("user".equals(active)) {
     userTag = "<li class=\"active\">";
   } else if ("about".equals(active)) {
     aboutTag = "<li class=\"active\">";
   } else if ("contact".equals(active)) {
     contactTag = "<li class=\"active\">";
   }
%>
<nav class="navbar navbar-default navbar-fixed-top">
  <div class="container">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand text-muted logo">Swirly</a>
    </div>
    <div id="navbar" class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        <%=homeTag%><a href="home.jsp">Home</a></li>
<%
   if (userService.isUserLoggedIn()) {
%>
        <%=traderTag%><a href="trader.jsp">Trader</a></li>
<%
     if (userService.isUserAdmin()) {
%>
        <%=contrTag%><a href="contr.jsp">Contract</a></li>
        <%=userTag%><a href="user.jsp">User</a></li>
<%
     }
   }
%>
        <%=aboutTag%><a href="about.jsp">About</a></li>
        <%=contactTag%><a href="contact.jsp">Contact</a></li>
      </ul>
      <ul class="nav navbar-nav navbar-right">
<%
   if (userService.isUserLoggedIn()) {
     final User user = userService.getCurrentUser();
     pageContext.setAttribute("user", user);
%>
        <li><a href="#">Hello, ${fn:escapeXml(user.nickname)}</a></li>
        <li>
          <a href="<%=userService.createLogoutURL("/home.jsp")%>">Sign Out</a>
        </li>
<% } else {%>
        <li><a href="#">Welcome</a></li>
        <li>
          <a href="<%=userService.createLoginURL(request.getRequestURI())%>">Sign In</a>
        </li>
<% }%>
      </ul>
    </div>
  </div>
</nav>
