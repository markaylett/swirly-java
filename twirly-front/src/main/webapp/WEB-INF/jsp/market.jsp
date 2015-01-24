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

    <div class="container" style="padding-top: 104px;">

      <jsp:include page="alert.jsp"/>

      <button type="button" class="btn btn-default" style="margin-bottom: 24px;"
              data-toggle="modal" data-target="#marketDialog" data-bind="click: clearMarket">
        New Market
      </button>

      <table class="table table-hover table-striped">
        <thead>
          <tr>
            <th>Mnem</th>
            <th>Display</th>
            <th>Contr</th>
            <th>Settl Date</th>
            <th>Expiry Date</th>
          </tr>
        </thead>
        <tbody data-bind="foreach: markets">
          <tr>
            <td data-bind="text: mnem"></td>
            <td data-bind="text: display"></td>
            <td data-bind="mnem: contr"></td>
            <td data-bind="text: settlDate"></td>
            <td data-bind="text: expiryDate"></td>
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
                <label for="mnem">Mnem:</label>
                <input id="mnem" type="text" class="form-control" data-bind="value: mnem"/>
              </div>
              <div class="form-group">
                <label for="display">Display:</label>
                <input id="display" type="text" class="form-control" data-bind="value: display"/>
              </div>
              <div class="form-group">
                <label for="contr">Contract:</label>
                <input id="contr" type="text" class="form-control"
                       data-bind="value: contr"/>
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

    <script type="text/javascript" src="/app/twirly.js"></script>
    <script type="text/javascript" src="/app/market.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
