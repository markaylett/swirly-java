<!--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 
   All rights reserved.
-->
<%@ page contentType="text/html;charset=utf-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="Mark Aylett">
    <link rel="icon" href="/favicon.ico">

    <title>Doobry</title>

    <!-- Bootstrap -->
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/bootstrap-theme.min.css" rel="stylesheet">
    <!-- Custom styles -->
    <link href="/css/theme.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
        <![endif]-->
  </head>

  <body role="document">

    <!-- Fixed navbar -->
    <nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                  data-target="#navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Doobry</a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="#">Home</a></li>
            <li><a href="#about">About</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
<%
  final UserService userService = UserServiceFactory.getUserService();
  final User user = userService.getCurrentUser();
  if (user != null) {
    pageContext.setAttribute("user", user);
%>
            <li><a href="#">Hello, ${fn:escapeXml(user.nickname)}</a></li>
            <li>
              <a href="<%=userService.createLogoutURL(request.getRequestURI())%>">Sign Out</a>
            </li>
<%
  } else {
%>
            <li><a href="#">Welcome</a></li>
            <li>
              <a href="<%=userService.createLoginURL(request.getRequestURI())%>">Sign In</a>
            </li>
<%
  }
%>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container theme-showcase" role="main">
      <form class="form-inline" role="form" style="margin-bottom: 20px;">
        <div class="form-group">
          <label for="contr" class="sr-only">Contr</label>
          <div id="the-basics">
            <input id="contr" type="text" class="form-control" placeholder="Enter contract"
                   data-bind="value: contrMnem, disable: isOrderSelected"/>
          </div>
        </div>
        <div class="form-group">
          <label for="settlDate" class="sr-only">Settl Date</label>
          <input id="settlDate" type="date" class="form-control" placeholder="Enter settl date"
                 data-bind="value: settlDate, disable: isOrderSelected"/>
        </div>
        <div class="form-group">
          <label for="price" class="sr-only">Price</label>
          <input id="price" type="number" class="form-control" placeholder="Enter price"
                 data-bind="value: price, disable: isOrderSelected" min="0"/>
        </div>
        <div class="form-group">
          <label for="lots" class="sr-only">Lots</label>
            <input id="lots" type="number" class="form-control" placeholder="Enter lots"
                 data-bind="value: lots"/>
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

      <table class="table table-hover table-striped">
        <thead>
          <tr>
            <th>Contr</th>
            <th>Settl Date</th>
            <th>Bid Price</th>
            <th>Bid Lots</th>
            <th>Bid Count</th>
            <th>Offer Price</th>
            <th>Offer Lots</th>
            <th>Offer Count</th>
          </tr>
        </thead>
        <tbody data-bind="foreach: books">
          <tr>
            <td data-bind="mnem: contr"></td>
            <td data-bind="text: settlDate"></td>
            <td style="cursor: pointer; cursor: hand;"
                data-bind="optnum: bidPrice, click: $root.selectBid"></td>
            <td style="cursor: pointer; cursor: hand;"
                data-bind="optnum: bidLots, click: $root.selectBid"></td>
            <td style="cursor: pointer; cursor: hand;"
                data-bind="optnum: bidCount, click: $root.selectBid"></td>
            <td style="cursor: pointer; cursor: hand;"
                data-bind="optnum: offerPrice, click: $root.selectOffer"></td>
            <td style="cursor: pointer; cursor: hand;"
                data-bind="optnum: offerLots, click: $root.selectOffer"></td>
            <td style="cursor: pointer; cursor: hand;"
                data-bind="optnum: offerCount, click: $root.selectOffer"></td>
          </tr>
        </tbody>
      </table>

      <div class="btn-group" role="group" style="margin-bottom: 20px;">
        <button type="button" class="btn btn-default"
                data-bind="click: refreshAll">
          <span class="glyphicon glyphicon-refresh"></span>
          Refresh
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: reviseAll, enable: isOrderSelected">
          <span class="glyphicon glyphicon-pencil"></span>
          Revise
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: cancelAll, enable: isOrderSelected">
          <span class="glyphicon glyphicon-remove"></span>
          Cancel
        </button>
        <button type="button" class="btn btn-default"
                data-bind="click: confirmAll, enable: isTradeSelected">
          <span class="glyphicon glyphicon-ok"></span>
          Confirm
        </button>
      </div>

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
                <th>Price</th>
                <th>Lots</th>
                <th>Resd</th>
                <th>Exec</th>
                <th>Last Price</th>
                <th>Last Lots</th>
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
                <td data-bind="text: price"></td>
                <td data-bind="text: lots"></td>
                <td data-bind="text: resd"></td>
                <td data-bind="text: exec"></td>
                <td data-bind="optnum: lastPrice"></td>
                <td data-bind="optnum: lastLots"></td>
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
                <th>Price</th>
                <th>Lots</th>
                <th>Resd</th>
                <th>Exec</th>
                <th>Last Price</th>
                <th>Last Lots</th>
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
                <td data-bind="text: price"></td>
                <td data-bind="text: lots"></td>
                <td data-bind="text: resd"></td>
                <td data-bind="text: exec"></td>
                <td data-bind="text: lastPrice"></td>
                <td data-bind="text: lastLots"></td>
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
                <th>Buy Price</th>
                <th>Buy Lots</th>
                <th>Sell Price</th>
                <th>Sell Lots</th>
                <th>Net Price</th>
                <th>Net Lots</th>
              </tr>
            </thead>
            <tbody data-bind="foreach: posns">
              <tr>
                <td data-bind="mnem: contr"></td>
                <td data-bind="text: settlDate"></td>
                <td style="cursor: pointer; cursor: hand;"
                    data-bind="optnum: buyPrice, click: $root.selectBuy"></td>
                <td style="cursor: pointer; cursor: hand;"
                    data-bind="optnum: buyLots, click: $root.selectBuy"></td>
                <td style="cursor: pointer; cursor: hand;"
                    data-bind="optnum: sellPrice, click: $root.selectSell"></td>
                <td style="cursor: pointer; cursor: hand;"
                    data-bind="optnum: sellLots, click: $root.selectSell"></td>
                <td style="cursor: pointer; cursor: hand;"
                    data-bind="text: netPrice, click: $root.selectNet"></td>
                <td style="cursor: pointer; cursor: hand;"
                    data-bind="text: netLots, click: $root.selectNet"></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div> <!-- /container -->

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="/js/knockout.js"></script>
    <script type="text/javascript" src="/js/doobry.js"></script>
    <script type="text/javascript">
      $(documentReady);
    </script>
  </body>
</html>