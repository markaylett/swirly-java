<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
--%>
<%@ page contentType="text/html;charset=utf-8" language="java"%>
<!DOCTYPE html>
<html lang="en">

  <jsp:include page="head.jsp"/>

  <body>

    <jsp:include page="navbar.jsp"/>

    <div class="container" style="margin-top: 32px">

      <div class="jumbotron">

        <div class="container">
          <div class="row">
            <div class="col-sm-2">
              <br/>
              <img src="/img/swirly-md.png" class="img-responsive"/>
            </div>
            <div class="col-sm-10">
              <h2>Swirly Cloud</h2>
              <p>We aim to build an Internet-scale trading application in the Cloud, where people
              can trade on exciting new markets.</p>
              <p><a href="/page/trade" class="btn btn-primary btn-lg" role="button">Try the demo now &raquo;</a></p>
            </div>
          </div>
        </div>

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
