<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="application/xml" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<data>
  <matchestable>
    <table>
      <c:forEach items="${mapping.matches}" var="match">
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
                <td>{<span title="${match.comment}" style="cursor:pointer"><i>C</i></span>}</td>
              </c:if>
            </c:when>
            <c:when test="${match.getClass().simpleName eq 'CompositeMatch'}">
              <td><b>(${match.matchesString})</b></td>
              <td></td>
            </c:when>
          </c:choose>
          <td>
            <img id="${match.id}" src="images/favicon-remove.ico" title="Remove Match" width="16px" style="cursor:pointer" onclick="removeMatch('${match.id}')"/>
          </td>
        </tr>
      </c:forEach>
    </table>
  </matchestable>
  
  <coveragetable>
    <b>${mapping.base} Elements:</b>
    <table>
      <c:forEach items="${mapping.nonCoveredElements}" var="elem">
        <tr>
          <td>${" -"}</td>
          <td> ${elem}</td>
        </tr>
      </c:forEach>
      <c:forEach items="${mapping.partiallyCoveredElements}" var="elem">
        <tr>
          <td><img src="images/favicon-part.ico" width="16px"/></td>
          <td> ${elem}</td>
        </tr>
      </c:forEach>
      <c:forEach items="${mapping.fullyCoveredElements}" var="elem">
        <tr>
          <td><img src="images/favicon-full.ico" width="16px"/></td>
          <td> ${elem}</td>
        </tr>
      </c:forEach>
      
    </table>
  </coveragetable>
  
  <coverageicons>
    <c:forEach items="${mapping.base.diagram.positions}" var="npos">
      <c:forEach items="${mapping.fullyCoveredElements}" var="elem">
        <c:if test="${npos.notion eq elem}">
          <c:set var="xpos" value="${npos.xpos + npos.width - 24 + 12}"/>
          <img class="icon" src="images/favicon-full.ico" style="top:${npos.ypos - 12}px; left:${xpos}px; position:absolute"></img>
        </c:if>
      </c:forEach>
      <c:forEach items="${mapping.partiallyCoveredElements}" var="elem">
        <c:if test="${npos.notion eq elem}">
          <c:set var="xpos" value="${npos.xpos + npos.width - 24 + 12}"/>
          <img class="icon" src="images/favicon-part.ico" style="top:${npos.ypos - 12}px; left:${xpos}px; position:absolute"></img>
        </c:if>
      </c:forEach>
    </c:forEach>
  </coverageicons>
  
  
  <coveragetext>${mapping.coverage}%</coveragetext>
  
  <messagetext>${message}</messagetext>
  
  <questiontext>${question}</questiontext>
  
</data>