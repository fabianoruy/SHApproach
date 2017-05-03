<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="shmapper.model.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SH Approach</title>

<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="css/jquery-ui.css">

<style>
table {
	border-collapse: collapse;
}

td, th {
	border: 1px solid lightgrey;
	padding: 4px;
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

<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>

<script>
  $(document).ready(function() {
    $('#createbutton').click(function() {
      if (checkFields()) {
        createDecision();
      }
    });
  });

  /* Calls (the servlet via ajax) for updating the page. */
  function doUpdate() {
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'updateAnalysis',
      },
      success : function(responseXml) {
        updateAnalysis(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a new Decision. */
  function createDecision() {
    console.log("create");
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'createDecision',
        elemId : $('#elemid').val(),
        reason : $('#reason').val(),
        justif  : $('#justif').val(),
      },
      success : function(responseXml) {
        updateAnalysis(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for removing a Decision. */
  function removeDecision(elemId) {
    console.log("AJAX");
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'removeDecision',
        elemId : elemId
      },
      success : function(responseXml) {
        updateAnalysis(responseXml);
      }
    });
  }
  
  /* Shows a form for editing Decision Justification. */
  function editJustification(elemId, justif) {
    $('#justifText').val(justif);
    $('#dialog-form').dialog({
      resizable : false,
      height : "auto",
      width : 700,
      modal : true,
      buttons : {
        Save : function() {
          $(this).dialog('close');
          changeJustification(elemId, $('#justifText').val());
        },
        Cancel : function() {
          $(this).dialog('close');
        }
      }
    });
  }

  /* Calls (the servlet via ajax) for changing a Decision Justification. */
  function changeJustification(elemId, justif) {
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'changeJustification',
        elemId : elemId,
        justif : justif
      },
      success : function(responseXml) {
        updateAnalysis(responseXml);
      }
    });
  }


  /* Updates the page with the current information. */
  function updateAnalysis(responseXml) {
    //console.log(responseXml);
    var question = $(responseXml).find('questiontext').html();
    $('#uncovereddiv').html($(responseXml).find('uncovereddiv').html());
    $('#elemid').val("");
    $('#elemname').text("");
    $('#reason').val('EMPTY');
    $('#justif').val("");
    //$('#coveragelabel').html($(responseXml).find('coveragelabel').html());
    $('#decisionsdiv').html($(responseXml).find('decisionsdiv').html());
    //$('#messagediv').html($(responseXml).find('messagediv').html());
  }
  
  function selectElement(elemID, elemname) {
    $('#elemid').val(elemID);
    $('#elemname').text(elemname);
  }

  /* Check if the fields are well filled. */
  function checkFields() {
    if ($('#elemid').val() == '') {
      showMessage("Select an uncovered Element.");
      return false;
    } else if ($('#reason').val() == 'EMPTY') {
      showMessage("Select a Reason.");
      return false;
    } else if ($('#justif').val() == '') {
      showMessage("Inform a Justification for this decison.");
      return false;
    }
    return true;
  }

  /* Shows a message dialog. */
  function showMessage(text) {
    $('#messageText').empty().append(text);
    $('#dialog-message').show();
    $('#dialog-message').dialog({
      modal : true,
      width : 500,
      buttons : {
        Ok : function() {
          $(this).dialog('close');
        }
      }
    });
  }

  /* Shows a question message dialog. */
  function showQuestion(text, yesFunction) {
    console.log("question")
    $('#questionText').empty().append(text);
    $('#dialog-question').dialog({
      resizable : false,
      height : "auto",
      width : 600,
      modal : true,
      buttons : {
        Yes : function() {
          $(this).dialog('close');
          yesFunction();
        },
        No : function() {
          $(this).dialog('close');
        }
      }
    });
  }
</script>
</HEAD>

