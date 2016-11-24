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
.elembox {
  border-radius: 8px;
  border: 2px solid blue;
  padding: 8px;
  width: 400px;
  min-height: 140px;
}

label {
  font-weight: bold;
}

table {
  border-collapse: collapse;
}

td,th {
  border: 2px solid lightgrey;
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

.DISCARDED {
  background-color: lightred
}
</style>

<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>

<script>
  $(document).ready(function() {
    // Match button
    $('#createbutton').click(function() {
      if (checkFields()) {
        createElement();
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
  function createElement() {
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'create',
        name : $('#elemname').val(),
        ismt : $('#ismtypeid').val(),
        def  : $('#elemdef').val(),
        // all the selected elements with the values
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for removing an Element. */
  function removeElement(elemId) {
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
    console.log(responseXml);
//     var question = $(responseXml).find('questiontext').html();
//     var qtype = $(responseXml).find('questiontype').html();
//     //     switch (qtype) {
//     //     case 'CompositeEquivalent':
//     //       showCompositeQuestionE(question, doCompositeMatch);
//     //       break;
//     //     case 'CompositeEquivalentPart':
//     //       showCompositeQuestionEP(question, doCompositeMatch);
//     //       break;
//     //     case 'Basetype':
//     //       showQuestion(question, function() {
//     //         doMatch(true)
//     //       });
//     //       break;
//     //     default:
//     //       break;
//     //     }
//     $('#matchingsdiv').html($(responseXml).find('matchestable').html());
//     $('#messagediv').html($(responseXml).find('messagetext').html());
//     $('#commentsfield').empty();
//     $('#coveragediv').html($(responseXml).find('coveragetable').html());
//     $('#covernumber').text($(responseXml).find('coveragetext').html());
//     $('.icon').remove();
//     $('#standarddiv').append($(responseXml).find('coverageicons').html());
  }

  /* Check if the fields are well filled. */
  function checkFields() {
    // getting values
    var elem = $('#elemname').val();
    var type = $('#ismtype').val();
    var def = $('#elemdef').val();
    // at least one element selected

    // verifying
    if (elem == '') {
      showMessage("Inform a name for the new ICM Element.");
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

<!-- <BODY onload="doUpdate()"> -->
<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(5) ICM Elements Creation</h1>

  <h2><b>Content Mapping to ICM</b></h2>

  <h2>Map the Standards' Elements to new Elements in the ICM</h2>
  <p align="justify" style="width: 98%"><b>The remaining uncovered elements from the vertical mapping shall be
      mapped to new elements in the Integrated Conceptual Model.</b> <br /> In this phase, new elements can be added to the
    Integrated Content Model (ICM) (originally a copy of the <a href=#nothing onclick="showSeonView()">SEON View</a>) in
    order to provide new matches, raising the Standards’ coverage. The elements from each standard whose remain not or
    only partially covered are listed. Select the related ones and match them with a New ICM Element.</p>

  <!-- ##### Main Table Blocks ##### -->
  <table>
    <tr style="background-color: #99ccff">
      <c:forEach items="${initiative.diagonalContentMappings}" var="map">
        <th style="width: 30%">${map.base}</th>
      </c:forEach>
    </tr>
    <c:forEach items="${typesMatrix}" var="elements" varStatus="loop">
      <tr style="background-color: #ccf2ff">
        <td colspan="100%">${ufotypes[loop.index]}<c:if test="${empty ufotypes[loop.index]}">Type not defined</c:if>
        </td>
      </tr>
      <c:forEach items="${elements}" var="row">
        <tr>
          <c:forEach items="${row}" var="cell">
            <td class='${cell[2]}'>
              <div style="font-size: 90%">
                <c:if test="${not empty cell[0]}">
                ${cell[0]}
                <div style="float: right; display: inline">
                    <c:if test="${not empty cell[1]}">
                      <label title="${cell[1]}">[M]</label>
                    </c:if>
                    <select id="${cell[0].id}" title="Which is the coverage of the Element on the new Element?">
                      <option value="EMPTY"></option>
                      <option value="EQUIVALENT">[E]</option>
                      <option value="PARTIAL">[P]</option>
                      <option value="WIDER">[W]</option>
                      <option value="INTERSECTION">[I]</option>
                    </select>
                  </div>
                </c:if>
              </div>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>
    </c:forEach>
  </table>
  <!-- ***** Main Table Blocks ***** -->


  <!-- ##### Elements Creation Blocks ##### -->
  <h3>New ICM Element</h3>
  <div style="width: 879px; border: 1px solid gray; border-radius: 10px; padding: 10px">
    <div>
      <div style="display: inline-block">
        <label>Name</label><br /> <input id="elementname" type="text" placeholder="New Element's Name" size=65 style="height: 20px"
          required />
      </div>
      <div style="display: inline-block; margin: 0 0 0 35px">
        <label>ISM Type</label><br /> <select id="ismtypeid" style="height: 26px; width: 300px"
          title="Which is the generalization of the new element in the Integrated Structural Model (ISM)?" required>
          <c:forEach items="${initiative.integratedSM.notionsOrdered}" var="notion">
            <option value="${notion.id}">[${notion.indirectUfotype}] ${notion}</option>
          </c:forEach>
        </select>
      </div>
    </div>
    <div style="display: inline-block; width: 60%; margin: 10px 0 0 0">
      <label>Definition</label>
      <textarea id="elemdef" placeholder="New Element's Definition" rows="4" cols="115" required></textarea>
    </div>
    <div style="text-align: center; margin: 10px 0 0 0">
      <button id="createbutton" style="width: 80px; height: 30px; font-weight: bold">Create</button>
    </div>
  </div>

  <div style="display: inline-block; width: 900px; margin: 15px 0 0 0">
    <strong>Message</strong>
    <div id="messagediv" style="font-size: 90%; height: 80px; overflow: auto; border: 1px solid gray; border-radius: 8px; padding: 6px;">
    </div>
  </div>

  <div style="display: inline-block; width: 900px; margin: 15px 0 0 0">
    <strong>ICM Elements Created.
      <c:forEach items="${initiative.standardCMs}" var="standard">
        &nbsp;&nbsp;&nbsp;
        (Coverage ${standard}: <span id="cover${standard.id}">0%</span>)
        </c:forEach>
      </strong>
    <div id="elementsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 400px; padding: 3px"></div>
  </div>

  <div style="text-align: center; width: 900px; margin: 10px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="finishbutton">Back to Menu</button>
    </form>
  </div>
  <!-- ***** Elements Creation Blocks ***** -->



  <!-- ##### Dialog Boxes ##### -->
  <!-- Information Dialog -->
  <div id="seonview" title="Original SEON View" hidden>
    <IMG src="${initiative.seonView.diagram.path}" width="${initiative.seonView.diagram.width}">
  </div>

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