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
          <td width="180px" title="Coverage: ${match.coverage}">${match.matchType.text}</td>
          <c:set var="tdef" value="title='[]'"/>
          <c:if test="${match['class'].simpleName eq 'SimpleMatch'}">
            <c:set var="tdef" value="title='${match.target.definition}'"/>
          </c:if>
          <td width="400px" ${tdef}><b>${match.target}</b></td>
          <c:if test="${match.deduced}"><td title="Deduced Match">D</td></c:if>
          <c:if test="${not match.deduced}"><td></td></c:if>
          <td>
            <c:choose>
              <c:when test="${not empty match.comment}">
                {<span title="${match.comment}" style="cursor:pointer" onclick="editComment('${match.id}', '${match.comment}')"><i><b>C</b></i></span>}
              </c:when>
              <c:when test="${(empty match.comment) && (match.matchType == 'EQUIVALENT' || match.matchType == 'PARTIAL')}">
                {<span style="cursor:pointer" onclick="editComment('${match.id}', '')"><i>C</i></span>}
              </c:when>
              <c:when test="${(empty match.comment) && (match.matchType == 'WIDER' || match.matchType == 'OVERLAP')}">
                {<span style="cursor:pointer; color:red" title="add comment!" onclick="editComment('${match.id}', '')"><i>C</i></span>}
              </c:when>
            </c:choose>
          </td>
          <td><img src="images/favicon-remove.ico" title="Remove Match" width="16px" style="cursor: pointer"
            onclick="removeMatch('${match.id}')" /></td>
        </tr>
      </c:forEach>
    </table>
  </matchestable>

  <mirrormatchestable>
    <table>
      <c:forEach items="${mapping.mirror.matches}" var="match" varStatus="loop">
        <c:set var="color" value="style='background-color:#F0F0F0'"/>
        <c:if test="${loop.index%2 == 0}"><c:set var="color" value=""/></c:if>
        <tr ${color}>
          <td width="400px" title="${match.source.definition}"><b>${match.source}</b></td>
          <td width="180px" title="Coverage: ${match.coverage}">${match.matchType.text}</td>
          <c:set var="tdef" value="title='[]'"/>
          <c:if test="${match['class'].simpleName eq 'SimpleMatch'}">
            <c:set var="tdef" value="title='${match.target.definition}'"/>
          </c:if>
          <td width="400px" ${tdef}><b>${match.target}</b></td>
          <c:if test="${match.deduced}"><td title="Deduced Match">D</td></c:if>
          <c:if test="${not match.deduced}"><td></td></c:if>
          <td>
            <c:choose>
              <c:when test="${not empty match.comment}">
                {<span title="${match.comment}" style="cursor:pointer" onclick="editComment('${match.id}', '${match.comment}')"><i><b>C</b></i></span>}
              </c:when>
              <c:when test="${(empty match.comment) && (match.matchType == 'EQUIVALENT' || match.matchType == 'PARTIAL')}">
                {<span style="cursor:pointer" onclick="editComment('${match.id}', '')"><i>C</i></span>}
              </c:when>
              <c:when test="${(empty match.comment) && (match.matchType == 'WIDER' || match.matchType == 'OVERLAP')}">
                {<span style="cursor:pointer; color:red" title="add comment!" onclick="editComment('${match.id}', '')"><i>C</i></span>}
              </c:when>
            </c:choose>
          </td>
          <td><img src="images/favicon-remove.ico" title="Remove Match" width="16px" style="cursor: pointer"
            onclick="removeMatch('${match.id}')" /></td>
        </tr>
      </c:forEach>
    </table>
  </mirrormatchestable>


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
              <td width="30px">&nbsp;-&nbsp;</td>
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
              <td><c:if test="${hmap.isCompositeAble(elem)}">
                <button style="font-size: 80%" title="Check for Composite Matching" onclick="checkComposite('${hmap.id}', '${elem.id}')">Composite?</button>
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
  
  <deduceresults>${deductionresults}</deduceresults>
  
</data>