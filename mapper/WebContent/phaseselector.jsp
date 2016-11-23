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
<link rel="stylesheet" href="css/jquery-ui.css">
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>

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

.phasebutton {
  width: 100px;
  height: 25px;
}
</style>

<script>
  var reset = false;

  $(document)
      .ready(
          function() {
            // Parse button
            $('#parsebutton')
                .click(
                    function(e) {
                      console.log(reset);
                      if (reset == false) {
                        var status = '${initiative.status}';
                        if (status == 'STRUCTURED') {
                          e.preventDefault();
                          showQuestion("You already have some Structural Mappings done! Parsing astah again will RESET all the mappigs. Are you sure?");
                        } else if (status == 'CONTENTED') {
                          e.preventDefault();
                          showQuestion("You already have some Content Mappings started! Parsing astah again will RESET all the mappigs. Are you sure?");
                        } else if (status == 'FINISHED') {
                          e.preventDefault();
                          showQuestion("You already have finished your Mappings! Parsing astah again will RESET ALL THE MAPPINGS. Are you sure?");
                        }

                      }
                    });
          });

  /* Shows a question message dialog. */
  function showQuestion(text) {
    $('#questionText').empty().append(text);
    $('#dialog-question').dialog({
      resizable : false,
      height : "auto",
      width : 600,
      modal : true,
      buttons : {
        Yes : function() {
          $(this).dialog('close');
          reset = true;
          $("#parsebutton").click();
          reset = false;
          return true;
        },
        No : function() {
          $(this).dialog('close');
          return false;
        }
      }
    });
  }
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">Phase Selection</h1>

  <h2><b>Initiative: ${initiative.domain}</b></h2>

  <!--   <div style="width: 220px; font-size: 80%"> -->
  <!--     <div class="done" style="padding: 7px"> -->
  <!--       <b>Done</b>: Phase Finished -->
  <!--     </div> -->
  <!--     <div class="open" style="padding: 7px"> -->
  <!--       <b>Open</b>: Phase Open to be Performed -->
  <!--     </div> -->
  <!--     <div class="closed" style="padding: 7px"> -->
  <!--       <b>Closed</b>: Phase not Open Yet -->
  <!--     </div> -->
  <!--   </div> -->

  <p />
  <div>
    <!-- Main DIV -->
    <div style="width: 60%; margin: auto">

      <div class="done">
        <div style="display: inline-block">
          <b>1) Initiative Info</b>
          <ul>
            <li><b>Domain: ${initiative.domain}</b></li>
            <li>Status: ${initiative.status}</li>
            <%--             <li>Purpose: ${initiative.purpose}</li> --%>
            <%--             <li>Scope: ${initiative.scope}</li> --%>
            <%--             <li>People: ${initiative.people}</li> --%>
          </ul>
        </div>
        <div style="display: inline-block; float: right">
          <form action="InitiativeStartServlet" method="POST">
            <input type="hidden" name="action" value="editInfo">
            <button class="phasebutton" id="infobutton">Edit Info</button>
          </form>
        </div>
      </div>

      <p />
      <c:choose>
        <c:when test="${initiative.status == 'INITIATED'}">
          <div class="open">
            <div style="display: inline-block">
              <b>2) Astah Parsing</b><br /> <br />
            </div>
        </c:when>
        <c:otherwise>
          <div class="done">
            <div style="display: inline-block">
              <b>2) Astah Parsing</b>
              <ul>
                <li>SEON View: ${initiative.domain}</li>
                <li>Integrated Model: ${initiative.integratedCM}</li>
                <li>Standard Models: ${initiative.standardCMs}</li>
                <li>Elements and Concepts: ${initiative.allNotions.size()}</li>
              </ul>
            </div>
        </c:otherwise>
      </c:choose>
      <div style="display: inline-block; float: right">
        <form action="AstahParseServlet" method="POST">
          <input type="hidden" name="action" value="openPage">
          <button class="phasebutton" id="parsebutton">Parse Astah</button>
        </form>
      </div>
    </div>


    <p />
    <c:choose>
      <c:when test="${initiative.status == 'INITIATED'}">
        <div class="closed">
          <b>3) Structural Mapping</b><br /> <br />
        </div>
      </c:when>
      <c:when test="${initiative.status == 'PARSED'}">
        <div class="open">
          <div style="display: inline-block">
            <b>3) Structural Mapping</b><br /> <br />
          </div>
          <div style="display: inline-block; float: right">
            <form action="PhaseSelectServlet" method="POST">
              <input type="hidden" name="action" value="doStructuralMapping">
              <button class="phasebutton" id="structuralbutton">Do Mapping</button>
            </form>
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="done">
          <div style="display: inline-block">
            <b>3) Structural Mapping</b><br />All Structural Mappings created <br />
          </div>
          <div style="display: inline-block; float: right">
            <button class="phasebutton" id="structuralbutton" disabled>Do Mapping</button>
          </div>
        </div>
      </c:otherwise>
    </c:choose>

    <p />
    <c:choose>
      <c:when test="${initiative.status == 'INITIATED' || initiative.status == 'PARSED'}">
        <div class="closed">
      </c:when>
      <c:otherwise>
        <div class="open">
      </c:otherwise>
    </c:choose>
    <b>4) Vertical Mapping</b><br />
    <p />
    <c:forEach items="${initiative.verticalContentMappings}" var="map" varStatus="loop">
      <div class="open">
        <div style="display: inline-block">
          <b>4.${loop.index+1}) ${map}</b><br /> Status: ${map.status}<br /> Coverage: ${map.coverage}%
        </div>
        <div style="display: inline-block; float: right">
          <form action="VerticalMappingServlet" method="POST">
            <input type="hidden" name="action" value="startMapping"> <input type="hidden" name="mapId"
              value="${map.id}">
            <button class="phasebutton" id="mappingbutton">Do Mapping</button>
          </form>
        </div>
      </div>
      <p />
    </c:forEach>
  </div>

  <p />
  <c:choose>
    <c:when test="${initiative.status == 'INITIATED' || initiative.status == 'PARSED'}">
      <div class="closed">
        <b>5) ICM Mapping</b><br /> <br />
      </div>
    </c:when>
    <c:otherwise>
      <!--       <div class="closed"> -->
      <div class="open">
        <div style="display: inline-block">
          <b>5) ICM Mapping</b><br /> <br />
          <c:forEach items="${initiative.diagonalContentMappings}" var="map" varStatus="loop">
            <b>Base: ${map.base}</b> Status: ${map.status} Coverage: +${map.coverage}%<br />
          </c:forEach>
        </div>
        <div style="display: inline-block; float: right">
          <form action="DiagonalMappingServlet" method="POST">
            <input type="hidden" name="action" value="startMapping">
            <button class="phasebutton" id="dmappingbutton">Do Mapping</button>
          </form>
        </div>
      </div>
    </c:otherwise>
  </c:choose>


  <p />
  <c:choose>
    <c:when test="${initiative.status == 'INITIATED' || initiative.status == 'PARSED'}">
      <div class="closed">
    </c:when>
    <c:otherwise>
      <div class="closed">
        <!--      <div class="open"> -->
    </c:otherwise>
  </c:choose>
  <b>6) Horizontal Mapping</b>
  <br />
  <p />
  <c:forEach items="${initiative.horizontalContentMappings}" var="map" varStatus="loop">
    <div class="closed">
      <div style="display: inline-block">
        <b>6.${loop.index+1}) ${map}</b><br /> Status: ${map.status}<br /> Coverage: ${map.coverage}%
      </div>
      <div style="display: inline-block; float: right">
        <button class="phasebutton" disabled>Do Mapping</button>
      </div>
    </div>
    <p />
  </c:forEach>
  </div>

  <p />
  <c:choose>
    <c:when test="${initiative.status == 'INITIATED' || initiative.status == 'PARSED'}">
      <div class="closed">
        <b>7) Harmonization Initiative Results</b><br /> <br />
      </div>
    </c:when>
    <c:otherwise>
      <div class="open">
        <div style="display: inline-block">
          <b>7) Harmonization Initiative Results</b><br /> <br />
        </div>
        <div style="display: inline-block; float: right">
          <form action="PhaseSelectServlet" method="POST">
            <input type="hidden" name="action" value="openResults">
            <button class="phasebutton" id="resultsbutton">See Results</button>
          </form>
        </div>
      </div>
    </c:otherwise>
  </c:choose>

  <div style="text-align: center; margin: 15px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="endSession">
      <button style="width: 120px; height: 25px;" id="sessionbutton">Exit Application</button>
    </form>
  </div>

  <a id="logfile" href="${logfile}" target="_blank"><code>log file</code></a>

  <!-- ##### Dialog Boxes ##### -->
  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <p><span class="ui-icon ui-icon-circle-check" style="float: left; margin: 0 7px 50px 0;"></span>
    <div id="messageText"></div>
    </p>
  </div>

  <!-- Question Message -->
  <div id="dialog-question" title="Question" hidden>
    <p><span class="ui-icon ui-icon-help" style="float: left; margin: 12px 12px 20px 0;"></span>
    <p>
    <div id="questionText"></div>
    </p>
  </div>
  <!-- ***** Dialog Boxes ***** -->

</BODY>
</HTML>

