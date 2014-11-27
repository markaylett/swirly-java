<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>

   All rights reserved.
--%>
<%@ page contentType="text/html;charset=utf-8" language="java" %>
<!DOCTYPE html>
<html lang="en">

<jsp:include page="include/head.jsp"/>

  <body>

<jsp:include page="include/navbar.jsp">
  <jsp:param name="active" value="user"/>
</jsp:include>

    <div class="container" style="padding: 90px 15px 0;">

<jsp:include page="include/alert.jsp"/>

      <button type="button" class="btn btn-default" data-toggle="modal" data-target="#userDialog"
              data-bind="click: clearUser">
        New User
      </button>

      <table class="table table-hover table-striped">
        <thead>
          <tr>
            <th>Mnem</th>
            <th>Display</th>
            <th>Email</th>
          </tr>
        </thead>
        <tbody data-bind="foreach: users">
          <tr>
            <td data-bind="text: mnem"></td>
            <td data-bind="text: display"></td>
            <td data-bind="text: email"></td>
          </tr>
        </tbody>
      </table>

    </div>

    <div id="userDialog" class="modal fade" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
            <h4 class="modal-title">New User</h4>
          </div>
          <div class="modal-body">
            <form role="form">
              <div class="form-group">
                <label for="mnem">Mnem:</label>
                <input id="mnem" type="text" class="form-control" data-bind="value: mnem"/>
              </div>
              <div class="form-group">
                <label for="display">Display:</label>
                <input id="display" type="email" class="form-control" data-bind="value: display"/>
              </div>
              <div class="form-group">
                <label for="email">Email:</label>
                <input id="email" type="text" class="form-control" data-bind="value: email"/>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            <button type="button" class="btn btn-primary" data-dismiss="modal"
                    data-bind="click: submitUser">Save</button>
          </div>
        </div>
      </div>
    </div>

<jsp:include page="include/footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/bootstrap.min.js"></script>
    <script type="text/javascript" src="js/bootstrap3-typeahead.min.js"></script>
    <script type="text/javascript" src="js/knockout.min.js"></script>

    <script type="text/javascript" src="js/swirly.js"></script>
    <script type="text/javascript" src="js/user.js"></script>
    <script type="text/javascript">
      $(initApp);
    </script>
  </body>
</html>
