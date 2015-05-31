<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
--%>
<%@ page contentType="text/html;charset=utf-8" language="java"%>
<!DOCTYPE html>
<html lang="en">

  <jsp:include page="head.jsp"/>

  <body>

    <jsp:include page="navbar.jsp"/>
    <div id="module" class="container">
    </div>
    <jsp:include page="footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="/js/react.min.js"></script>

    <script type="text/javascript" src="/js/twirly.js"></script>
    <script type="text/javascript">
      React.render(
          React.createElement(MarketModule, {pollInterval: 5000}),
          document.getElementById('module')
      );
    </script>
  </body>
</html>
