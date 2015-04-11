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

      <form class="form-inline" style="margin-bottom: 24px;">
        <div class="form-group">
          <input id="market" type="text" class="form-control" placeholder="Enter market"
                 data-bind="value: market, disable: isWorkingSelected"/>
        </div>
        <div class="form-group">
          <input id="price" type="number" class="form-control" placeholder="Enter price"
                 data-bind="value: price, disable: isWorkingSelected"/>
        </div>
        <div class="form-group">
            <input id="lots" type="number" class="form-control" placeholder="Enter lots"
                 data-bind="value: lots, disable: isWorkingSelected"/>
        </div>
        <button type="button" class="btn btn-default"
                data-bind="click: submitBuy, disable: isWorkingSelected">
          <span class="glyphicon glyphicon-plus"></span>
          Buy
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: submitSell, disable: isWorkingSelected">
          <span class="glyphicon glyphicon-minus"></span>
          Sell
        </button>
      </form>

      <table class="table table-hover table-striped" style="margin-bottom: 24px;">
        <thead>
          <tr>
            <th>
              <input type="checkbox" data-bind="checked: allViews"/>
            </th>
            <th>Market</th>
            <th style="text-align: right;">Bid Count</th>
            <th style="text-align: right;">Bid Lots</th>
            <th style="text-align: right;">Bid Price</th>
            <th style="text-align: right;">Last Price</th>
            <th style="text-align: right;">Offer Price</th>
            <th style="text-align: right;">Offer Lots</th>
            <th style="text-align: right;">Offer Count</th>
          </tr>
        </thead>
        <tbody data-bind="foreach: views">
          <tr style="cursor: pointer; cursor: hand;">
            <td style="cursor: initial;">
              <input type="checkbox" data-bind="checked: isSelected"/>
            </td>
            <td data-bind="text: market, click: $root.selectView"></td>
            <td style="text-align: right;"
                data-bind="depth: bidCount, click: $root.selectBid"></td>
            <td style="text-align: right;"
                data-bind="depth: bidLots, click: $root.selectBid"></td>
            <td style="text-align: right;"
                data-bind="depth: bidPrice, click: $root.selectBid"></td>
            <td style="text-align: right;"
                data-bind="optional: lastPrice, click: $root.selectLast"></td>
            <td style="text-align: right;"
                data-bind="depth: offerPrice, click: $root.selectOffer"></td>
            <td style="text-align: right;"
                data-bind="depth: offerLots, click: $root.selectOffer"></td>
            <td style="text-align: right;"
                data-bind="depth: offerCount, click: $root.selectOffer"></td>
          </tr>
        </tbody>
      </table>

      <form class="form-inline" style="margin-bottom: 24px;">
        <div class="btn-group">
          <button type="button" class="btn btn-default"
                  data-bind="click: cancelAll, enable: isWorkingSelected">
            <span class="glyphicon glyphicon-remove"></span>
            Cancel
          </button>
          <button type="button" class="btn btn-default"
                  data-bind="click: archiveAll, enable: isArchivableSelected">
            <span class="glyphicon glyphicon-ok"></span>
            Archive
          </button>
          <button type="button" class="btn btn-default"
                  data-bind="click: refreshAll">
            <span class="glyphicon glyphicon-refresh"></span>
            Refresh
          </button>
        </div>
        <div class="form-group">
            <input id="reviseLots" type="number" class="form-control" placeholder="Enter lots"
                 data-bind="value: lots, enable: isWorkingSelected"/>
        </div>
        <button type="button" class="btn btn-default"
                data-bind="click: reviseAll, enable: isWorkingSelected">
          <span class="glyphicon glyphicon-pencil"></span>
          Revise
        </button>
      </form>

      <ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
        <li>
          <a id="workingTab" href="#working" data-toggle="tab"
             data-bind="click: selectTab;">
            Working (<span data-bind="text: working().length"></span>)
          </a>
        </li>
        <li>
          <a id="doneTab" href="#done" data-toggle="tab"
             data-bind="click: selectTab;">
            Done (<span data-bind="text: done().length"></span>)
          </a>
        </li>
        <li>
          <a id="tradeTab" href="#trades" data-toggle="tab"
             data-bind="click: selectTab;">
            Trades (<span data-bind="text: trades().length"></span>)
          </a>
        </li>
        <li>
          <a id="posnTab" href="#posns" data-toggle="tab"
             data-bind="click: selectTab;">
            Posns (<span data-bind="text: posns().length"></span>)
          </a>
        </li>
      </ul>
      <div id="tab-content" class="tab-content">
        <div id="working" class="tab-pane active">
          <table class="table table-hover table-striped">
            <thead>
              <tr>
                <th>
                  <input type="checkbox" data-bind="checked: allWorking"/>
                </th>
                <th>Market</th>
                <th>Id</th>
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
            <tbody data-bind="foreach: working">
              <tr style="cursor: pointer; cursor: hand;"
                  data-bind="click: $root.selectOrder">
                <td>
                  <input type="checkbox"
                         data-bind="checked: isSelected, click: $root.selectOrder,
                                    clickBubble: false"/>
                </td>
                <td data-bind="text: market"></td>
                <td data-bind="text: id"></td>
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
        <div id="done" class="tab-pane active">
          <table class="table table-hover table-striped">
            <thead>
              <tr>
                <th>
                  <input type="checkbox" data-bind="checked: allDone"/>
                </th>
                <th>Market</th>
                <th>Id</th>
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
            <tbody data-bind="foreach: done">
              <tr style="cursor: pointer; cursor: hand;"
                  data-bind="click: $root.selectOrder">
                <td>
                  <input type="checkbox"
                         data-bind="checked: isSelected, click: $root.selectOrder,
                                    clickBubble: false"/>
                </td>
                <td data-bind="text: market"></td>
                <td data-bind="text: id"></td>
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
                <th>Market</th>
                <th>Id</th>
                <th>Order Id</th>
                <th>Action</th>
                <th style="text-align: right;">Price</th>
                <th style="text-align: right;">Lots</th>
                <th style="text-align: right;">Resd</th>
                <th style="text-align: right;">Exec</th>
                <th>Role</th>
                <th>Cpty</th>
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
                <td data-bind="text: market"></td>
                <td data-bind="text: id"></td>
                <td data-bind="text: orderId"></td>
                <td data-bind="text: action"></td>
                <td style="text-align: right;" data-bind="text: lastPrice"></td>
                <td style="text-align: right;" data-bind="text: lastLots"></td>
                <td style="text-align: right;" data-bind="text: resd"></td>
                <td style="text-align: right;" data-bind="text: exec"></td>
                <td data-bind="text: role"></td>
                <td data-bind="text: cpty"></td>
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
                <th style="text-align: right;">Sell Price</th>
                <th style="text-align: right;">Sell Lots</th>
                <th style="text-align: right;">Buy Lots</th>
                <th style="text-align: right;">Buy Price</th>
                <th style="text-align: right;">Net Price</th>
                <th style="text-align: right;">Net Lots</th>
              </tr>
            </thead>
            <tbody data-bind="foreach: posns">
              <tr>
                <td data-bind="mnem: contr"></td>
                <td data-bind="text: settlDate"></td>
                <td style="text-align: right;" data-bind="text: sellPrice"></td>
                <td style="text-align: right;" data-bind="text: sellLots"></td>
                <td style="text-align: right;" data-bind="text: buyLots"></td>
                <td style="text-align: right;" data-bind="text: buyPrice"></td>
                <td style="text-align: right;" data-bind="text: netPrice"></td>
                <td style="text-align: right;" data-bind="text: netLots"></td>
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

    <script type="text/javascript" src="/src/twirly.js"></script>
    <script type="text/javascript" src="/src/trade.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
