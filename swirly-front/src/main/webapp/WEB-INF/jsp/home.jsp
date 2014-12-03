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

    <div class="container">

      <div class="jumbotron" style="margin-top: 32px">

        <img src="/img/swirly-sm.png" class="img-responsive pull-left"
             style="margin: 16px 32px 64px 0;"/>
        <h2>Swirly Cloud</h2>
        <p>We aim to build an Internet-scale trading application in the cloud, where people can
        trade on exciting new markets.</p>
        <p><a href="/page/trade" class="btn btn-primary btn-lg" role="button">Try the demo now &raquo;</a></p>

      </div>

    </div>

    <jsp:include page="footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
  </body>
</html>
