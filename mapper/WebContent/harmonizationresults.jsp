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
  padding: 5px;
}

.FULLY {
  background-color: #ccffcc
}

.PARTIALLY {
  background-color: #ffffcc
}

.NONCOVERED {
  background-color: white
}

.EMPTY {
  background-color: #f2f2f2
}

.DISCARDED {
  background-color: lightred
}
</style>

<script>
  
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(7) Harmonization Results</h1>


  <!------------------------  INDEX  ------------------------>
  <h2><b>Structural Mappings</b></h2>
  <table>
    <tr>
      <th style="width: 200px; background-color: #e6e6e6">VERTICAL</th>
      <c:forEach items="${initiative.verticalStructuralMappings}" var="map">
        <td style="width: 350px; text-align: center"><b>${map}</b><br />(${map.matches.size()} matches)</td>
      </c:forEach>
    </tr>
    <tr>
      <th style="background-color: #e6e6e6">INTEGRATED</th>
      <c:forEach items="${initiative.diagonalStructuralMappings}" var="map">
        <td style="text-align: center"><b>${map}</b><br />(${map.matches.size()} matches)</td>
      </c:forEach>
    </tr>
    <tr>
      <th style="background-color: #e6e6e6">HORIZONTAL</th>
      <c:forEach items="${initiative.horizontalStructuralMappings}" var="map">
        <td style="text-align: center"><b>${map}</b><br />(${map.matches.size()} matches)</td>
      </c:forEach>
    </tr>
  </table>

  <br />
  <br />
  <h2><b>Content Mappings</b></h2>
  <table>
    <tr>
      <th style="width: 200px; background-color: #e6e6e6">VERTICAL</th>
      <c:forEach items="${initiative.verticalContentMappings}" var="map">
        <td style="width: 350px; text-align: center"><a href="#${map.id}"><b>${map}</b></a><br />(${map.matches.size()}
          matches)</td>
      </c:forEach>
    </tr>
    <tr>
      <th style="background-color: #e6e6e6">INTEGRATED</th>
      <c:forEach items="${initiative.diagonalContentMappings}" var="map">
        <td style="text-align: center"><a href="#dmappings"><b>${map}</b></a><br />(${map.matches.size()} matches)</td>
      </c:forEach>
    </tr>
    <tr>
      <th style="background-color: #e6e6e6">HORIZONTAL</th>
      <c:forEach items="${initiative.horizontalContentMappings}" var="map">
        <td style="text-align: center"><a href="#${map.id}"><b>${map.base} &#8660; ${map.target}</b></a><br />(${map.matches.size()} matches)</td>
      </c:forEach>
    </tr>
  </table>
  
  <br />
  <br />
  <h2><b>Standards Coverage</b></h2>
  <table style="text-align: center">
    <tr style="background-color: #e6e6e6">
      <th style="width: 200px">BASE x TARGET</th>
      <th style="width: 200px">SEON View + ICM</th>
      <c:forEach items="${coverageIndex}" var="base">
        <th style="width: 200px">${base[0][0]}</th>
      </c:forEach>
    </tr>
    <c:forEach items="${coverageIndex}" var="base">
      <tr>
        <th style="background-color: #e6e6e6">${base[0][0]}</th>
        <c:forEach items="${base}" begin="1" var="cover">
          <c:if test="${empty cover[0]}">
            <td>-</td>
          </c:if>
          <c:if test="${not empty cover[0]}">
            <td><a href="#${cover[1]}">${cover[0]}%</a></td>
          </c:if>
        </c:forEach>
      </tr>
    </c:forEach>
  </table>

  <!------------------------  VERTICAL MAPPINGS  ------------------------>
  <br />
  <br />
  <hr size="5" noshade/>
  <br />
  <h2><b>Content Vertical Mappings</b> (Standard to SEON)</h2>
  <c:forEach items="${vmapsMatrix}" var="table" varStatus="mloop">
    <c:set var="vmap" value="${initiative.verticalContentMappings[mloop.index]}" />
    <h3 id="${vmap.id}"><b>${vmap}</b> (Coverage: ${vmap.coverage}%)</h3>
    <div style="width: 70%">
      <table>
        <tr style="background-color: #999999">
          <th style="width: 450px">${vmap.base}&nbsp;Element</th>
          <th style="width: 400px">Match</th>
          <th style="width: 450px">${vmap.target}&nbsp;Concept</th>
        </tr>
        <c:forEach items="${table}" var="types" varStatus="tloop">
          <tr style="background-color: #c8c8c8">
            <td colspan="100%">
              <c:if test="${not empty ufotypes[tloop.index]}">${ufotypes[tloop.index]}S</c:if>
              <c:if test="${empty ufotypes[tloop.index]}">Type not defined</c:if>
            </td>
          </tr>
          <c:forEach items="${types}" var="data">
            <c:set var="match" value="${data[0]}" />
            <tr style="font-size: 90%">
              <c:if test="${data[2] != 0}">
                <td rowspan="${data[2]}" class="${data[1]}" title="${match.source.definition}">${match.source}</td>
              </c:if>
              <td>${match.matchType}<c:if test="${not empty match.comment}">
                  <br />{<i>${match.comment}</i>}</c:if>
              </td>
              <c:if test="${match['class'].simpleName eq 'SimpleMatch'}">
                <c:set var="title" value="title='${match.target.definition}'" />
              </c:if>
              <td ${title}>${match.target}</td>
            </tr>
          </c:forEach>
        </c:forEach>
      </table>
    </div>
    <br />
  </c:forEach>

  <!------------------------  DIAGONAL MAPPINGS  ------------------------>
  <br />
  <hr />
  <br />
  <h2 id="dmappings"><b>Integrated Content Model (ICM) Mappings</b></h2>
  <div style="width: 70%">
    <table>
      <tr style="background-color: #c8c8c8">
        <th style="width: 500px" colspan="2">Matches</th>
        <th style="width: 200px">New ICM Element</th>
        <th style="width: 300px">Definition</th>
      </tr>
      <c:forEach items="${dmapsMatrix}" var="elementline" varStatus="loopout">
        <c:set var="elem" value="${elementline[0]}" />
        <c:set var="rows" value="${elementline[1].size()}" />
        <c:if test="${loopout.index%2 == 1}">
          <c:set var="rowcolor" value=" background-color: #F0F0F0;" />
        </c:if>
        <c:if test="${loopout.index%2 == 0}">
          <c:set var="rowcolor" value=" background-color: white;" />
        </c:if>
        <c:forEach items="${elementline[1]}" var="match" varStatus="loop">
          <tr style="font-size: 90%; ${rowcolor} ">
            <td style="width: 250px">${match.source.model}:&nbsp;${match.source}</td>
            <td style="text-align: center; width: 50px">${match.matchType.abbreviation}</td>
            <c:if test="${loop.index == 0}">
              <td style="width: 150px" rowspan="${rows}"><b>${elem}</b> <br />(${elem.basetypes[0]})</td>
              <td style="width: 400px" rowspan="${rows}" style="font-size: 90%">${elem.definition}</td>
            </c:if>
          </tr>
        </c:forEach>
      </c:forEach>
    </table>
  </div>
  
  
  <!------------------------  VERTICAL COVERAGE  ------------------------>
  <br />
  <hr />
  <br />
  <h2 id="vcoverage"><b>Coverage: Standards' Elements by Ontologies (SEON View + ICM) - Vertical Mappings</b></h2>
  <table style="font-size: 80%">
    <tr><td class='FULLY'>Fully Covered Element</td></tr>
    <tr><td class='PARTIALLY'>Partially Covered Element</td></tr>
    <tr><td class='NONCOVERED'>Not Covered Element</td></tr>
  </table>
  <br />
  <b style="font-size:90%; color:red">Partially and Not Covered elements shall be justified.</b>
  <table>
    <tr style="background-color: #999999">
      <c:forEach items="${initiative.standardCMs}" var="std" varStatus="sloop">
        <th style="width: 30%; font-size: 120%">${std} (${coverageIndex[sloop.index][1][0]}%)</th>
      </c:forEach>
    </tr>
    <c:forEach items="${coverageMatrix}" var="elements" varStatus="loop">
      <tr style="background-color: #c8c8c8">
        <td colspan="100%">
          <c:if test="${not empty ufotypes[loop.index]}">${ufotypes[loop.index]}S</c:if>
          <c:if test="${empty ufotypes[loop.index]}">Type not defined</c:if>
        </td>
      </tr>
      <c:forEach items="${elements}" var="row">
        <tr>
          <c:forEach items="${row}" var="cell">
            <c:set var="element" value="${cell[0]}" />
            <c:set var="matches" value="${cell[1]}" />
            <c:set var="situation" value="${cell[2]}" />
            <td class='${situation}'>
              <div style="font-size: 90%">
                <c:if test="${not empty element}">
                  <label title="${element.definition}">${element}</label>
                  <div style="float: right; display: inline">
                    <c:if test="${not empty matches}">
                      <label title="${matches}">[M]</label>
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
  
  <br />
  <br />
  <h2 id="coverageanalysis"><b>Coverage Analysis: Elements' Justifications</b></h2>
  <br />
  
  <!------------------------  HORIZONTAL MAPPINGS  ------------------------>
  <br />
  <br />
  <hr size="5" noshade/>
  <br />
  <h2><b>Content Horizontal Mappings</b> (Standard to Standard)</h2>
  <c:forEach items="${hmapsMatrix}" var="table" varStatus="mloop">
    <c:set var="hmap" value="${allhmaps[mloop.index]}" />
    <h3 id="${hmap.id}"><b>${hmap}</b> (Coverage: ${hmap.coverage}%)</h3>
    <div style="width: 70%">
      <table>
        <tr style="background-color: #999999">
          <th style="width: 450px">${hmap.base}&nbsp;Element</th>
          <th style="width: 400px">Match</th>
          <th style="width: 450px">${hmap.target}&nbsp;Element</th>
        </tr>
        <c:forEach items="${table}" var="types" varStatus="tloop">
          <tr style="background-color: #c8c8c8">
            <td colspan="100%">
              <c:if test="${not empty ufotypes[tloop.index]}">${ufotypes[tloop.index]}S</c:if>
              <c:if test="${empty ufotypes[tloop.index]}">Type not defined</c:if>
            </td>
          </tr>
          <c:forEach items="${types}" var="data">
            <c:set var="match" value="${data[0]}" />
            <tr style="font-size: 90%">
              <c:if test="${data[2] != 0}">
                <td rowspan="${data[2]}" class="${data[1]}" title="${match.source.definition}">${match.source}</td>
              </c:if>
              <td>${match.matchType}&nbsp;
                <c:if test="${match.deduced}">
                  (<span title="Deduced Match">D</span>)
                </c:if>
                <c:if test="${not empty match.comment}">
                  <br />{<i>${match.comment}</i>}
                </c:if>
              </td>
              <c:if test="${match['class'].simpleName eq 'SimpleMatch'}">
                <c:set var="title" value="title='${match.target.definition}'" />
              </c:if>
              <td ${title}>${match.target}</td>
            </tr>
          </c:forEach>
        </c:forEach>
      </table>
    </div>
    <br />
  </c:forEach>
  
  <br />
  <br />
  <div style="text-align: center; width: 100%; margin: 15px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="finishbutton">Return to Menu</button>
    </form>
  </div>

</BODY>
</HTML>