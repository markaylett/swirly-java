<%-- -*- html -*- --%>
<%--
   Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>

   All rights reserved.
--%>
<%@ page contentType="application/json;charset=utf-8" isErrorPage="true" language="java"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
{"num":500,"msg":"${fn:escapeXml(exception.message)}"}