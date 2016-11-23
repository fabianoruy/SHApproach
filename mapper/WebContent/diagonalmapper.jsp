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
        createElement(false);
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

  /* Calls (the servlet via ajax) for creating a Match. */
  function createElement(force) {
    $.ajax({
      type : 'POST',
      url : 'DiagonalMappingServlet',
      data : {
        action : 'create',
        //         elem : $('#elementidfield').val(),
        //         conc : $('#conceptidfield').val(),
        //         cover : $('#coveringfield').val(),
        //         comm : $('#commentsfield').val(),
        force : force
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
        action : 'removeElement',
        matchId : matchId
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Updates the page with the current information. */
  function updateMapping(responseXml) {
    console.log(responseXml);
    var question = $(responseXml).find('questiontext').html();
    var qtype = $(responseXml).find('questiontype').html();
    //     switch (qtype) {
    //     case 'CompositeEquivalent':
    //       showCompositeQuestionE(question, doCompositeMatch);
    //       break;
    //     case 'CompositeEquivalentPart':
    //       showCompositeQuestionEP(question, doCompositeMatch);
    //       break;
    //     case 'Basetype':
    //       showQuestion(question, function() {
    //         doMatch(true)
    //       });
    //       break;
    //     default:
    //       break;
    //     }
    $('#matchingsdiv').html($(responseXml).find('matchestable').html());
    $('#messagediv').html($(responseXml).find('messagetext').html());
    $('#commentsfield').empty();
    $('#coveragediv').html($(responseXml).find('coveragetable').html());
    $('#covernumber').text($(responseXml).find('coveragetext').html());
    $('.icon').remove();
    $('#standarddiv').append($(responseXml).find('coverageicons').html());
  }

  /* Highlight the diagrams' elements/concepts and make then selectable. */
  $(function() {
    $('.map').maphilight();
    $('#squidheadlink').mouseover(function(e) {
      $('#squidhead').mouseover();
    }).mouseout(function(e) {
      $('#squidhead').mouseout();
    });

    // Fills the Element field from the map click
    $('#StandardMap').click(function(e) {
      var name = stdJson[e.target.id].name;
      var btype = stdJson[e.target.id].basetype;
      var def = stdJson[e.target.id].definition;
      $('#elementidfield').val(e.target.id);
      $('#elementfield').text(name);
      $('#ebasetypefield').text("(" + btype + ")");
      $('#edefinitionfield').text(def);
    });

    // Fill the Concept field from the map click
    $('#OntologyMap').click(function(e) {
      var name = ontoJson[e.target.id].name;
      var btype = ontoJson[e.target.id].basetype;
      var def = ontoJson[e.target.id].definition;
      $('#conceptidfield').val(e.target.id);
      $('#conceptfield').text(name);
      $('#cbasetypefield').text("(" + btype + ")");
      $('#cdefinitionfield').text(def);
    });
  });

  /* Check if the fields are well filled. */
  function checkFields() {
    // getting values
    var elem = $('#elementidfield').val();
    var conc = $('#conceptidfield').val();
    var relc = $('#coveringfield').val();
    var comm = $('#commentsfield').val();

    // verifying
    if (elem == '' || (conc == '' && relc != 'NOCOVERAGE')) {
      showMessage("Select an element from each diagram.");
      return false;
    }
    if (comm == '' && (relc == 'WIDER' || relc == 'INTERSECTION')) {
      showMessage("WIDER and INTERSECTION matches require a comment explaining the non-covered part(s).");
      return false;
    }
    return true;
  }

  function showCoverageInfo() {
    $("#coverinfo").dialog({
      width : 1000
    });
  }

  function showCoverageStatus() {
    $("#coveragediv").dialog({
      width : 530
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

  /* Shows a question message dialog for Composite Matching (1 yes option). */
  function showCompositeQuestionE(text, compositeFunction) {
    $('#compositeText').empty().append(text);
    $('#dialog-composite').dialog({
      resizable : false,
      height : "auto",
      width : 600,
      modal : true,
      buttons : {
        "Yes, the element is EQUIVALENT to the sun of the concepts." : function() {
          $(this).dialog('close');
          compositeFunction('EQUIVALENT');
        },
        "No, the element remains not fully covered." : function() {
          $(this).dialog('close');
        }
      }
    });
  }

  /* Shows a question message dialog for Composite Matching (2 yes options). */
  function showCompositeQuestionEP(text, compositeFunction) {
    $('#compositeText').empty().append(text);
    $('#dialog-composite').dialog({
      resizable : false,
      height : "auto",
      width : 600,
      modal : true,
      buttons : {
        "Yes, the element is EQUIVALENT to the sun of the concepts." : function() {
          $(this).dialog('close');
          compositeFunction('EQUIVALENT');
        },
        "Yes, the element is PART OF the sun of the concepts." : function() {
          $(this).dialog('close');
          compositeFunction('PARTIAL');
        },
        "No, the element remains not fully covered." : function() {
          $(this).dialog('close');
        }
      }
    });
  }
</script>
</HEAD>

<BODY onload="doUpdate()">
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(5) ICM Elements Creation</h1>

  <h2><b>Content Mapping to ICM</b></h2>

  <h2>Map the Standards' Elements to new Elements in the Integrated Content Model</h2>
  <p align="justify" style="width: 98%"><b>The standards' elements shall be mapped to the domain ontologies'
      concepts (vertical mapping).</b> <br /> This tool supports the mapping by providing features for selecting the
    desired elements and concepts and establishing different types of matches between them. Select an element from the
    left-hand side model (the Standard Model) and select a concept from the right-hand side model (the SEON View). Then,
    choose the suitable <a href=#nothing onclick="showCoverageInfo()">coverage relation</a> and add comments for the
    match. Try to achieve a larger standard coverage by making as many suitable matches as possible.</p>

  <!-- ##### Main Table Blocks ##### -->
  <table>
    <tr style="background-color: #99ccff">
      <c:forEach items="${initiative.diagonalContentMappings}" var="map">
        <th style="width: 30%">${map.base}</th>
      </c:forEach>
    </tr>
    <c:forEach items="${typesMatrix}" var="elements" varStatus="loop">
      <tr style="background-color: #ccf2ff">
        <td colspan="100%">${ufotypes[loop.index]}</td>
      </tr>
      <c:forEach items="${elements}" var="row">
        <tr>
          <c:forEach items="${row}" var="cell">
            <td class='${cell[1]}'>
              <div>
                ${cell[0]}
                <div style="float: right; display: inline">
                  <select id="${cell[0].id}cover" style="height: 22px"
                    title="Which is the coverage of the Element on the new Element?">
                    <option value="EMPTY"></option>
                    <option value="EQUIVALENT">[E]</option>
                    <option value="PARTIAL">[P]</option>
                    <option value="WIDER">[W]</option>
                    <option value="INTERSECTION">[I]</option>
                  </select>
                </div>
              </div>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>
    </c:forEach>
  </table>
  <!-- ***** Main Table Blocks ***** -->


  <br />
  <br />
  <!-- ##### Elements Creation Blocks ##### -->
  <h3>New ICM Element</h3>
  <div style="width: 60%; border: 1px solid gray; border-radius: 10px; padding: 10px">
    <div>
      <div style="display: inline-block; width: 60%">
        <label>Name</label><br /> <input type="text" placeholder="New Element's Name" size=60 style="height: 20px"
          required />
      </div>
      <div style="display: inline-block; float: right; width: 40%">
        <label>ISM Type</label><br /> <select id="ismtype" style="height: 25px; width: 250px"
          title="Which is the generalization of the new element in the Integrated Structural Model (ISM)?" required>
          <option value="EMPTY">Process</option>
          <option value="EQUIVALENT">Activity</option>
          <option value="PARTIAL">Artifact</option>
          <option value="WIDER">...</option>
        </select>
      </div>
    </div>
    <div style="display: inline-block; width: 60%; margin: 10px 0 0 0">
      <label>Definition</label>
      <textarea id="commentsfield" placeholder="New Element's Definition" rows="4" cols="120" required></textarea>
    </div>
    <div style="text-align: center; margin: 10px 0 0 0">
      <button id="createbutton" style="width: 80px; height: 30px; font-weight: bold">Create</button>
    </div>
  </div>


  <br />
  <br />
  <div style="display: inline-block; overflow: auto; min-width: 800px; width: 60%; margin: 15px 0 0 0">
    <strong>Message</strong>
    <div id="messagediv"
      style="font-size: 90%; border: 1px solid gray; height: 70px; border-radius: 10px; padding: 8px;"></div>
  </div>

  <div style="display: inline-block; min-width: 800px; width: 60%; margin: 15px 0 0 0">
    <strong>Matches Established. (<a href=#nothing onclick="showCoverageStatus()">Coverage: <span
        id="covernumber">0%</span></a>)
    </strong>
    <div id="matchingsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 400px; padding: 3px"></div>
  </div>

  <div style="text-align: center; width: 998px; margin: 10px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="finishbutton">Back to Menu</button>
    </form>
  </div>



  <!-- ***** Elements Creation Blocks ***** -->

  <!-- Information Dialog -->
  <div id="coveragediv" title="Coverage Status"
    style="font-size: 95%; overflow: auto; border: 1px solid gray; width: 500px; height: 500px" hidden></div>

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