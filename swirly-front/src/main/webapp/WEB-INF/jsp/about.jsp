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

    <div class="container">

      <div class="page-header" style="margin-bottom: 16px;">
        <h3>About</h3>
      </div>

      <h4>What are we doing?</h4>

      <p>Swirly Cloud will match buyers and sellers in a online Exchange with markets for various
        kinds of goods and services. Traders will be notified of their trades, and these trades will
        stipulate the terms of the transaction, including what will be exchanged and when, in what
        quantity, and at what price. Transactions may be settled directly between counter-parts or
        via an intermediary, but, like eBay, we will not ultimately be responsible for settling the
        transactions, and we do not intend to compete with high-frequency streaming platforms or
        APIs.</p>

      <h4>Why are we doing it?</h4>

      <p>We believe that cost-cutting by financial organisations will lead them to consider new software
        architectures that can dynamically grow or shrink to meet demand.</p>

      <p>Large online retailers and tech companies have invested heavily in technologies, such as
        Google's BigTable, that can cope with Internet-scale workloads, and they are providing
        access to their global infrastructure and compute as a managed service (the Cloud). The
        landscape of modern software development has fundamentally changed as a consequence. We
        think that the world of finance can benefit tremendously from these innovations.</p>

      <p>The beauty of Cloud computing is that we only pay for what we use, so we don't need to
        invest our capital on infrastructure and extra capacity for future growth. The Cloud also
        simplifies maintenance, operations, disaster recovery and security, so that we can focus our
        efforts on writing great software.</p>

      <h4>How are we doing it?</h4>

      <p>Our system has been designed from the ground-up to run efficiently in the Cloud with
        limited resources.  It draws on lessons learned from years spent building front-office
        trading applications, and adapts their best ideas for the Cloud. The matching-engine at the
        heart of our application was originally developed using high-frequency trading techniques to
        achieve microsecond latencies.</p>

      <p>We deliberately develop and test our system with very limited resource quotas, so that we
        remain focused on our bottom line. We have also taken care to avoid vendor lock-in, by
        ensuring that our core application only uses open standards and technologies. This gives us
        the option to target different application containers, run on dedicated infrastructure, or
        to open-source the software.</p>

      <h4>Who is doing it?</h4>

      <p>The project was originally started by Mark Aylett as a proof-of-concept for a low-latency
        matching-engine. Mark has now been joined by several former colleagues from the
        industry.</p>

    </div>

    <jsp:include page="footer.jsp"/>

    <!-- Bootstrap core JavaScript
         ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>
  </body>
</html>
