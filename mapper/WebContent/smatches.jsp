<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<data>
  <smapinfo>
    <p>Structural Mappings defined for this initiative.</p>
    <table border=1 cellpadding=6 style="width: 100%; font-size: 95%">
      <tbody style="border: 1px solid gray">
        <tr style="background-color: #F0F0F0">
          <c:forEach items="${smaptableHeader}" var="thead">
            <th width="150"><b>${thead}</b></th>
          </c:forEach>
        </tr>
        <c:forEach items="${smaptable}" var="line">
          <tr>
            <c:forEach items="${line}" var="cell">
              <td>${cell}</td>
            </c:forEach>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <p><small>* ISM Elements</small></p>
  </smapinfo>
</data>