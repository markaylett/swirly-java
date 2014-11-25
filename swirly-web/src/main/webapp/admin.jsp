<!-- -*- html -*- -->
<!--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>

   All rights reserved.
-->
<%@ page contentType="text/html;charset=utf-8" language="java"%>
<!DOCTYPE html>
<html lang="en">

<%@ include file="include/head.jsp" %>

  <body role="document">

    <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Swirly</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li><a href="home.jsp">Home</a></li>
            <li><a href="about.jsp">About</a></li>
            <li><a href="contact.jsp">Contact</a></li>
            <li><a href="trader.jsp">Trader</a></li>
            <li class="active"><a href="admin.jsp">Admin</a></li>
          </ul>
<%@ include file="include/navright.jsp" %>
        </div>
      </div>
    </nav>

    <div class="container" role="main" style="padding: 90px 15px 0;">

      <table class="table table-hover table-striped">
        <thead>
          <tr>
            <th>Mnem</th>
            <th>Display</th>
            <th>Asset Type</th>
            <th>Asset</th>
            <th>Ccy</th>
            <th style="text-align: right;">Tick Numer</th>
            <th style="text-align: right;">Tick Denom</th>
            <th style="text-align: right;">Lot Numer</th>
            <th style="text-align: right;">Lot Denom</th>
            <th style="text-align: right;">Price Dp</th>
            <th style="text-align: right;">Pip Dp</th>
            <th style="text-align: right;">Qty Dp</th>
            <th style="text-align: right;">Min Lots</th>
            <th style="text-align: right;">Max Lots</th>
          </tr>
        </thead>
        <tbody data-bind="foreach: contrs">
          <tr>
            <td data-bind="text: mnem"></td>
            <td data-bind="text: display"></td>
            <td data-bind="text: assetType"></td>
            <td data-bind="text: asset"></td>
            <td data-bind="text: ccy"></td>
            <td style="text-align: right;" data-bind="text: tickNumer"></td>
            <td style="text-align: right;" data-bind="text: tickDenom"></td>
            <td style="text-align: right;" data-bind="text: lotNumer"></td>
            <td style="text-align: right;" data-bind="text: lotDenom"></td>
            <td style="text-align: right;" data-bind="text: priceDp"></td>
            <td style="text-align: right;" data-bind="text: pipDp"></td>
            <td style="text-align: right;" data-bind="text: qtyDp"></td>
            <td style="text-align: right;" data-bind="text: minLots"></td>
            <td style="text-align: right;" data-bind="text: maxLots"></td>
          </tr>
        </tbody>
      </table>

    </div>

<%@ include file="include/footer.jsp" %>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/bootstrap.min.js"></script>
    <script type="text/javascript" src="js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="js/knockout.min.js"></script>

    <script type="text/javascript" src="js/swirly.js"></script>
    <script type="text/javascript" src="js/admin.js"></script>
    <script type="text/javascript">
      $(documentReady);
    </script>
  </body>
</html>
