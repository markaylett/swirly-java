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

      <form class="form-inline" style="margin-bottom: 24px;">
        <div class="form-group">
          <input id="contr" type="text" class="form-control" placeholder="Enter contract"
                 data-bind="value: contrMnem, disable: isOrderSelected"/>
        </div>
        <div class="form-group">
          <input id="settlDate" type="date" class="form-control" placeholder="Enter settl date"
                 data-bind="value: settlDate, disable: isOrderSelected"/>
        </div>
        <div class="form-group">
          <input id="price" type="number" class="form-control" placeholder="Enter price"
                 data-bind="value: price, disable: isOrderSelected" min="0"/>
        </div>
        <div class="form-group">
            <input id="lots" type="number" class="form-control" placeholder="Enter lots"
                 data-bind="value: lots, disable: isOrderSelected"/>
        </div>
        <button type="button" class="btn btn-default"
                data-bind="click: submitBuy, disable: isOrderSelected">
          <span class="glyphicon glyphicon-plus"></span>
          Buy
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: submitSell, disable: isOrderSelected">
          <span class="glyphicon glyphicon-minus"></span>
          Sell
        </button>
      </form>

      <table class="table table-hover table-striped" style="margin-bottom: 24px;">
        <thead>
          <tr>
            <th>
              <input type="checkbox" data-bind="checked: allMarkets"/>
            </th>
            <th>Contr</th>
            <th>Settl Date</th>
            <th style="text-align: right;">Bid Count</th>
            <th style="text-align: right;">Bid Lots</th>
            <th style="text-align: right;">Bid Price</th>
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
            <td style="cursor: pointer; cursor: hand; text-align: right;"
                data-bind="depth: bidCount, click: $root.selectBid"></td>
            <td style="cursor: pointer; cursor: hand; text-align: right;"
                data-bind="depth: bidLots, click: $root.selectBid"></td>
            <td style="cursor: pointer; cursor: hand; text-align: right;"
                data-bind="depth: bidPrice, click: $root.selectBid"></td>
            <td style="cursor: pointer; cursor: hand; text-align: right;"
                data-bind="depth: offerPrice, click: $root.selectOffer"></td>
            <td style="cursor: pointer; cursor: hand; text-align: right;"
                data-bind="depth: offerLots, click: $root.selectOffer"></td>
            <td style="cursor: pointer; cursor: hand; text-align: right;"
                data-bind="depth: offerCount, click: $root.selectOffer"></td>
          </tr>
        </tbody>
      </table>

      <div class="btn-group" style="float: left; margin-bottom: 24px; margin-right: 4px;">
        <button type="button" class="btn btn-default"
                data-bind="click: refreshAll">
          <span class="glyphicon glyphicon-refresh"></span>
          Refresh
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: cancelAll, enable: isOrderSelected">
          <span class="glyphicon glyphicon-remove"></span>
          Cancel
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: archiveAll, enable: isTradeSelected">
          <span class="glyphicon glyphicon-ok"></span>
          Archive
        </button>
      </div>

      <form class="form-inline" style="margin-bottom: 24px;">
        <div class="form-group">
            <input id="reviseLots" type="number" class="form-control" placeholder="Enter lots"
                 data-bind="value: lots, enable: isOrderSelected"/>
        </div>
        <button type="button" class="btn btn-default"
                data-bind="click: reviseAll, enable: isOrderSelected">
          <span class="glyphicon glyphicon-pencil"></span>
          Revise
        </button>
      </form>

      <ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
        <li>
          <a id="orderTab" href="#orders" data-toggle="tab"
             data-bind="click: selectTab;">Orders</a>
        </li>
        <li>
          <a id="tradeTab" href="#trades" data-toggle="tab"
             data-bind="click: selectTab;">Trades</a>
        </li>
        <li>
          <a id="posnTab" href="#posns" data-toggle="tab"
             data-bind="click: selectTab;">Posns</a>
        </li>
      </ul>
      <div id="tab-content" class="tab-content">
        <div id="orders" class="tab-pane active">
          <table class="table table-hover table-striped">
            <thead>
              <tr>
                <th>
                  <input type="checkbox" data-bind="checked: allOrders"/>
                </th>
                <th>Id</th>
                <th>Contr</th>
                <th>Settl Date</th>
                <th>State</th>
                <th>Action</th>
                <th style="text-align: right;">Price</th>
                <th style="text-align: right;">Lots</th>
                <th style="text-align: right;">Resd</th>
                <th style="text-align: right;">Exec</th>
                <th style="text-align: right;">Last Price</th>
                <th style="text-align: right;">Last Lots</th>
              </tr>
            </thead>
            <tbody data-bind="foreach: orders">
              <tr style="cursor: pointer; cursor: hand;"
                  data-bind="click: $root.selectOrder">
                <td>
                  <input type="checkbox"
                         data-bind="checked: isSelected, click: $root.selectOrder,
                                    clickBubble: false"/>
                </td>
                <td data-bind="text: id"></td>
                <td data-bind="mnem: contr"></td>
                <td data-bind="text: settlDate"></td>
                <td data-bind="text: state"></td>
                <td data-bind="text: action"></td>
                <td style="text-align: right;" data-bind="text: price"></td>
                <td style="text-align: right;" data-bind="text: lots"></td>
                <td style="text-align: right;" data-bind="text: resd"></td>
                <td style="text-align: right;" data-bind="text: exec"></td>
                <td style="text-align: right;" data-bind="optional: lastPrice"></td>
                <td style="text-align: right;" data-bind="optional: lastLots"></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div id="trades" class="tab-pane">
          <table class="table table-hover table-striped">
            <thead>
              <tr>
                <th>
                  <input type="checkbox" data-bind="checked: allTrades"/>
                </th>
                <th>Id</th>
                <th>Order Id</th>
                <th>Contr</th>
                <th>Settl Date</th>
                <th>Action</th>
                <th style="text-align: right;">Price</th>
                <th style="text-align: right;">Lots</th>
                <th style="text-align: right;">Resd</th>
                <th style="text-align: right;">Exec</th>
                <th style="text-align: right;">Orig Price</th>
                <th style="text-align: right;">Orig Lots</th>
              </tr>
            </thead>
            <tbody data-bind="foreach: trades">
              <tr style="cursor: pointer; cursor: hand;"
                  data-bind="click: $root.selectTrade">
                <td>
                  <input type="checkbox"
                         data-bind="checked: isSelected, click: $root.selectTrade,
                                    clickBubble: false"/>
                </td>
                <td data-bind="text: id"></td>
                <td data-bind="text: orderId"></td>
                <td data-bind="mnem: contr"></td>
                <td data-bind="text: settlDate"></td>
                <td data-bind="text: action"></td>
                <td style="text-align: right;" data-bind="text: lastPrice"></td>
                <td style="text-align: right;" data-bind="text: lastLots"></td>
                <td style="text-align: right;" data-bind="text: resd"></td>
                <td style="text-align: right;" data-bind="text: exec"></td>
                <td style="text-align: right;" data-bind="text: price"></td>
                <td style="text-align: right;" data-bind="text: lots"></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div id="posns" class="tab-pane">
          <table class="table table-hover table-striped">
            <thead>
              <tr>
                <th>Contr</th>
                <th>Settl Date</th>
                <th style="text-align: right;">Buy Lots</th>
                <th style="text-align: right;">Buy Price</th>
                <th style="text-align: right;">Sell Price</th>
                <th style="text-align: right;">Sell Lots</th>
                <th style="text-align: right;">Net Price</th>
                <th style="text-align: right;">Net Lots</th>
              </tr>
            </thead>
            <tbody data-bind="foreach: posns">
              <tr>
                <td data-bind="mnem: contr"></td>
                <td data-bind="text: settlDate"></td>
                <td style="cursor: pointer; cursor: hand; text-align: right;"
                    data-bind="text: buyLots, click: $root.selectBuy"></td>
                <td style="cursor: pointer; cursor: hand; text-align: right;"
                    data-bind="text: buyPrice, click: $root.selectBuy"></td>
                <td style="cursor: pointer; cursor: hand; text-align: right;"
                    data-bind="text: sellPrice, click: $root.selectSell"></td>
                <td style="cursor: pointer; cursor: hand; text-align: right;"
                    data-bind="text: sellLots, click: $root.selectSell"></td>
                <td style="cursor: pointer; cursor: hand; text-align: right;"
                    data-bind="text: netPrice, click: $root.selectNet"></td>
                <td style="cursor: pointer; cursor: hand; text-align: right;"
                    data-bind="text: netLots, click: $root.selectNet"></td>
              </tr>
            </tbody>
          </table>
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
    <script type="text/javascript" src="/js/trade.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
