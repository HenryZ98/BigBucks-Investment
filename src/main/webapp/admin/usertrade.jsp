<%@page import="com.ibm.security.appscan.altoromutual.model.Trade"%>
<%@page import="com.ibm.security.appscan.altoromutual.model.Holding"%>
<%@page import="com.ibm.security.appscan.altoromutual.util.DBUtil"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>



<jsp:include page="/header.jspf"/>

<div id="wrapper" style="width: 99%;">
  <jsp:include page="/bank/membertoc.jspf"/>
  <td valign="top" colspan="3" class="bb">
    <%@ page import="com.ibm.security.appscan.altoromutual.model.Account" %>
    <%@ page import="java.text.DecimalFormat" %>
    <%@ page import="java.sql.SQLException" %>
    <%@ page import="java.sql.Timestamp" %>
    <%@ page import="java.text.SimpleDateFormat" %>


    <div class="fl" style="width: 99%;">
      <%
        Account[] allAccounts = DBUtil.getAllAccounts();
      %>
      <h1>Users Summary</h1>
      <form id="trades" name="trades" method="post" action="/admin/viewTrade">
        <table width="700" border="0" style="padding-bottom:10px;">
          <tr><td>
            <br><b>User's Holding Records</b>
            <table border=1 cellpadding=2 cellspacing=0 width='540'>
              <tr style="color:Black;background-color:#BFD7DA;font-weight:bold;">
                <th width=90>Account ID</th>
                <th width=90>Stock Symbol</th>
                <th width=90>Stock Name</th>
                <th width=90>Shares Holding</th>
                <th width=90>Price per share</th>
              </tr>
            </table>
            <DIV ID='userHolding' STYLE='width:590px; padding:0px; margin: 0px' ><table border=1 cellpadding=2 cellspacing=0 width='540'>
              <% Holding[] holdings = DBUtil.getHolding(allAccounts);
                for (Holding holding: holdings) {
                  double dblcostPrice = holding.getCostPrice();
                  String dollarFormat = (dblcostPrice<1)?"$0.00":"$.00";
                  String costPrice = new DecimalFormat(dollarFormat).format(dblcostPrice);
              %>
              <tr>
                <td width=90><%=holding.getAccountId()%></td>
                <td width=90><%=holding.getStockSymbol()%></td>
                <td width=90><%=holding.getStockName()%></td>
                <td width=90 align=right><%=holding.getHoldingAmount()%></td>
                <td width=90 align=right><%=costPrice%></td>
              </tr>
              <% } %>
            </table></DIV>
          </td></tr>
          <tr><td>
            <br><b>Today's Trade Records</b>
            <table border=1 cellpadding=2 cellspacing=0 width='700'>
              <tr style="color:Black;background-color:#BFD7DA;font-weight:bold;">
                <th width=90>Trade ID</th>
                <th width=90>Account ID</th>
                <th width=90>Description</th>
                <th width=90>Stock Symbol</th>
                <th width=90>Stock Name</th>
                <th width=90>Shares</th>
                <th width=90>Price per share</th>
              </tr>
            </table>
            <DIV ID='record' STYLE='width:710px; padding:0px; margin: 0px' ><table border=1 cellpadding=2 cellspacing=0 width='700'>
              <% Trade[] trades = new Trade[0];
                try {
                  Timestamp date = new Timestamp(new java.util.Date().getTime());
                  SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
                  String dateStr = sd.format(date);
                  trades = DBUtil.getTradeRecords(dateStr,dateStr,allAccounts,0);
                } catch (SQLException e) {
                  e.printStackTrace();
                }
                for (Trade trade: trades) {
                  double dblcostPrice = trade.getPrice();
                  String dollarFormat = (dblcostPrice<1)?"$0.00":"$.00";
                  String price = new DecimalFormat(dollarFormat).format(dblcostPrice);
              %>
              <tr>
                <td width=90><%=trade.getTradeId()%></td>
                <td width=90><%=trade.getAccountId()%></td>
                <td width=90><%=trade.getTradeType()%></td>
                <td width=90><%=trade.getStockSymbol()%></td>
                <td width=90><%=trade.getStockName()%></td>
                <td width=90 align=right><%=trade.getAmount()%></td>
                <td width=90 align=right><%=price%></td>
              </tr>
              <% } %>
            </table></DIV>
          </td></tr>
        </table>
      </form>
    </div>
  </td>
</div>
