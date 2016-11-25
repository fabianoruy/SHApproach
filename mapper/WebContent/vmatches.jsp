<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<data>
  <matchestable>
    <table>
      <c:forEach items="${mapping.matches}" var="match" varStatus="loop">
        <c:if test="${loop.index%2 == 0}"><tr></c:if>
        <c:if test="${loop.index%2 == 1}"><tr style="background-color:#F0F0F0"></c:if>
          <td width="400px"><b>${match.source}</b></td>
          <td width="180px">${match.coverage.text}</td>
          <td width="400px"><b>${match.target}</b></td>
          <td><c:if test="${not empty match.comment}">
            {<span title="${match.comment}" style="cursor:pointer"><i>C</i></span>}
          </c:if></td>
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
          <td>${" - "}</td>
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
  <questiontype>${qtype}</questiontype>
  
</data>