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

<jsp:include page="navbar.jsp">
  <jsp:param name="active" value="contr"/>
</jsp:include>

    <div class="container" style="padding: 90px 15px 0;">

<jsp:include page="alert.jsp"/>

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

<jsp:include page="footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="/js/knockout.min.js"></script>

    <script type="text/javascript" src="/js/swirly.js"></script>
    <script type="text/javascript" src="/js/contr.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
