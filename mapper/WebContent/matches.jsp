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
          <c:choose>
            <c:when test="${match.getClass().simpleName eq 'SimpleMatch'}">
              <td><b>${match.target}</b></td>
              <c:if test="${empty match.comment}">
                <td></td>
              </c:if>
              <c:if test="${not empty match.comment}">
                <td>{<i>${match.comment}</i>}</td>
              </c:if>
            </c:when>
            <c:when test="${match.getClass().simpleName eq 'CompositeMatch'}">
              <td><b>(${match.matchesString})</b></td>
              <td></td>
            </c:when>
          </c:choose>
        </tr>
      </c:forEach>
    </table>
  </matchestable>
  
  <messagetext><p>${message}</p></messagetext>
  
  <questiontext>${question}</questiontext>
  
  
</data>