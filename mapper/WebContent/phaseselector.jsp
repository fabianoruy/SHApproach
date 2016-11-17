<%@page import="shmapper.model.SHInitiative"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="java.util.List"%>
<%@ page import="shmapper.model.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SH Approach</title>

<link rel="stylesheet" href="css/style.css">
<script src="js/jquery.min.js"></script>

<style>
.done {
  border-radius: 10px;
  border: 3px solid #73AD21;
  padding: 10px;
  width: 97%;
}

.open {
  border-radius: 10px;
  border: 3px solid blue;
  padding: 10px;
  width: 97%;
}

.closed {
  border-radius: 10px;
  border: 3px solid gray;
  background: #F0F0F0;
  padding: 10px;
  width: 97%;
}
</style>
<script>
  
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">Phase Selection</h1>

  <h2><b>Initiative: ${initiative.domain}</b></h2>

  <div style="width: 220px; font-size: 80%">
    <div class="done" style="padding: 7px">
      <b>Done</b>: Phase Finished
    </div>
    <div class="open" style="padding: 7px">
      <b>Open</b>: Phase Open to be Performed
    </div>
    <div class="closed" style="padding: 7px">
      <b>Closed</b>: Phase not Open Yet
    </div>
  </div>

  <p />
  <div>
    <div style="width: 60%; margin: auto">
      <div class="done">
        <b>1) Initiative Info</b><br />
        <ul>
          <li>Domain: ${initiative.domain}</li>
          <li>Purpose: ${initiative.purpose}</li>
          <li>Scope: ${initiative.scope}</li>
          <li>People: ${initiative.people}</li>
        </ul>
      </div>

      <p />
      <div class="done">
        <b>2) Astah Parsing</b><br /> Astah succesfully parsed.<br />
        <ul>
          <li>SEON View: ${initiative.domain}</li>
          <li>Integrated Model: ${initiative.integratedCM}</li>
          <li>Standard Models: ${initiative.standardCMs}</li>
          <li>Elements and Concepts: ${initiative.allNotions.size()}</li>
        </ul>
      </div>

      <p />
      <div class="closed">
        <b>3) Structural Mapping</b><br /> <i>Feature not included in this version.</i>
      </div>

      <p />
      <div class="open">
        <b>4) Vertical Mapping</b><br />
        <p />
        <c:forEach items="${initiative.verticalMappings}" var="map" varStatus="loop">
          <div class="open">
            <div style="display: inline-block">
              <b>4.${loop.index+1}) ${map}</b><br /> Status: ${map.status}<br /> Coverage: ${map.coverage}%
            </div>
            <div style="display: inline-block; float: right">
              <form action="VerticalMappingServlet" method="POST">
                <input type="hidden" name="action" value="startMapping"> <input type="hidden" name="mapId"
                  value="${map.id}">
                <button id="mappingbutton">Do Mapping</button>
              </form>
            </div>
          </div>
          <p />
        </c:forEach>
      </div>
      <p />
      <div class="closed">
        <div style="display: inline-block">
          <c:set var="map" value="${initiative.diagonalMapping}" />
          <b>5) ICM Mapping: ${map}</b> <br /> Status: ${map.status}<br /> Coverage: ${map.coverage}%
        </div>
        <div style="display: inline-block; float: right">
          <button disabled>Do Mapping</button>
        </div>

      </div>

      <p />
      <div class="closed">
        <b>6) Horizontal Mapping</b><br />
        <p />
        <c:forEach items="${initiative.horizontalMappings}" var="map" varStatus="loop">
          <div class="closed">
            <div style="display: inline-block">
              <b>6.${loop.index+1}) ${map}</b><br /> Status: ${map.status}<br /> Coverage: ${map.coverage}%
            </div>
            <div style="display: inline-block; float: right">
              <button disabled>Do Mapping</button>
            </div>
          </div>
          <p />
        </c:forEach>
      </div>

      <p />
      <div class="closed">
        <div style="display: inline-block">
          <b>7) Harmonization Initiative Results</b><br /> To see the initiative results.
        </div>
        <div style="display: inline-block; float: right">
          <button disabled>See Results</button>
        </div>
      </div>

      <div style="text-align: center; margin: 10px 0 0 0">
        <form action="PhaseSelectServlet" method="POST">
          <input type="hidden" name="action" value="endSession">
          <button id="sessionbutton">Exit Application</button>
        </form>
      </div>

    </div>
  </div>
</BODY>
</HTML>

