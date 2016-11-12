<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="application/xml" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<data>
  <matchestable>
    <table>
      <c:forEach items="${matches}" var="match">
        <tr>
          <td><b>${match.source}</b></td>
          <td>${match.coverage.text}</td>
          <td><b>${match.target}</b></td>
          <td>{<i>${match.comment}</i>}</td>
        </tr>
      </c:forEach>
    </table>
  </matchestable>
  
  <messagetext><p>${message}</p></messagetext>
  
  <questiontext>${question}</questiontext>
  
  
</data>