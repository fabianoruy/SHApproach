<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<data>
  <matchestable>
    <table>
      <c:forEach items="${mapping.matches}" var="match" varStatus="loop">
        <c:if test="${loop.index%2 == 0}">
          <tr>
        </c:if>
        <c:if test="${loop.index%2 == 1}">
          <tr style="background-color: #F0F0F0">
        </c:if>
        <td width="400px"><b>${match.source}</b></td>
        <td width="180px">${match.coverage.text}</td>
        <td width="400px"><b>${match.target}</b></td>
        <td>
          <c:if test="${not empty match.comment}">
            {<span title="${match.comment}" style="cursor: pointer"><i>C</i></span>}
          </c:if>
        </td>
        <td><img src="images/favicon-remove.ico" title="Remove Match" width="16px" style="cursor: pointer"
          onclick="removeMatch('${match.id}')" /></td>
      </tr>
      </c:forEach>
    </table>
  </matchestable>

  <coverageicons>
    <c:set var="hmap" value="${mapping}" />
    <c:set var="tablename" value="baseicons" />
    <c:forEach begin="1" end="2">
      <${tablename}>
        <c:forEach items="${hmap.base.diagram.positions}" var="npos">
          <c:forEach items="${hmap.fullyCoveredElements}" var="elem">
            <c:if test="${npos.notion eq elem}">
              <c:set var="xpos" value="${npos.xpos + npos.width - 24 + 12}" />
              <c:set var="ypos" value="${npos.ypos - 12}" />
              <img class="icon" src="images/favicon-full.ico" style="top:${ypos}px; left:${xpos}px; position:absolute"></img>
            </c:if>
          </c:forEach>
          <c:forEach items="${hmap.partiallyCoveredElements}" var="elem">
            <c:if test="${npos.notion eq elem}">
              <c:set var="xpos" value="${npos.xpos + npos.width - 24 + 12+1}" />
              <c:set var="ypos" value="${npos.ypos - 12-1}" />
              <img class="icon" src="images/favicon-part.ico" style="top:${ypos}px; left:${xpos}px; position:absolute"></img>
            </c:if>
          </c:forEach>
        </c:forEach>
      </${tablename}>
      <c:set var="hmap" value="${mapping.mirror}" />
      <c:set var="tablename" value="targicons" />
    </c:forEach>
  </coverageicons>

  <coveragetables>
    <c:set var="hmap" value="${mapping}" />
    <c:set var="tablename" value="basecovertable" />
    <c:forEach begin="1" end="2">
      <${tablename}>
        <b>${hmap.base} Elements:</b>
        <table>
          <c:forEach items="${hmap.nonCoveredElements}" var="elem">
            <tr>
              <td width="20px">&nbsp;-&nbsp;</td>
              <td width="400px">${elem}</td>
              <td>${elem.indirectUfotype}</td>
              <td> </td>
            </tr>
          </c:forEach>
          <c:forEach items="${hmap.partiallyCoveredElements}" var="elem">
            <tr>
              <td><img src="images/favicon-part.ico" width="16px" />&nbsp;</td>
              <td>${elem}</td>
              <td>${elem.indirectUfotype}</td>
              <td><c:if test="${hmap.checkCompositeChance(elem)}">
              Check Composite
              </c:if></td>
            </tr>
          </c:forEach>
          <c:forEach items="${hmap.fullyCoveredElements}" var="elem">
            <tr>
              <td><img src="images/favicon-full.ico" width="16px" />&nbsp;</td>
              <td>${elem}</td>
              <td>${elem.indirectUfotype}</td>
              <td> </td>
            </tr>
          </c:forEach>
        </table>
      </${tablename}>
      <c:set var="hmap" value="${mapping.mirror}" />
      <c:set var="tablename" value="targcovertable" />
    </c:forEach>
  </coveragetable>
  
  <basecovernumber>${mapping.coverage}</basecovernumber>
  <targcovernumber>${mapping.targetCoverage}</targcovernumber>
  
  <messagetext>${message}</messagetext>
  <questiontext>${question}</questiontext>
  <questiontype>${qtype}</questiontype>
  
</data>