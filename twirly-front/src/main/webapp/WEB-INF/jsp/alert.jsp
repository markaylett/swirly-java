<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
--%>
<div class="alert alert-warning alert-dismissible" role="alert" style="display: none;"
     data-bind="visible: hasErrors">
  <button type="button" class="close" data-bind="click: clearErrors">
    &times;
  </button>
  <!-- ko foreach: errors -->
    <span class="glyphicon glyphicon-warning-sign"></span>
    <strong>error <span data-bind="text: num"></span>:</strong>
    <span data-bind="text: msg"></span>
    <br/>
  <!-- /ko -->  
</div>
