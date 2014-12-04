<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>

   All rights reserved.
--%>
<%@ page contentType="text/html;charset=utf-8" language="java"%>
<!DOCTYPE html>
<html lang="en">

  <jsp:include page="head.jsp"/>

  <body>

    <jsp:include page="navbar.jsp"/>

    <div class="container" style="padding-top: 88px;">

      <jsp:include page="alert.jsp"/>

      <form class="form-signup">
        <h2>Trader sign-up</h2>
        <div class="form-group">
          <label for="mnem">Username:</label>
          <input id="mnem" type="text" class="form-control" data-bind="value: mnem"/>
        </div>
        <div class="form-group">
          <label for="display">Full name:</label>
          <input id="display" type="text" class="form-control" data-bind="value: display"/>
        </div>
        <button type="button" class="btn btn-lg btn-primary btn-block" data-bind="click: signup">Sign up</button>
      </form>

    </div>

    <jsp:include page="footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="/js/knockout.min.js"></script>

    <script type="text/javascript" src="/js/swirly.js"></script>
    <script type="text/javascript" src="/js/signup.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
