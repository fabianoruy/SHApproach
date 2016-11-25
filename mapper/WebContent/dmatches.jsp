<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<data> <uncovereddiv>
<table>
  <tr style="background-color: #808080">
    <c:forEach items="${initiative.diagonalContentMappings}" var="map">
      <th style="width: 30%; font-size:120%">${map.base}</th>
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
                <label title="${cell[0].definition}">${cell[0]}</label>
                <div style="float: right; display: inline">
                  <c:if test="${not empty cell[1]}">
                    <label title="${cell[1]}">[M]</label>
                  </c:if>
                  <c:if test="${cell[2] == 'FULLY'}">
                    <select disabled><option></option><option>[W]</option></select>
                  </c:if>
                  <c:if test="${cell[2] != 'FULLY'}">
                    <select class="covers" id="${cell[0].id}"
                      title="Which is the coverage of the Element on the new Element?">
                      <option value="EMPTY"></option>
                      <option value="EQUIVALENT">[E]</option>
                      <option value="PARTIAL">[P]</option>
                      <option value="WIDER">[W]</option>
                      <option value="INTERSECTION">[I]</option>
                    </select>
                  </c:if>
                </div>
              </c:if>
            </div>
          </td>
        </c:forEach>
      </tr>
    </c:forEach>
  </c:forEach>
</table>
</uncovereddiv> <coveragelabel> <c:forEach items="${coverages}" var="stdcover">
    &nbsp;&nbsp;&nbsp;(Coverage ${stdcover[0]}: ${stdcover[1]} + ${stdcover[2]})
  </c:forEach> </coveragelabel> <elementsdiv>
<table>
  <c:forEach items="${initiative.integratedCM.elements}" var="element">
    <tr>
      <td><b>${element}</b> <br />(${element.basetypes[0]})</td>
      <td style="font-size: 90%">${element.definition}</td>
      <td><img src="images/favicon-remove.ico" title="Remove Element" width="16px" style="cursor: pointer"
        onclick="showQuestion('Do you want to remove the element <b>${element}</b> together with all its matches?', function() {removeElement('${element.id}');})" /></td>
    </tr>
  </c:forEach>
</table>
</elementsdiv> <messagediv>${message}</messagediv> <questiontext>${question}</questiontext> <questiontype>${qtype}</questiontype> </data>