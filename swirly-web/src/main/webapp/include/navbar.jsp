<!-- -*- html -*- -->
<!--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>

   All rights reserved.
-->
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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
<%
   final String active = request.getParameter("active");
   if ("home".equals(active)) { %>
        <li class="active"><a href="home.jsp">Home</a></li>
        <li><a href="about.jsp">About</a></li>
        <li><a href="contact.jsp">Contact</a></li>
        <li><a href="trader.jsp">Trader</a></li>
        <li><a href="contr.jsp">Contract</a></li>
        <li><a href="user.jsp">User</a></li>
<% } else if ("about".equals(active)) { %>
        <li><a href="home.jsp">Home</a></li>
        <li class="active"><a href="about.jsp">About</a></li>
        <li><a href="contact.jsp">Contact</a></li>
        <li><a href="trader.jsp">Trader</a></li>
        <li><a href="contr.jsp">Contract</a></li>
        <li><a href="user.jsp">User</a></li>
<% } else if ("contact".equals(active)) { %>
        <li><a href="home.jsp">Home</a></li>
        <li><a href="about.jsp">About</a></li>
        <li class="active"><a href="contact.jsp">Contact</a></li>
        <li><a href="trader.jsp">Trader</a></li>
        <li><a href="contr.jsp">Contract</a></li>
        <li><a href="user.jsp">User</a></li>
<% } else if ("trader".equals(active)) { %>
        <li><a href="home.jsp">Home</a></li>
        <li><a href="about.jsp">About</a></li>
        <li><a href="contact.jsp">Contact</a></li>
        <li class="active"><a href="trader.jsp">Trader</a></li>
        <li><a href="contr.jsp">Contract</a></li>
        <li><a href="user.jsp">User</a></li>
<% } else if ("contr".equals(active)) { %>
        <li><a href="home.jsp">Home</a></li>
        <li><a href="about.jsp">About</a></li>
        <li><a href="contact.jsp">Contact</a></li>
        <li><a href="trader.jsp">Trader</a></li>
        <li class="active"><a href="contr.jsp">Contract</a></li>
        <li><a href="user.jsp">User</a></li>
<% } else if ("user".equals(active)) { %>
        <li><a href="home.jsp">Home</a></li>
        <li><a href="about.jsp">About</a></li>
        <li><a href="contact.jsp">Contact</a></li>
        <li><a href="trader.jsp">Trader</a></li>
        <li><a href="contr.jsp">Contract</a></li>
        <li class="active"><a href="user.jsp">User</a></li>
<% } %>
      </ul>
      <ul class="nav navbar-nav navbar-right">
<%
   final UserService userService = UserServiceFactory.getUserService();
   final User user = userService.getCurrentUser();
   if (user != null) {
     pageContext.setAttribute("user", user);
%>
        <li><a href="#">Hello, ${fn:escapeXml(user.nickname)}</a></li>
        <li>
          <a href="<%=userService.createLogoutURL(request.getRequestURI())%>">Sign Out</a>
        </li>
<% } else {%>
        <li><a href="#">Welcome</a></li>
        <li>
          <a href="<%=userService.createLoginURL(request.getRequestURI())%>">Sign In</a>
        </li>
<% } %>
      </ul>
    </div>
  </div>
</nav>
