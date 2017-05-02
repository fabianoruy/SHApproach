<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<data>
<uncovereddiv>
<table>
<form action="">
  <tr style="background-color: #808080">
    <c:forEach items="${initiative.diagonalContentMappings}" var="map">
      <th style="width: 30%; font-size: 120%">${map.base}</th>
    </c:forEach>
  </tr>
  <c:forEach items="${typesMatrix}" var="elements" varStatus="loop">
    <tr style="background-color: #c8c8c8">
      <td colspan="100%">${ufotypes[loop.index]}<c:if test="${empty ufotypes[loop.index]}">Type not defined</c:if>
      </td>
    </tr>
    <c:forEach items="${elements}" var="row">
      <tr>
        <c:forEach items="${row}" var="cell">
          <td class='${cell[2]}'>
            <div style="font-size: 90%">
              <c:if test="${not empty cell[0]}">
                <input type="radio" name="element" onclick="selectElement('${cell[0].id}', '${cell[0]}')" value="${cell[0].id}"><label title="Definition: ${cell[0].definition}"> ${cell[0]}</label>
                <div style="float: right; display: inline">
                  <c:if test="${not empty cell[1]}">
                    <label title="${cell[1]}">[M]</label>
                  </c:if>
                </div>
              </c:if>
            </div>
          </td>
        </c:forEach>
      </tr>
    </c:forEach>
  </c:forEach>
</form>
</table>
</uncovereddiv>

<coveragelabel>
  <c:forEach items="${coverages}" var="stdcover">
    &nbsp;&nbsp;&nbsp;(Coverage ${stdcover[0]}: ${stdcover[1]}% + ${stdcover[2]}%)
  </c:forEach>
</coveragelabel>
  
<decisionsdiv>
<table style="border:0">
  <c:forEach items="${decisions}" var="decision" varStatus="loop">
    <c:set var="color" value="style='background-color:#F0F0F0'"/>
    <c:if test="${loop.index%2 == 0}"><c:set var="color" value=""/></c:if>
    <tr ${color}>
      <c:set var="elem" value="${decision.element}"/>
      <td style="width: 50px">${elem.model}</td>
      <td style="width: 300px" title="${elem.definition}">(${elem.indirectUfotype})&nbsp;${elem}</td>
      <td style="width: 80px"><b>${decision.reason.text}</b></td>
      <td style="width: 400px">
        <span style="cursor:pointer" title="Edit" onclick="editJustification('${elem.id}', '${decision.justification}')">${decision.justification}</span>
      </td>
      <td><img src="images/favicon-remove.ico" title="Remove Decision" width="16px" style="cursor: pointer"
            onclick="showQuestion('Do you want to remove the <b>decision (${decision.reason.text})</b> on the element <b>${elem}</b>?', function() {removeDecision('${elem.id}');})" />
      </td>
    </tr>
  </c:forEach>
</table>
</decisionsdiv>

<messagediv>${message}</messagediv>

<questiontext>${question}</questiontext>

</data>