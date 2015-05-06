<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
--%>
<%@ page contentType="text/html;charset=utf-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">

  <jsp:include page="head.jsp"/>

  <body>

    <jsp:include page="navbar.jsp"/>

    <div class="container" style="margin-top: 32px; margin-bottom: 80px">

      <c:if test="${state.errorPage}">
        <div class="alert alert-warning alert-dismissible" role="alert">
          <button type="button" class="close" data-dismiss="alert">
            &times;
          </button>
          <span class="glyphicon glyphicon-warning-sign"></span>
          the email or password that you entered is incorrect
        </div>
      </c:if>

      <form class="signinForm"
            action='<%= response.encodeURL("j_security_check") %>'
            method="POST">
        <h3>Sign in to your account</h3>
        <div class="form-group">
          <label for="email">Email:</label>
          <input id="email" name="j_username" type="text" class="form-control"/>
        </div>
        <div class="form-group">
          <label for="pass">Password:</label>
          <input id="pass" name="j_password" type="password" class="form-control"/>
        </div>
        <button type="submit" class="btn btn-lg btn-primary btn-block">Sign in</button>
      </form>

    </div>

    <jsp:include page="footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
  </body>
</html>
