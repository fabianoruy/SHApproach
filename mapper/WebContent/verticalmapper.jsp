<%@page import="org.apache.jasper.tagplugins.jstl.core.ForEach"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
	min-height: 140px;
}

.concbox {
	border-radius: 8px;
	border: 2px solid green;
	padding: 8px;
	min-height: 140px;
}
</style>
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/maphilight.js"></script>

<script>
  var stdJson = JSON.parse('${stdJson}');
  var ontoJson = JSON.parse('${ontoJson}');
  //console.log(json);

  $(document).ready(function() {
    // Match button
    $('#matchbutton').click(function() {
      if (checkFields()) {
        doMatch(false);
      }
    });
  });

  /* Calls (the servlet via ajax) for updating the page with the current Mapping. */
  function doUpdate() {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'update',
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a Match. */
  function doMatch(forceBT) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'match',
        elem : $('#elementidfield').val(),
        conc : $('#conceptidfield').val(),
        cover : $('#coveringfield').val(),
        comm : $('#commentsfield').val(),
        force : forceBT
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a Composite Match. */
  function doCompositeMatch(cover) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'compositeMatch',
        elem : $('#elementidfield').val(),
        cover : cover
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for removing a Match. */
  function removeMatch(matchId) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'removeMatch',
        matchId : matchId
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
    case 'CompositeEquivalent':
      showCompositeQuestionE(question, doCompositeMatch);
      break;
    case 'CompositeEquivalentPart':
      showCompositeQuestionEP(question, doCompositeMatch);
      break;
    case 'Basetype':
      showQuestion(question, function() {
        doMatch(true)
      });
      return;
      break;
    default:
      break;
    }
    $('#matchingsdiv').html($(responseXml).find('matchestable').html());
    $('#messagediv').html($(responseXml).find('messagetext').html());
    $('#commentsfield').val("");
    $('#coveragediv').html($(responseXml).find('coveragetable').html());
    $('#covernumber').text($(responseXml).find('coveragetext').html());
    $('.icon').remove();
    $('#standarddiv').append($(responseXml).find('coverageicons').html());
    $('#messagediv').scrollTop(1E10);
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

  /* Cleans the Ontology Concept when Not Covered is selected. */
  function cleanOC() {
    if ($('#coveringfield :selected').val() == 'NOCOVERAGE') {
      $('#conceptidfield').val('');
      $('#conceptfield').val('');
    }
  }

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
      width : 530,
      height : 700
    });
  }

  /* Shows a message dialog. */
  function showMessage(text) {
    $('#messageText').empty().append(text);
    $('#dialog-message').show();
    $('#dialog-message').dialog({
      modal : true,
      width : 600,
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
      width : 700,
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
      width : 700,
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
      width : 700,
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
  <h1 align="center">(4) Vertical Mappings</h1>

  <h2>
    <b>Content Vertical Mapping</b>
  </h2>

  <h2>Map the Standards' Models to the Domain Ontologies</h2>
  <p align="justify" style="width: 98%">
    <b>The standards' elements shall be mapped to the domain ontologies' concepts (vertical mapping).</b>
    <br />
    This tool supports the mapping by providing features for selecting the desired elements and concepts and
    establishing different types of matches between them. Select an element from the left-hand side model (the Standard
    Model) and select a concept from the right-hand side model (the SEON View). Then, choose the suitable <a
      href=#nothing onclick="showCoverageInfo()">coverage relation</a> and add comments for the match. Try to achieve a
    larger standard coverage by making as many suitable matches as possible.
  </p>

  <!-- ##### Diagrams Blocks ##### -->
  <div>
    <div style="width: 49.5%; display: inline-block">
      <b>Standard: ${standard.name}</b>
    </div>
    <div style="width: 49%; display: inline-block">
      <b>Ontology: ${ontology.name}</b>
    </div>
  </div>

  <div style="width: 100%; height: 100%">
    <div id="standarddiv"
      style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid blue; position: relative">
      <IMG src="${standard.diagram.path}" width="${standard.diagram.width}" class="map" usemap="#Standard">
      <MAP id="StandardMap" name="Standard">
        <c:forEach var="entry" items="${stdCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}">
        </c:forEach>
      </MAP>
      <!-- diagram icons included here by ajax -->
    </div>

    <div style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid green">
      <IMG src="${ontology.diagram.path}" width="${ontology.diagram.width}" class="map" usemap="#Ontology">
      <MAP id="OntologyMap" name="Ontology">
        <c:forEach var="entry" items="${ontoCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}">
        </c:forEach>
      </MAP>
    </div>
  </div>
  <!-- ***** Diagrams Blocks ***** -->

  <!-- ##### Match Blocks ##### -->
  <h3>How do the Standard's Elements cover the Ontology's Concepts?</h3>
  <div style="display: inline-block; width: 100%">
    <div style="width: 410px; display: inline-block; float: left">
      <label>
        <b>Standard Element</b>
      </label>
      <br />
      <div class="elembox" title="Select an Element from the Standard model">
        <input id="elementidfield" type="hidden" />
        <span id="elementfield" style="font-weight: bold">(select an element)</span>
        <br />
        <span id="ebasetypefield"></span>
        <br />
        <span id="edefinitionfield" style="font-size: 90%"></span>
      </div>
    </div>

    <div style="width: 140px; display: inline-block; float: left; margin: 0 20px 0 20px">
      <div style="display: inline-block">
        <b>Coverage (<a href=#nothing onclick="showCoverageInfo()">?</a>)
        </b>
        <br />
        <select id="coveringfield" title="Which is the coverage of the Element on the Concept?" onchange="cleanOC(this)">
          <option value="EQUIVALENT">[E] EQUIVALENT</option>
          <option value="PARTIAL">[P] PART OF</option>
          <option value="WIDER">[W] WIDER</option>
          <option value="INTERSECTION">[I] INTERSECTION</option>
          <!--         <option value="NOCOVERAGE">[-] NO COVERAGE</option> -->
        </select>
      </div>
      <div style="display: inline-block; width: 140px; height: 138px; position: relative">
        <button id="matchbutton"
          style="width: 80px; height: 30px; font-weight: bold; position: absolute; bottom: 0; right: 30px;">MATCH!</button>
      </div>
    </div>


    <div style="width: 410px; display: inline-block; float: left">
      <label>
        <b>Ontology Concept</b>
      </label>
      <br />
      <div class="concbox" title="Select a Concept from the Ontology model.">
        <input id="conceptidfield" type="hidden" />
        <span id="conceptfield" style="font-weight: bold">(select concept)</span>
        <br />
        <span id="cbasetypefield"></span>
        <br />
        <span id="cdefinitionfield" style="font-size: 90%"></span>
      </div>
    </div>

  </div>

  <div style="width: 600px; margin: 15px 0 0 0">
    <b>Covering Comments</b>
    <br />
    <textarea id="commentsfield" title="Describe the non-covered portions of the Element." rows="4" cols="139"></textarea>
  </div>

  <div style="display: inline-block; width: 998px; margin: 15px 0 0 0">
    <strong>Message</strong>
    <div id="messagediv"
      style="font-size: 90%; height: 80px; overflow: auto; border: 1px solid gray; border-radius: 8px; padding: 6px;">
      <!-- Messages included here by ajax -->
    </div>
  </div>

  <!--   <div> -->
  <div style="display: inline-block; width: 998px; margin: 15px 0 0 0">
    <strong>Matches Established. (<a href=#nothing onclick="showCoverageStatus()">Coverage: <span
        id="covernumber">0%</span></a>)
    </strong>
    <div id="matchingsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 400px; padding: 3px">
      <!-- Matches included here by ajax -->
    </div>
  </div>
  <!--   </div> -->

  <div style="text-align: center; width: 998px; margin: 10px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="finishbutton">SAVE and Back to Menu</button>
    </form>
  </div>
  <!-- ***** Match Blocks ***** -->

  <!-- Information Dialog -->
  <div id="coveragediv" title="Coverage Status"
    style="font-size: 95%; overflow: auto; border: 1px solid gray; width: 500px; height: 500px" hidden></div>

  <!-- Information Dialog -->
  <div id="coverinfo" title="Coverage Relations" hidden>
    <p>
      Some symbols are used to establish a relation between a <b>Standard&rsquo;s Element</b> and an <b>Ontology&rsquo;s
        Concept</b> (or between two Elements from different Standards). It is always a binary relation comparing the <b>notions&rsquo;
        coverage</b> on the domain, i.e. <em>how the domain portion covered by an Element is related to the domain
        portion covered by a Concept (or by another Element</em>).
      <br />
      For example, <b>A [P] O</b> (A is PART OF O), means that &ldquo;<em>Element A covers a portion of the domain
        that <b>is part of</b> the portion covered by Concept O
      </em>&rdquo;.
    </p>
    <p>For the matches where an Element remains with non-covered portions (WIDER or INTERSECTION relations), a
      comment is required for explaining such portions.</p>
    <table border=1 cellpadding=6 style="width: 100%; font-size: 95%">
      <tbody style="border: 1px solid gray">
        <tr style="background-color: #F0F0F0">
          <th width="140"><b>Coverage</b></th>
          <th width="60"><b>Symbol</b></th>
          <th width="300"><b>Meaning</b></th>
          <th width="150"><b>View</b></th>
          <th width="250"><b>Example</b></th>
        </tr>
        <tr>
          <td><b>[E] EQUIVALENT</b></td>
          <td><b>A [E] O</b></td>
          <td>A is Equivalent to O.<br /> Element A covers a portion of the domain that <b>is equivalent to</b>
            the portion covered by Concept O.
          </td>
          <td style="text-align: center"><IMG src="images/Equivalent.png"></td>
          <td>(Element) Risk Plan<br /> <b>[E]</b>
          <br /> (Concept) Plan of Risks
          </td>
        </tr>
        <tr>
          <td><b>[P] PART OF</b></td>
          <td><b>A [P] O</b></td>
          <td>A is Part of O<br /> Element A covers a portion of the domain that <b>is part of</b> the portion
            covered by Concept O (O includes A).
          </td>
          <td style="text-align: center"><IMG src="images/Partof.png"></td>
          <td>(Element) Risk Plan<br /> <b>[P]</b>
          <br /> (Concept) Project Plan
          </td>
        </tr>
        <tr>
          <td><b>[W] WIDER</b></td>
          <td><b>A [W] O</b></td>
          <td>A is Wider than O.<br /> Element A covers a portion of the domain that <b>is wider than</b> the
            portion covered by Concept O (A includes O).
          </td>
          <td style="text-align: center"><IMG src="images/Wider.png"></td>
          <td>(Element) Risk Plan<br /> <b>[W]</b>
          <br /> (Concept) Mitigation Plan<br /> <br /> <b>{contingency actions not covered}</b>
          </td>
        </tr>
        <tr>
          <td><b>[I] INTERSECTION</b></td>
          <td><b>A [I] O</b></td>
          <td>A has Intersection with O.<br /> Element A covers a portion of the domain that <b>has
              intersection with</b> the portion covered by Concept O.
          </td>
          <td style="text-align: center"><IMG src="images/Intersection.png"></td>
          <td>(Element) Risk Plan<br /> <b>[I]</b> <br /> (Concept) Internal Project Plan<br /> <br /> <b>{external
              risks not covered}</b>
          </td>
        </tr>
      </tbody>
    </table>
    <p>
      An Element that is EQUIVALENT or PART OF any Concept is considered <b>fully covered</b> <img
        src="images/favicon-full.ico">.
      <br />
      An Element that is WIDER than or have INTERSECTION with any Concept is considered <b>partially covered</b> <img
        src="images/favicon-part.ico">.
    </p>
  </div>


  <!-- ##### Dialog Boxes ##### -->

  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <!--     <p><span class="ui-icon ui-icon-circle-check" style="float: left; margin: 0 7px 50px 0;"></span> -->
    <p>
    <div id="messageText"></div>
    </p>
  </div>

  <!-- Question Message -->
  <div id="dialog-question" title="Question" hidden>
    <!--     <p><span class="ui-icon ui-icon-help" style="float: left; margin: 12px 12px 20px 0;"></span> -->
    <p>
    <div id="questionText"></div>
    </p>
  </div>

  <!-- Composite Match Question Message -->
  <div id="dialog-composite" title="Is it a Composite Match?" hidden>
    <p>
    <div id="compositeText"></div>
    </p>
  </div>

  <!-- ***** Dialog Boxes ***** -->
</BODY>
</HTML>