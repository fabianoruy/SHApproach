<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<data>
  <matchestable>
    <table>
      <c:forEach items="${mapping.matches}" var="match" varStatus="loop">
        <c:set var="color" value="style='background-color:#F0F0F0'"/>
        <c:if test="${loop.index%2 == 0}"><c:set var="color" value=""/></c:if>
        <tr ${color}>
          <td width="400px" title="${match.source.definition}"><b>${match.source}</b></td>
          <td width="180px">${match.matchType.text}</td>
          <c:set var="tdef" value="title='[]'"/>
          <c:if test="${match['class'].simpleName eq 'SimpleMatch'}">
            <c:set var="tdef" value="title='${match.target.definition}'"/>
          </c:if>
          <td width="400px" ${tdef}><b>${match.target}</b></td>
          <c:set var="comm" value="C"/>
          <c:if test="${not empty match.comment}">
            <c:set var="comm" value="<b>C</b>"/>
          </c:if>
          <td>{<span title="${match.comment}" style="cursor:pointer" onclick="editComment('${match.id}', '${match.comment}')"><i>${comm}</i></span>}</td>
          <td><img src="images/favicon-remove.ico" title="Remove Match" width="16px" style="cursor:pointer" onclick="removeMatch('${match.id}')"/></td>
        </tr>
      </c:forEach>
    </table>
  </matchestable>
  
  <coveragetable>
    <b>${mapping.base} Elements:</b>
    <table>
      <c:forEach items="${mapping.nonCoveredElements}" var="elem">
        <tr>
          <td width="25px">${" - "}&nbsp;</td>
          <td> ${elem}</td>
          <td>${elem.indirectUfotype}</td>
        </tr>
      </c:forEach>
      <c:forEach items="${mapping.partiallyCoveredElements}" var="elem">
        <tr>
          <td><img src="images/favicon-part.ico" width="16px"/>&nbsp;</td>
          <td> ${elem}</td>
          <td>${elem.indirectUfotype}</td>
        </tr>
      </c:forEach>
      <c:forEach items="${mapping.fullyCoveredElements}" var="elem">
        <tr>
          <td><img src="images/favicon-full.ico" width="16px"/>&nbsp;</td>
          <td> ${elem}</td>
          <td>${elem.indirectUfotype}</td>
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
          <c:set var="xpos" value="${npos.xpos + npos.width - 24 + 12+1}"/>
          <img class="icon" src="images/favicon-part.ico" style="top:${npos.ypos - 12+1}px; left:${xpos}px; position:absolute"></img>
        </c:if>
      </c:forEach>
      <c:forEach items="${mapping.discardedElements}" var="elem">
        <c:if test="${npos.notion eq elem}">
          <c:set var="xpos" value="${npos.xpos + npos.width - 24 + 12+1}"/>
          <img class="icon" src="images/favicon-discarded.ico" style="top:${npos.ypos - 12+1}px; left:${xpos}px; position:absolute"></img>
        </c:if>
      </c:forEach>
    </c:forEach>
  </coverageicons>
  
  
  <coveragetext>${mapping.coverage}%</coveragetext>
  
  <messagetext>${message}</messagetext>
  <questiontext>${question}</questiontext>
  <questiontype>${qtype}</questiontype>
  
  <coveragelist>${coveragelist}</coveragelist>
  
</data>