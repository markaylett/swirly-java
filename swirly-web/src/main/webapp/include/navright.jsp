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
<%
  } else {
%>
  <li><a href="#">Welcome</a></li>
  <li>
    <a href="<%=userService.createLoginURL(request.getRequestURI())%>">Sign In</a>
  </li>
<%
  }
%>
</ul>
