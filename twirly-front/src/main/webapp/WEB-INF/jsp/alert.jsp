<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
--%>
<div class="alert alert-warning alert-dismissible" role="alert" style="display: none;"
     data-bind="visible: hasErrors">
  <button type="button" class="close" data-bind="click: clearErrors">
    &times;
  </button>
  <ul data-bind="foreach: errors">
    <li>
      <strong data-bind="text: num"></strong>:&nbsp;<span data-bind="text: msg"></span>
    </li>
  </ul>
</div>
