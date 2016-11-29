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
        createElement(false); // false = not forced
      }
    });
  });

  /* Calls (the servlet via ajax) for updating the page with the current Mapping. */
  function doUpdate() {
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'update',
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a new ICM Element. */
  function createElement(forceBT) {
    var selected = [];
    var i = 0;
    $('.covers option:selected').each(function() {
      if($(this).val() != 'EMPTY') {
        selected[i++] = [$(this).parent().attr('id'), $(this).val()];
      }
    });
    
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'create',
        name : $('#elemname').val(),
        ismt : $('#ismtype').val(),
        def  : $('#elemdef').val(),
        elems : JSON.stringify(selected), // all the selected elements with the values
        force : forceBT
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

    /* Calls (the servlet via ajax) for removing an Element. */
    function removeElement(elemId) {
      console.log("AJAX");
      $.ajax({
        type : 'POST',
        url : 'DiagonalMappingServlet',
        data : {
          action : 'remove',
          elemId : elemId
        },
        success : function(responseXml) {
          updateMapping(responseXml);
        }
      });
    }

  /* Updates the page with the current information. */
  function updateMapping(responseXml) {
    //console.log(responseXml);
     var question = $(responseXml).find('questiontext').html();
     var qtype = $(responseXml).find('questiontype').html();
     switch (qtype) {
        case 'Basetype':
          showQuestion(question, function() {
            createElement(true); // true = basetype forced
          });
          break;
        default:
		  $('#uncovereddiv').html($(responseXml).find('uncovereddiv').html());
    	  $('#elemname').val("");
    	  $('#elemdef').val("");
    	  $('#ismtype').val(1);
    	  $('#coveragelabel').html($(responseXml).find('coveragelabel').html());
    	  $('#elementsdiv').html($(responseXml).find('elementsdiv').html());
          break;
        }
    $('#messagediv').html($(responseXml).find('messagediv').html());
  }

  /* Check if the fields are well filled. */
  function checkFields() {
    // getting values
    var elem = $('#elemname').val();
    var type = $('#ismtype').val();
    var def = $('#elemdef').val();
    var count = 0;
    $('.covers option:selected').each(function(index) {
      if($(this).val() != 'EMPTY') {
        count++;
      }
    });
    // verifying
    if (elem == '') {
      showMessage("Inform a name for the new ICM Element.");
      return false;
    } else if(type == 'EMPTY') {
      showMessage("Inform an ISM Type for the new ICM Element.");
      return false;
    } else if(def == '') {
      showMessage("Inform a definition for the new ICM Element.");
      return false;
    } else if (count == 0) {
      showMessage("At least one uncovered Element must be selected.");
      return false;
    } 
    return true;
  }
  
  function showSeonView() {
    $("#seonview").dialog({
      width : ${initiative.seonView.diagram.width}+50,
    });
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
  <h1 align="center">(5) ICM Elements Creation</h1>

  <h2>
    <b>Content Mapping to ICM</b>
  </h2>

  <h2>Map the Standards' Elements to new Elements in the ICM</h2>
  <p align="justify" style="width: 98%">
    <b>The remaining uncovered elements from the vertical mapping shall be mapped to new elements in the Integrated
      Conceptual Model.</b>
    <br />
    In this phase, new elements can be added to the Integrated Content Model (ICM) (originally a copy of the <a
      href=#nothing onclick="showSeonView()">SEON View</a>) in order to provide new matches, raising the Standards’
    coverage. The elements from each standard whose remain not or only partially covered are listed. Select the related
    ones and match them with a New ICM Element.
  </p>

  <!-- ##### Main Table Blocks ##### -->
  <div id="uncovereddiv">
    <!-- Table of uncovered Elements included here by ajax -->
  </div>
  <!-- ***** Main Table Blocks ***** -->


  <!-- ##### Elements Creation Blocks ##### -->
  <div style="width: 900px">
    <h3>New ICM Element</h3>
    <div>
      <div>
        <div style="display: inline-block">
          <label>
            <b>Name</b>
          </label>
          <br />
          <input id="elemname" type="text" placeholder="New Element's Name" size=70 style="height: 20px" required />
        </div>
        <div style="display: inline-block; margin: 0 0 0 50px">
          <label>
            <b>ISM Type</b>
          </label>
          <br />
          <select id="ismtype" style="height: 26px; width: 320px"
            title="Which is the generalization of the new element in the Integrated Structural Model (ISM)?" required>
            <option value="EMPTY"></option>
            <c:forEach items="${initiative.integratedSM.notionsOrdered}" var="notion">
              <option value="${notion.id}">[${notion.indirectUfotype}] ${notion}</option>
            </c:forEach>
          </select>
        </div>
      </div>
      <div style="display: inline-block; width: 60%; margin: 10px 0 0 0">
        <label>
          <b>Definition</b>
        </label>
        <textarea id="elemdef" placeholder="New Element's Definition" rows="4" cols="125" required></textarea>
      </div>
      <div style="text-align: center; margin: 10px 0 0 0">
        <button id="createbutton" style="width: 80px; height: 30px; font-weight: bold">Create</button>
      </div>
    </div>

    <div style="display: inline-block; width: 900px; margin: 20px 0 0 0">
      <label>
        <b>Message</b>
      </label>
      <div id="messagediv"
        style="font-size: 90%; height: 80px; overflow: auto; border: 1px solid gray; border-radius: 8px; padding: 6px;">
        <!-- Messages included here by ajax -->
      </div>
    </div>

    <div style="display: inline-block; width: 900px; margin: 15px 0 0 0">
      <label>
        <b>ICM Elements Created.</b>
      </label>
      <label id="coveragelabel">
        <!-- Coverage numbers included here by ajax -->
      </label>
      <div id="elementsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 450px; padding: 3px">
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
  <!-- ***** Elements Creation Blocks ***** -->



  <!-- ##### Dialog Boxes ##### -->
  <!-- Information Dialog -->
  <div id="seonview" title="Original SEON View" hidden>
    <IMG src="${initiative.seonView.diagram.path}" width="${initiative.seonView.diagram.width}">
  </div>

  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <!--     <p> -->
    <div id="messageText"></div>
    <!--     </p> -->
  </div>

  <!-- Question Message -->
  <div id="dialog-question" title="Question" hidden>
    <!--     <p> -->
    <div id="questionText"></div>
    <!--     </p> -->
  </div>
  <!-- ***** Dialog Boxes ***** -->
</BODY>
</HTML>