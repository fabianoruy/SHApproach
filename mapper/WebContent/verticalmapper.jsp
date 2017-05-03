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
  min-height: 150px;
}

.concbox {
  border-radius: 8px;
  border: 2px solid green;
  padding: 8px;
  min-height: 150px;
}

</style>
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/maphilight.js"></script>

<script>
  var stdJson = JSON.parse('${stdJson}');
  var ontoJson = JSON.parse('${ontoJson}');
  var coverJson;
  var cmatchescount = 0;
  var previouscmatchescount = -1;

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
        type : $('#matchselect').val(),
        cover : $('input[name=coveradio]:checked').val(),
        comm : $('#commentsfield').val(),
        force : forceBT  // forces Basetype
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a Composite Match. */
  function doCompositeMatch(type) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'compositeMatch',
        elem : elemSavedId,
        type : type
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }
  
  var elemSavedId = null;
  /* Calls (the servlet via ajax) for checking a Composite Match. */
  function checkComposite(elemId) {
    elemSavedId = elemId;
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'checkCompositeMatch',
        source : elemId
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

  /* Shows a question message dialog. */
  function editComment(matchId, comment) {
    $('#commentsText').val(comment);
    $('#dialog-form').dialog({
      resizable : false,
      height : "auto",
      width : 700,
      modal : true,
      buttons : {
        Save : function() {
          $(this).dialog('close');
          changeComment(matchId, $('#commentsText').val());
        },
        Cancel : function() {
          $(this).dialog('close');
        }
      }
    });
  }

  /* Calls (the servlet via ajax) for changing a Match comment. */
  function changeComment(matchId, comment) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'changeComment',
        matchId : matchId,
        comment : comment
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }
  
  /* Saves the Mapping Analysis. */
  function saveAnalysis() {
    var txt = $('#analysisfield').val();
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'saveAnalysis',
        text : txt,
      }
    });
  }

  
  /* Calls (the servlet via ajax) for discarding an element. */
  function doDiscard(elemId) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'discardElement',
        elemId : elemId,
      },
      success : function(responseXml) {
        updateMapping(responseXml);
  	  	$('#discardbutton').hide();
      	$('#restorebutton').show();
      	$('#matchbutton').prop('disabled', true);
      	$('#elementfield').append("&nbsp<span style='color:red'><b>(Discarded)</b></span>");
      }
    });
  }
  
  /* Calls (the servlet via ajax) for restoring an element. */
  function doRestore(elemId) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMappingServlet',
      data : {
        action : 'restoreElement',
        elemId : elemId,
      },
      success : function(responseXml) {
        updateMapping(responseXml);
  	  	$('#discardbutton').show();
      	$('#restorebutton').hide();
      	$('#matchbutton').prop('disabled', false);
      	$('#elementfield').text(stdJson[elemId].name);
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
    case 'CompositePartof':
      showCompositeQuestionP(question, doCompositeMatch);
      break;
    case 'Basetype':
      showQuestion(question,
        function() {doMatch(true);},
        function() {}
      );
      return;
      break;
    default:
      break;
    }
    $('#matchingsdiv').html($(responseXml).find('matchestable').html());
    $('#messagediv').html($(responseXml).find('messagetext').html());
    $('#messagediv').scrollTop(1E10);
    $('#matchselect').prop("selectedIndex", 0);
    $('#rfull').prop('checked', true);
    $('#rfull').prop('disabled', false);
    $("#rpart").prop("disabled", true);
    $("#rlarge").prop("disabled", true);

    $('#commentsfield').val("");
    $('#coveragediv').html($(responseXml).find('coveragetable').html());
    $('#covernumber').text($(responseXml).find('coveragetext').html());
    $('.icon').remove();
    $('#standarddiv').append($(responseXml).find('coverageicons').html());
    coverJson = JSON.parse($(responseXml).find('coveragelist').html());
    cmatchescount = JSON.parse($(responseXml).find('cmatchescount').html());
    if(previouscmatchescount == -1) previouscmatchescount = cmatchescount; 
    console.log("conter: "+cmatchescount);
  }

  /* Highlight the diagrams' elements/concepts and make then selectable. */
  $(function() {
    $('#matchbutton').click(function() {
      if (checkFields()) {
        doMatch(false);
      }
    });
    
    $('#matchselect').change(function() {
      $('#rfull').prop('checked', false);
      $('#rpart').prop('checked', false);
      $('#rlarge').prop('checked', false);
      if($(this).val() == 'EQUIVALENT' || $(this).val() == 'PARTIAL') {
        $('#rfull').prop('checked', true);
        $('#rfull').prop('disabled', false);
        $("#rpart").prop("disabled", true);
        $("#rlarge").prop("disabled", true);
      } else if($(this).val() == 'WIDER' || $(this).val() == 'OVERLAP') {
        $('#rfull').prop('disabled', true);
        $("#rpart").prop("disabled", false);
        $("#rlarge").prop("disabled", false);
      } else {
        $('#rfull').prop('disabled', false);
        $("#rpart").prop("disabled", false);
        $("#rlarge").prop("disabled", false);
      }
	});
    
    $('#finishbutton').click(function(e) {
      saveAnalysis();
      if(previouscmatchescount < cmatchescount) {
        showQuestion("This mapping has some possible Composite Matches. Do you want to deal with them now?",
          function() {showCoverageStatus();},
          function() {$('#finishform').submit();}
        );
      } else {
        $('#finishform').submit();
      }
    });
    

    $('.map').maphilight();
    //     $('.elembox').mouseover(function(e) {
    //       console.log($('#elementidfield').val());
    //       var id = $('#elementidfield').val();
    //       $('.EVENT').mouseover();
    //     });

    // Fills the Element field from the map click
    $('#StandardMap').click(function(e) {
      var id = e.target.id;
      var name = stdJson[id].name;
      var btype = stdJson[id].basetype;
      var def = stdJson[id].definition;
      $('#elementidfield').val(id);
      $('#elementfield').text(name);
      $('#ebasetypefield').text("(" + btype + ")");
      $('#edefinitionfield').text(def);
      if(coverJson[id] == 'DISCARDED') {//check if the element is already discarded
	  	$('#discardbutton').hide();
      	$('#restorebutton').show();
      	$('#matchbutton').prop('disabled', true);
      	$('#elementfield').append("&nbsp<span style='color:red'><b>(Discarded)</b></span>");
      } else {
  	  	$('#discardbutton').show();
      	$('#restorebutton').hide();
      	$('#matchbutton').prop('disabled', false);
      }
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
    
    // Clears the concept box when "NORELATION" is selected
    $('#matchselect').change(function() {
      if ($(this).val() == 'NORELATION') {
        $('#conceptidfield').val('');
        $('#conceptfield').text('');
        $('#cbasetypefield').text('');
        $('#cdefinitionfield').text('');
      }
    });
  });
  
  /* Discards the selected element from the initiative. */
  function discardElement() {
    var id = $('#elementidfield').val();
    if(coverJson[id] == 'FULLY' || coverJson[id] == 'PARTIALLY') {
      showMessage("This element already has matches. Remove them if you want to discard it.");
      return false;
    }
    showQuestion("Are you sure to discard <b>" + stdJson[id].name + "</b> from the initiative? It will be disregarded from all mappings.",
      function() {doDiscard(id);},
      function() {}
    );
  }

  /* Restores the selected element to the initiative. */
  function restoreElement() {
    var id = $('#elementidfield').val();
    showQuestion("Do you want to bring <b>" + stdJson[id].name + "</b> back to the initiative?",
      function() {doRestore(id);},
      function() {}
    );
  }

  /* Check if the fields are well filled. */
  function checkFields() {
    // getting values
    var elem = $('#elementidfield').val();
    var conc = $('#conceptidfield').val();
    var type = $('#matchselect').val();
    var comm = $('#commentsfield').val();
    var cover = $('input[name=coveradio]:checked').val();

    // verifying
    if (elem == '' || (conc == '' && type != 'NORELATION')) {
      showMessage("Select an element from each diagram.");
      return false;
    }
    if (cover != 'FULL' && comm == '') {
      showMessage("Matches <b>not FULLY</b> covered require a comment explaining the uncovered part(s).");
      return false;
    }
    return true;
  }

  function showMatchtypeInfo() {
    $("#matchinfo").dialog({
      width : 1000
    });
  }
  
  function showCoverageInfo() {
    $("#coverinfo").dialog({
      width : 600
    });
  }


  function showCoverageStatus() {
    $("#coveragediv").dialog({
      width : 700,
      height : 720
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
  function showQuestion(text, yesFunction, noFunction) {
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
          noFunction();
        }
      }
    });
  }

  /* Shows a question message dialog for Composite Matching (EQUIVALENT). */
  function showCompositeQuestionE(text, compositeFunction) {
    $('#compositeText').empty().append(text);
    $('#dialog-composite').dialog({
      resizable : false,
      height : "auto",
      width : 700,
      modal : true,
      buttons : {
        "Yes, the Element is EQUIVALENT to the sun of the Concepts." : function() {
          $(this).dialog('close');
          compositeFunction('EQUIVALENT');
        },
        "No, the Element remains not fully covered." : function() {
          $(this).dialog('close');
        }
      }
    });
  }

  /* Shows a question message dialog for Composite Matching (PART OF). */
  function showCompositeQuestionP(text, compositeFunction) {
    $('#compositeText').empty().append(text);
    $('#dialog-composite').dialog({
      resizable : false,
      height : "auto",
      width : 700,
      modal : true,
      buttons : {
        "Yes, the Element is PART OF the sun of the Concepts." : function() {
          $(this).dialog('close');
          compositeFunction('PARTIAL');
        },
        "No, the Element remains not fully covered." : function() {
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

  <h2><b>Content Vertical Mapping</b></h2>

  <h2>Map the Standards' Models to the Domain Ontologies</h2>
  <p align="justify" style="width: 98%"><b>The standards' elements shall be mapped to the domain ontologies'
      concepts (vertical mapping).</b> <br />
      This tool supports the mapping by providing features for selecting the desired elements and concepts and
      establishing different types of matches between them. Select an <b>Element</b> from the left-hand side model
      (the ${standard.name}'s Model) and select a <b>Concept</b> from the right-hand side model (the SEON View).
      Then, choose the proper <a href=#nothing onclick="showMatchtypeInfo()">Match Type</a> and
      <a href=#nothing onclick="showCoverageInfo()">Element Coverage</a>, and add comments for the match.
      Try to achieve a larger standard coverage by making as many suitable matches as possible.<br />
      At the end, check the <a href=#nothing onclick="showCoverageStatus()">coverage status</a> defining the composite
      matches, and describe an analysis for this mapping.
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
      <IMG src="${pageContext.request.contextPath}${standard.diagram.path}" width="${standard.diagram.width}"
        class="map" usemap="#Standard">
      <MAP id="StandardMap" name="Standard">
        <c:forEach var="entry" items="${stdCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}" class="${entry.key.indirectUfotype}">
        </c:forEach>
      </MAP>
      <!-- diagram icons included here by ajax -->
    </div>

    <div style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid green">
      <IMG src="${pageContext.request.contextPath}${ontology.diagram.path}" width="${ontology.diagram.width}"
        class="map" usemap="#Ontology">
      <MAP id="OntologyMap" name="Ontology">
        <c:forEach var="entry" items="${ontoCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}">
        </c:forEach>
      </MAP>
    </div>
  </div>
  <!-- ***** Diagrams Blocks ***** -->

  <!-- ##### Match Blocks ##### -->
  <h3>How do the Standard's Elements match to the Ontology's Concepts?</h3>
  <div style="display: inline-block; width: 1000px">
    <div style="width: 400px; float: left">
      <label> <b>Standard Element</b>&nbsp;&nbsp; </label>
      <img id="discardbutton" src="images/favicon-discarded.ico" title="Discard element (remove it from the initiative scope)" width="16px" style="cursor:pointer" onclick="discardElement()" hidden/>
      <img id="restorebutton" src="images/favicon-restore.ico" title="Recover element (bring it back to the initiative scope)" width="16px" style="cursor:pointer" onclick="restoreElement()" hidden/>
      <br />
      <div class="elembox" title="Select an Element from the Standard model">
        <input id="elementidfield" type="hidden" /> <span id="elementfield" style="font-weight: bold">(select an
          element)</span> <br /> <span id="ebasetypefield"></span> <br /> <span id="edefinitionfield" style="font-size: 90%"></span>
      </div>
    </div>

    <div style="width: 170px; float: left; margin: 0 20px 0 10px">
      <div style="display: inline-block">
        <b>Match Type (<a href=#nothing onclick="showMatchtypeInfo()">?</a>)
        </b> <br /> <select id="matchselect" title="Which is the match type of the Element on the Concept?">
          <option value="EQUIVALENT" title="The Element is Equivalent to the Concept">[E] EQUIVALENT</option>
          <option disabled>──────────</option>
          <option value="PARTIAL" title="The Element is Part of the Concept">[P] PART OF</option>
          <option value="WIDER" title="The Element is Wider than the Concept">[W] WIDER</option>
          <option value="OVERLAP" title="The Element Overlaps the Concept">[O]  OVERLAP</option>
          <option disabled>──────────</option>
          <option value="SPECIALIZATION" title="The Element Specializes the Concept">[S] SPECIALIZATION</option>
          <option value="GENERALIZATION" title="The Element Generalizes the Concept">[G] GENERALIZATION</option>
          <option disabled>──────────</option>
          <option value="ACTS" title="The Element can Act as the Concept">[A] ACTS AS</option>
          <option value="BYACTED" title="The Element can be Acted By the Concept">[B] IS ACTED BY</option>
          <!-- <option disabled>──────────</option> -->
          <!-- <option value="NORELATION">[-]  NO RELATION</option> -->
        </select>
        <br/>
        <br/>
        <form id="coveradio" action="">
          <b>Coverage (<a href=#nothing onclick="showCoverageInfo()">?</a>)</b><br/>
          <input id="rpart" type="radio" name="coveradio" value="PARTIAL" disabled title="The concept covers less than 50% of the element."> Partially<br/>
          <input id="rlarge" type="radio" name="coveradio" value="LARGE" disabled title="The concept covers more than 50% of the element."> Largely<br/>
          <input id="rfull" type="radio" name="coveradio" value="FULL" checked title="The concept totally covers the element."> Fully<br/>
        </form>
      </div>
      
      <div style="display: inline-block; width: 170px; height: 55px; position: relative">
        <button id="matchbutton"
          style="width: 80px; height: 30px; font-weight: bold; position: absolute; bottom: 0px; left: 50px;">MATCH!</button>
      </div>
    </div>

    <div style="width: 400px; float: left">
      <label> <b>Ontology Concept</b>
      </label> <br />
      <div class="concbox" title="Select a Concept from the Ontology model.">
        <input id="conceptidfield" type="hidden" /> <span id="conceptfield" style="font-weight: bold">(select a
          concept)</span> <br /> <span id="cbasetypefield"></span> <br /> <span id="cdefinitionfield" style="font-size: 90%"></span>
      </div>
    </div>

  </div>

  <div style="width: 1000px; margin: 15px 0 0 0">
    <b>Covering Comments</b> <br />
    <textarea id="commentsfield" title="Describe the non-covered portions of the Element." rows="3" cols="139"></textarea>
  </div>

  <div style="display: inline-block; width: 1000px; margin: 15px 0 0 0">
    <strong>Message</strong>
    <div id="messagediv"
      style="font-size: 90%; height: 80px; overflow: auto; border: 1px solid gray; border-radius: 8px; padding: 6px;">
      <!-- Messages included here by ajax -->
    </div>
  </div>

  <div style="display: inline-block; width: 1000px; margin: 15px 0 0 0">
    <strong>Matches Established. (<a href=#nothing onclick="showCoverageStatus()">Coverage: <span
        id="covernumber">0%</span></a>)
    </strong>
    <div id="matchingsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 400px; padding: 3px">
      <!-- Matches included here by ajax -->
    </div>
  </div>
  
  <div style="width: 1000px; margin: 15px 0 0 0">
    <b>Mapping Analysis</b> <br />
    <textarea id="analysisfield" title="Describe the analysis about this mapping, e.g., which are the main parts/types not covered." rows="5" cols="139">${analysis}</textarea>
  </div>
  

  <div style="text-align: center; width: 1000px; margin: 15px 0 0 0">
      <button id="finishbutton">SAVE and Return to Menu</button>
  </div>
  
  <form action="PhaseSelectServlet" method="POST" id="finishform">
    <input type="hidden" name="action" value="openSelection">
  </form>
      
        
  <!-- ***** Match Blocks ***** -->

  <!-- Information Dialog -->
  <div id="coveragediv" title="Coverage Status"
    style="font-size: 95%; overflow: auto; border: 1px solid gray; width: 500px; height: 500px" hidden></div>

  <!-- Information Dialog -->
  <div id="matchinfo" title="Types of Match" hidden>
    <p>Some Match Types are used to establish a relation between a <b>Standard&rsquo;s Element</b> and an <b>Ontology&rsquo;s
        Concept</b> (or between two Elements from different Standards). It is a binary relation comparing the <b>notions</b>. <br />
        For example, <b>A [P] O</b> (A is PART OF O), means that &ldquo;<em>Element A represents a notion that <b>is part of</b> the notion
        represented by Concept O</em>&rdquo;.
    </p>
    <table border=1 cellpadding=6 style="width: 100%; font-size: 95%">
      <tbody style="border: 1px solid gray">
        <tr style="background-color: #F0F0F0">
          <th width="120"><b>Type of Match</b></th>
          <th width="60"><b>Symbol</b></th>
          <th width="240"><b>Meaning</b></th>
          <th width="120"><b>Application</b></th>
          <!-- <th width="150"><b>Representation</b></th> -->
          <th width="250"><b>Example</b></th>
        </tr>
        <tr>
          <td><b>[E] EQUIVALENT</b></td>
          <td><b>A [E] O</b></td>
          <td>A is Equivalent to O.<br /> Element A represents a notion that <b>is equivalent to</b> the notion represented by Concept O.</td>
          <td><b>Equality Match</b><br/>Apply to any type of notion.</td>
          <!-- <td style="text-align: center"><IMG src="images/Equivalent.png"><br/><IMG src="images/Equivalent2.png"></td> -->
          <td>(Element) Risk Plan<br /> <b>[E]</b> <br /> (Concept) Plan of Risks
          </td>
        </tr>
        <tr>
          <td><b>[P] PART OF</b></td>
          <td><b>A [P] O</b></td>
          <td>A is Part of O<br /> Element A represents a notion that <b>is part of</b> the notion represented by Concept O.<br/>(O includes A)</td>
          <!-- <td style="text-align: center"><IMG src="images/Partof.png"><br/><IMG src="images/Partof2.png"></td> -->
          <td rowspan="3"><b>Composition Matches</b><br/>Apply to matches involving complex notions such as complex objects, events, and collective agents (e.g., Artifacts, Processes/Activities, and Teams).</td>
          <td>(Element) Risk Plan<br /> <b>[P]</b> <br /> (Concept) Project Plan
          </td>
        </tr>
        <tr>
          <td><b>[W] WIDER</b></td>
          <td><b>A [W] O</b></td>
          <td>A is Wider than O.<br /> Element A represents a notion that <b>is wider than</b> the notion represented by Concept O.<br/>(A includes O)</td>
          <!-- <td style="text-align: center"><IMG src="images/Wider.png"><br/><IMG src="images/Wider2.png"></td> -->
          <td>(Element) Risk Plan<br /> <b>[W]</b> <br /> (Concept) Mitigation Plan<br /> <br /> <b>{contingency actions not covered}</b>
          </td>
        </tr>
        <tr>
          <td><b>[O] OVERLAP</b></td>
          <td><b>A [O] O</b></td>
          <td>A has OVERLAP with O.<br /> Element A represents a notion that <b>has overlap with</b> the notion represented by Concept O.<br/> (A and O include P)</td>
          <!-- <td style="text-align: center"><IMG src="images/Intersection.png"><br/><IMG src="images/Intersection2.png"></td> -->
          <td>(Element) Requirements Verification and Validation<br /> <b>[O]</b> <br /> (Concept) Requirements Validation and Agreement<br /> <br /> <b>{verification not covered}</b>
          </td>
        </tr>
        <tr>
          <td><b>[S] SPECIALIZATION</b></td>
          <td><b>A [S] O</b></td>
          <td>A is a Specialization of O.<br /> Element A represents a notion that <b>specializes</b> the notion represented by Concept O.</td>
          <!-- <td style="text-align: center"><IMG src="images/Specialization.png"></td> -->
          <td rowspan="2"><b>Specialization/Generalization Matches</b><br/>Apply preferably for objects and agents (e.g., Artifacts and Stakeholders).</td>
          <td>(Element) Software Designer <br /> <b>[S]</b> <br /> (Concept) Developer
          </td>
        </tr>
        <tr>
          <td><b>[G] GENERALIZATION</b></td>
          <td><b>A [G] O</b></td>
          <td>A is a Generalization of O.<br /> Element A represents a notion that <b>generalizes</b> the notion represented by Concept O.<br/>(O specializes A)</td>
          <!-- <td style="text-align: center"><IMG src="images/Generalization.png"></td> -->
          <td>(Element) Requirement <br /> <b>[G]</b> <br /> (Concept) Functional Requirement
          </td>
        </tr>
        <tr>
          <td><b>[A] ACTS</b></td>
          <td><b>A [A] O</b></td>
          <td>A Acts as O.<br /> Element A represents a notion that can <b>act as</b> the <i>role</i> represented by Concept O.<br/></td>
          <!-- <td style="text-align: center"><IMG src="images/Acts.png"></td> -->
          <td rowspan="2"><b>Role-related Matches</b><br/>Apply when one of the notions is a role, usually objects and agents (e.g., Artifacts and Stakeholders roles).</td>
          <td>(Element) System Analyst <br /> <b>[A]</b> <br /> (Concept) Requirements Reviewer <br/> <small><i><br/>(a System Analyst can play the role of Requirements Reviewer)</i></small>
          </td>
        </tr>
        <tr>
          <td><b>[B] IS ACTED BY</b></td>
          <td><b>A [B] O</b></td>
          <td>A is acted By O.<br /> Element A represents the notion of a <i>role</i> that can be <b>acted by</b> the notion represented by Concept O.<br/>(O acts as A)</td>
          <!-- <td style="text-align: center"><IMG src="images/ByActed.png"></td> -->
          <td>(Element) Requirements Agreement <br /> <b>[B]</b> <br /> (Concept) Client E-mail <br/> <small><i><br/>(a Client E-mail can play the role of Requirements Agreement)</i></small>
          </td>
        </tr>
<!--         <tr>
          <td><b>[-] </b></td>
          <td><b>A [-]</b></td>
          <td>A has no relation.<br /> Element A represents a notion that <b>has no corresponding relation</b> with any notion in the target model.<br/></td>
          <td style="text-align: center">-</td>
          <td>(Element) Sequence Diagram <br /> <b>[-]</b> <br /> <small><i><br/>(there is no corresponding concept in the ontology)</i></small>
          </td>
        </tr>
 -->        
      </tbody>
    </table>
    <p>Elements with no matches are considered <b>non covered</b>.
    <br />Elements with Equivalent or Part of relations are considered <b>fully covered</b> <img width="16" src="images/favicon-full.ico">
    <br />Element with other matches are considered <b>partially covered</b> <img width="16" src="images/favicon-part.ico">
    <br />Elements said out of scope are considered <b>discarded</b> <img width="16" src="images/favicon-discarded.ico">
    </p>
  </div>

  <div id="coverinfo" title="Elements Coverage" hidden>
    <p>In a match, the Ontology Concept covers the Standard Element in some extension. Please, inform the
      approximate coverage:<br />
    <ul>
      <li><b>Partially</b>: The concept covers less than 50% of the element.</li>
      <li><b>Largely</b>: The concept covers more than 50% of the element.</li>
      <li><b>Fully</b>: The concept totally covers the element.</li>
    </ul>
    This information will be used in the mapping covering numbers. 
    </p>
    </div>


    <!-- ##### Dialog Boxes ##### -->

  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <div id="messageText"></div>
  </div>

  <!-- Comment Editing -->
  <div id="dialog-form" title="Inform the new Match Comment." hidden>
    <form>
      <div style="width: 500px; margin: 15px 0 0 0">
        <b>Covering Comments</b> <br />
        <textarea id="commentsText" rows="4" cols="80"></textarea>
      </div>
      <input type="submit" tabindex="-1" style="position: absolute; top: -1000px">
    </form>
  </div>

  <!-- Question Message -->
  <div id="dialog-question" title="Question" hidden>
    <div id="questionText"></div>
  </div>

  <!-- Composite Match Question Message -->
  <div id="dialog-composite" title="Is it a Composite Match?" hidden>
    <div id="compositeText"></div>
  </div>

  <!-- ***** Dialog Boxes ***** -->
</BODY>
</HTML>