<!-- <BODY> -->
<BODY onload="doUpdate()">
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(5.1) Coverage Analysis</h1>

  <h2><b>Elements' Coverage Analysis</b></h2>

  <h2>Analyze the remaining uncovered Elements and Justify</h2>
  <p align="justify" style="width: 98%">
    <b>The remaining uncovered elements (after finishing the Vertical and ICM Mappings) shall be justified.</b>
    <br />
    In this phase, the remaining uncovered Standards portions (if there are) can be analyzed and the uncovered elements justified.
    Thus, elements not considered covered in the Vertical and ICM Mappings can be explained. For doind this, just select an uncovered
    element and give a reason and a justification. The main reasons are: <br/>
    <b>ALREADY COVERED</b>, when the element itself has no enough matches, but all its aspects are already covered by the matches of other elements; and<br/>
    <b>OUT OF SCOPE</b>, when the element (or its unconvered portion) is not relevant for the initiative scope.    
  </p>

  <!-- ##### Main Table Blocks ##### -->
  <div id="uncovereddiv">
    <!-- Table of uncovered Elements included here by ajax -->
  </div>
  <!-- ***** Main Table Blocks ***** -->


  <!-- ##### Elements Creation Blocks ##### -->
  <div>
    <h3>New Decision</h3>
    <div>
      <div>
        <div style="display: inline-block; width: 595px">
          <label><b>Element</b></label><br />
          <input id="elemid" type="hidden" /> <span id="elemname">(select an element)</span> <br />
        </div>
        <div style="display: inline-block">
          <label><b>Reason</b></label><br /> <select id="reason" style="height: 26px; width: 300px"
            title="Why does this element was not covered during the mappings?" required>
            <option value="EMPTY">-</option>
            <option value="ALREADYCOVERED">ALREADY COVERED</option>
            <option value="OUTOFSCOPE">OUT OF SCOPE</option>
            <option value="OTHER">OTHER (describe)</option>
          </select>
        </div>
      </div>
      
      <div style="display: inline-block; width: 900px; margin: 10px 0 0 0">
        <label><b>Justification</b></label><br/>
        <textarea id="justif" placeholder="Decision Justification" rows="4" cols="125" required></textarea>
        <div style="text-align: center; margin: 10px 0 0 0">
          <button id="createbutton" style="width: 80px; height: 30px; font-weight: bold">Add</button>
        </div>
      </div>
    </div>

    <!--     <div style="display: inline-block; width: 900px; margin: 20px 0 0 0">
      <label><b>Message</b></label>
      <div id="messagediv"
        style="font-size: 90%; height: 60px; overflow: auto; border: 1px solid gray; border-radius: 8px; padding: 6px;">
        Messages included here by ajax
      </div>
    </div>
 -->
    <div style="display: inline-block; width: 900px; margin: 15px 0 0 0">
      <label><b>Analysis Decisions</b></label>
      <label id="coveragelabel">
        <!-- Coverage numbers included here by ajax -->
      </label>
      <div id="decisionsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 360px; padding: 3px">
        <!-- New Elements included here by ajax -->
      </div>
    </div>

    <div style="text-align: center; width: 900px; margin: 10px 0 0 0">
      <form action="PhaseSelectServlet" method="POST">
        <input type="hidden" name="action" value="openSelection">
        <button id="finishbutton">SAVE and Return to Menu</button>
      </form>
    </div>
  </div>
  <!-- ***** Decision Creation Blocks ***** -->



  <!-- ##### Dialog Boxes ##### -->

  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <div id="messageText"></div>
  </div>
  
  <!-- Justification Editing -->
  <div id="dialog-form" title="Inform the new Justification." hidden>
    <form>
      <div style="width: 500px; margin: 15px 0 0 0">
        <b>Decision Justification</b> <br />
        <textarea id="justifText" rows="4" cols="80"></textarea>
      </div>
      <input type="submit" tabindex="-1" style="position: absolute; top: -1000px">
    </form>
  </div>

  <!-- Question Message -->
  <div id="dialog-question" title="Question" hidden>
    <div id="questionText"></div>
  </div>

  <!-- ***** Dialog Boxes ***** -->
</BODY>
</HTML>