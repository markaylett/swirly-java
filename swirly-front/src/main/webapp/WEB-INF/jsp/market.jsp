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

    <div class="container" style="padding-top: 104px;">

      <jsp:include page="alert.jsp"/>

      <button type="button" class="btn btn-default" style="margin-bottom: 24px;"
              data-toggle="modal" data-target="#marketDialog" data-bind="click: clearMarket">
        New Market
      </button>

      <table class="table table-hover table-striped" style="margin-bottom: 24px;">
        <thead>
          <tr>
            <th>
              <input type="checkbox" data-bind="checked: allMarkets"/>
            </th>
            <th>Contr</th>
            <th>Settl Date</th>
            <th>Expiry Date</th>
            <th style="text-align: right;">Bid Count</th>
            <th style="text-align: right;">Bid Lots</th>
            <th style="text-align: right;">Bid Price</th>
            <th style="text-align: right;">Last Price</th>
            <th style="text-align: right;">Offer Price</th>
            <th style="text-align: right;">Offer Lots</th>
            <th style="text-align: right;">Offer Count</th>
          </tr>
        </thead>
        <tbody data-bind="foreach: markets">
          <tr>
            <td>
              <input type="checkbox" data-bind="checked: isSelected"/>
            </td>
            <td data-bind="mnem: contr"></td>
            <td data-bind="text: settlDate"></td>
            <td data-bind="text: expiryDate"></td>
            <td style="text-align: right;" data-bind="depth: bidCount"></td>
            <td style="text-align: right;" data-bind="depth: bidLots"></td>
            <td style="text-align: right;" data-bind="depth: bidPrice"></td>
            <td style="text-align: right;" data-bind="optional: lastPrice"></td>
            <td style="text-align: right;" data-bind="depth: offerPrice"></td>
            <td style="text-align: right;" data-bind="depth: offerLots"></td>
            <td style="text-align: right;" data-bind="depth: offerCount"></td>
          </tr>
        </tbody>
      </table>

    </div>

    <div id="marketDialog" class="modal fade" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
            <h4 class="modal-title">New Market</h4>
          </div>
          <div class="modal-body">
            <form role="form">
              <div class="form-group">
                <label for="contr">Contract:</label>
                <input id="contr" type="text" class="form-control"
                       data-bind="value: contrMnem"/>
              </div>
              <div class="form-group">
                <label for="settlDate">Settl Date:</label>
                <input id="settlDate" type="date" class="form-control"
                       data-bind="value: settlDate"/>
              </div>
              <div class="form-group">
                <label for="expiryDate">Expiry Date:</label>
                <input id="expiryDate" type="date" class="form-control"
                       data-bind="value: expiryDate"/>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-primary" data-dismiss="modal"
                    data-bind="click: submitMarket">Save</button>
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
    <script type="text/javascript" src="/js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="/js/knockout.min.js"></script>

    <script type="text/javascript" src="/js/swirly.js"></script>
    <script type="text/javascript" src="/js/market.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
