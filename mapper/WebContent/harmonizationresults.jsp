<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SH Approach</title>

<link rel="stylesheet" href="css/style.css">
<script src="js/jquery.min.js"></script>

<style>
table {
  border-collapse: collapse;
}

td,th {
  border: 2px solid lightgrey;
  padding: 6px;
}
</style>

<script>
  
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(7) Harmonization Results</h1>

  <h2><b>Structural Mappings</b></h2>
  <table>
    <tr>
      <th style="width: 200px">Vertical</th>
      <c:forEach items="${initiative.verticalStructuralMappings}" var="map">
        <td style="width: 350px; text-align: center"><b>${map}</b><br />(${map.matches.size()})</td>
      </c:forEach>
    </tr>
    <tr>
      <th>Integrated</th>
      <c:forEach items="${initiative.diagonalStructuralMappings}" var="map">
        <td style="text-align: center"><b>${map}</b><br />(${map.matches.size()})</td>
      </c:forEach>
    </tr>
    <tr>
      <th>Horizontal</th>
      <c:forEach items="${initiative.horizontalStructuralMappings}" var="map">
        <td style="text-align: center"><b>${map}</b><br />(${map.matches.size()})</td>
      </c:forEach>
    </tr>
  </table>

  <p />
  <p />
  <h2><b>Content Mappings</b></h2>
  <table>
    <tr>
      <th style="width: 200px">Vertical</th>
      <c:forEach items="${initiative.verticalContentMappings}" var="map">
        <td style="width: 350px; text-align: center"><b>${map}</b><br />(${map.matches.size()})</td>
      </c:forEach>
    </tr>
    <tr>
      <th>Integrated</th>
      <c:forEach items="${initiative.diagonalContentMappings}" var="map">
        <td style="text-align: center"><b>${map}</b><br />(${map.matches.size()})</td>
      </c:forEach>
    </tr>
    <tr>
      <th>Horizontal</th>
      <c:forEach items="${initiative.horizontalContentMappings}" var="map">
        <td style="text-align: center"><b>${map}</b><br />(${map.matches.size()})</td>
      </c:forEach>
    </tr>
  </table>

  <br />
  <br />

  <h2><b>Content Vertical Mappings</b></h2>
  <c:forEach items="${initiative.verticalContentMappings}" var="map">
    <h3><b>${map}</b> (${map.coverage}%)</h3>
    <table>
      <tr>
        <th style="width: 450px">Element</th>
        <th style="width: 350px">Match</th>
        <th style="width: 450px">Concept</th>
      </tr>
      <c:forEach items="${map.matches}" var="match">
        <tr>
          <td>${match.source}</td>
          <td>${match.coverage}<c:if test="${not empty match.comment}"> {<i>${match.comment}</i>}</c:if></td>
          <td>${match.target}</td>
        </tr>
      </c:forEach>
    </table>
    <br />
  </c:forEach>

  <div style="text-align: center; width: 998px; margin: 10px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="finishbutton">Back to Menu</button>
    </form>
  </div>

</BODY>
</HTML>