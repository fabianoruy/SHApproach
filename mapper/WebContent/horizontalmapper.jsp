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
.sourcebox {
  border-radius: 8px;
  border: 2px solid blue;
  padding: 8px;
  min-height: 150px;
}

.targetbox {
  border-radius: 8px;
  border: 2px solid #6600cc;
  padding: 8px;
  min-height: 150px;
}

</style>
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/maphilight.js"></script>

<script>
  var baseJson = JSON.parse('${baseJson}');
  var targJson = JSON.parse('${targJson}');
  //var coverJson;
  //var cmatchescount = 0;
  //var previouscmatchescount = -1;

  $(document).ready(function() {
    $('#matchbutton').click(function() {
      if (checkFields()) {
        doMatch(false);
      }
    });

    $('#deducebutton').click(function() {
      doDeduction('${mapping.id}');
    });

  });

  /* Calls (the servlet via ajax) for updating the page with the current Mapping. */
  function doUpdate() {
    $.ajax({
      type : 'POST',
      url : 'HorizontalMappingServlet',
      data : {
        action : 'update',
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  /* Calls (the servlet via ajax) for deducing the horizontal matches. */
  function doDeduction(mappingId) {
    $.ajax({
      type : 'POST',
      url : 'HorizontalMappingServlet',
      data : {
        action : 'deduce',
        mapping : mappingId
      },
      success : function(responseXml) {
        updateMapping(responseXml);
        showMessage($(responseXml).find('deduceresults').html());
        //$('#matchbutton').prop('disabled', false);
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a Match. */
  function doMatch(forceBT) {
    $.ajax({
      type : 'POST',
      url : 'HorizontalMappingServlet',
      data : {
        action : 'match',
        source : $('#sourceidfield').val(),
        target : $('#targetidfield').val(),
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
      url : 'HorizontalMappingServlet',
      data : {
        action : 'compositeMatch',
        source : elemSavedId,
        type : type
      },
      success : function(responseXml) {
        updateMapping(responseXml);
      }
    });
  }

  var elemSavedId = null;
  /* Calls (the servlet via ajax) for checking a Composite Match. */
  function checkComposite(mapId, elemId) {
    elemSavedId = elemId;
    $.ajax({
      type : 'POST',
      url : 'HorizontalMappingServlet',
      data : {
        action : 'checkCompositeMatch',
        mapping : mapId,
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
      url : 'HorizontalMappingServlet',
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
      url : 'HorizontalMappingServlet',
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
        function() {doMatch(true);}
      );
      return;
      break;
    default:
      break;
    }
    $('#matchingsdiv').html($(responseXml).find('matchestable').html());
    $('#mirrormappingdiv').html($(responseXml).find('mirrormatchestable').html());
    $('#messagediv').html($(responseXml).find('messagetext').html());
    $('#messagediv').scrollTop(1E10);
    $('#matchselect').prop("selectedIndex", 0);
    $('#rfull').prop('checked', true);
    $('#rfull').prop('disabled', false);
    $("#rpart").prop("disabled", true);
    $("#rlarge").prop("disabled", true);

    $('#commentsfield').val("");
    $('#basecoverdiv').html($(responseXml).find('basecovertable').html());
    $('#targcoverdiv').html($(responseXml).find('targcovertable').html());
    $('#basecovernumber').text($(responseXml).find('basecovernumber').html());
    $('#targcovernumber').text($(responseXml).find('targcovernumber').html());
    $('.icon').remove();
    $('#basediv').append($(responseXml).find('baseicons').html());
    $('#targdiv').append($(responseXml).find('targicons').html());
    //coverJson = JSON.parse($(responseXml).find('coveragelist').html());
    //cmatchescount = JSON.parse($(responseXml).find('cmatchescount').html());
    //if(previouscmatchescount == -1) previouscmatchescount = cmatchescount; 
    //console.log("conter: "+cmatchescount);

    if ($(responseXml).find('deduceresults').html() == 'ready') {
      $('#matchbutton').prop('disabled', true);
      $('#matchselect').prop('disabled', true);
      $('#coveradio').prop('disabled', true);
      $('#deducebutton').prop('hidden', false);
    } else {
      $('#matchbutton').prop('disabled', false);
      $('#matchselect').prop('disabled', false);
      $('#coveradio').prop('disabled', false);
      $('#deducebutton').prop('hidden', true);
    }
  }

  /* Highlight the diagrams' elements/concepts and make then selectable. */
  $(function() {
    $('.map').maphilight();
    //     $('.elembox').mouseover(function(e) {
    //       console.log($('#sourceidfield').val());
    //       var id = $('#sourceidfield').val();
    //       $('.EVENT').mouseover();
    //     });

    // Fills the Source Element field from the map click
    $('#BaseMap').click(function(e) {
      var name = baseJson[e.target.id].name;
      var btype = baseJson[e.target.id].basetype;
      var def = baseJson[e.target.id].definition;
      $('#sourceidfield').val(e.target.id);
      $('#sourcefield').text(name);
      $('#sourcebtfield').text("(" + btype + ")");
      $('#sourcedeffield').text(def);
    });

    // Fill the Target Element field from the map click
    $('#TargMap').click(function(e) {
      var name = targJson[e.target.id].name;
      var btype = targJson[e.target.id].basetype;
      var def = targJson[e.target.id].definition;
      $('#targetidfield').val(e.target.id);
      $('#targetfield').text(name);
      $('#targetbtfield').text("(" + btype + ")");
      $('#targetdeffield').text(def);
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
//       if(previouscmatchescount < cmatchescount) {
//         showQuestion("This mapping has some possible Composite Matches. Do you want to deal with them now?",
//           function() {showCoverageStatus();},
//           function() {$('#finishform').submit();}
//         );
//       } else {
        $('#finishform').submit();
//       }
    });

  });

  /* Cleans the Target Element when Not Covered is selected. */
  function cleanOC() {
    if ($('#coveringfield :selected').val() == 'NOCOVERAGE') {
      $('#targetidfield').val('');
      $('#targetfield').val('');
    }
  }

  /* Check if the fields are well filled. */
  function checkFields() {
    // getting values
    var source = $('#sourceidfield').val();
    var target = $('#targetidfield').val();
    var type = $('#matchselect').val();
    var cover = $('input[name=coveradio]:checked').val();
    var comm = $('#commentsfield').val();

    // verifying
    if (source == '' || (target == '' && type != 'NORELATION')) {
      showMessage("Select an Element from each diagram.");
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

  function showMirrorMapping() {
    $("#mirrormappingdiv").dialog({
      width : 1000,
      height : 455
    });
  }

  function showCoverageStatus(divId) {
    $("#" + divId).dialog({
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

  /* Shows a question message dialog for Composite Matching (EQUIVALENT). */
  function showCompositeQuestionE(text, compositeFunction) {
    $('#compositeText').empty().append(text);
    $('#dialog-composite').dialog({
      resizable : false,
      height : "auto",
      width : 700,
      modal : true,
      buttons : {
        "Yes, the Source Element is EQUIVALENT to the sun of the Target Elements." : function() {
          $(this).dialog('close');
          compositeFunction('EQUIVALENT');
        },
        "No, the Source Element remains not fully covered." : function() {
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
        "Yes, the Source Element is PART OF the sun of the Target Elements." : function() {
          $(this).dialog('close');
          compositeFunction('PARTIAL');
        },
        "No, the Source Element remains not fully covered." : function() {
          $(this).dialog('close');
        }
      }
    });
  }
</script>
</HEAD>

<BODY onload="doUpdate()">
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(6) Horizontal Mappings</h1>

  <h2><b>Content Horizontal Mapping: ${mapping}</b></h2>

  <h2>Map the Base ${mapping.base} Standard's Model to the Target ${mapping.target} Standard's Model</h2>
  <p align="justify" style="width: 98%"><b>The base standards' elements shall be mapped to the target standard's
      elements (horizontal mapping).</b> <br /> The Horizontal Mapping is supported by features for selecting the desired
    elements and establishing different types of matches between them. Select an element from the left-hand side model
    (the Base Standard Model) and another element from the right-hand side model (the Target Standard Model). Then,
    choose the suitable <a href=#nothing onclick="showCoverageInfo()">coverage relation</a> and add comments for the
    match. Try to achieve a larger standard coverage by making as many suitable matches as possible.</p>

  <!-- ##### Diagrams Blocks ##### -->
  <div>
    <div style="width: 49.5%; display: inline-block">
      <b>Standard: ${mapping.base}</b>
    </div>
    <div style="width: 49%; display: inline-block">
      <b>Standard: ${mapping.target}</b>
    </div>
  </div>

  <div style="width: 100%; height: 100%">
    <div id="basediv"
      style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid blue; position: relative">
      <IMG src="${pageContext.request.contextPath}${mapping.base.diagram.path}" width="${mapping.base.diagram.width}"
        class="map" usemap="#Base">
      <MAP id="BaseMap" name="Base">
        <c:forEach var="entry" items="${baseCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}" class="${entry.key.indirectUfotype}">
        </c:forEach>
      </MAP>
      <!-- diagram base icons included here by ajax -->
    </div>

    <div id="targdiv"
      style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid #6600cc; position: relative">
      <IMG src="${pageContext.request.contextPath}${mapping.target.diagram.path}"
        width="${mapping.target.diagram.width}" class="map" usemap="#Target">
      <MAP id="TargMap" name="Target">
        <c:forEach var="entry" items="${targCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}">
        </c:forEach>
      </MAP>
      <!-- diagram target icons included here by ajax -->
    </div>
  </div>
  <!-- ***** Diagrams Blocks ***** -->

  <!-- ##### Match Blocks ##### -->
  <h3>How do the ${mapping.base}'s Elements cover the ${mapping.target}'s Elements?</h3>
  <div style="display: inline-block; width: 1000px">
    <div style="width: 400px; float: left">
      <label> <b>Source Element</b> </label> <br />
      <div class="sourcebox" title="Select an Element from the base model">
        <input id="sourceidfield" type="hidden" /> <span id="sourcefield" style="font-weight: bold">(select an
          element)</span> <br /> <span id="sourcebtfield"></span> <br /> <span id="sourcedeffield" style="font-size: 90%"></span>
      </div>
    </div>

    <div style="width: 170px; float: left; margin: 0 20px 0 10px">
      <div style="display: inline-block">
        <b>Match Type (<a href=#nothing onclick="showMatchtypeInfo()">?</a>)
        </b> <br /> <select id="matchselect" title="Which is the match type of the Source Element on the Target Element?">
          <option value="EQUIVALENT" title="The Source is Equivalent to the Target">[E] EQUIVALENT</option>
          <option disabled>──────────</option>
          <option value="PARTIAL" title="The Source is Part of the Target">[P] PART OF</option>
          <option value="WIDER" title="The Source is Wider than the Target">[W] WIDER</option>
          <option value="OVERLAP" title="The Source Overlaps the Target">[O]  OVERLAP</option>
          <option disabled>──────────</option>
          <option value="SPECIALIZATION" title="The Source Specializes the Target">[S] SPECIALIZATION</option>
          <option value="GENERALIZATION" title="The Source Generalizes the Target">[G] GENERALIZATION</option>
          <option disabled>──────────</option>
          <option value="ACTS" title="The Source can Act as the Target">[A] ACTS AS</option>
          <option value="BYACTED" title="The Source can be Acted By the Target">[B] IS ACTED BY</option>
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
          style="width: 80px; height: 30px; font-weight: bold; position: absolute; top: 25px; right: 30px" disabled>MATCH!</button>
        <button id="deducebutton"
          style="width: 160px; height: 45px; font-weight: bold; position: absolute; top: 10px; right: 0px" hidden>Deduce
          Matches from previous Mappings</button>
      </div>
    </div>

    <div style="width: 400px; float: left">
      <label> <b>Target Element</b>
      </label> <br />
      <div class="targetbox" title="Select an Element from the target model.">
        <input id="targetidfield" type="hidden" /> <span id="targetfield" style="font-weight: bold">(select an
          element) </span> <br /> <span id="targetbtfield"></span> <br /> <span id="targetdeffield" style="font-size: 90%"></span>
      </div>
    </div>

  </div>

  <div style="width: 1000px; margin: 15px 0 0 0">
    <b>Covering Comments</b> <br />
    <textarea id="commentsfield" title="Describe the non-covered portions of the Element(s)." rows="3" cols="139"></textarea>
  </div>

  <div style="display: inline-block; width: 1000px; margin: 15px 0 0 0">
    <strong>Message</strong>
    <div id="messagediv"
      style="font-size: 90%; height: 80px; overflow: auto; border: 1px solid gray; border-radius: 8px; padding: 6px;">
      <!-- Messages included here by ajax -->
    </div>
  </div>

  <div style="display: inline-block; width: 1000px; margin: 15px 0 0 0">
    <div style="display: inline-block; width: 1000px">
      <div style="float: left; width: 350px">
        <strong>Matches Established (${mapping})</strong>
      </div>
      <div style="float: left; text-align: center; width: 340px">
        <strong>(Coverage:&nbsp;&nbsp; <a href=#nothing onclick="showCoverageStatus('basecoverdiv')">${mapping.base}:
            <span id="basecovernumber">0</span>%
        </a> &nbsp;&nbsp;|&nbsp;&nbsp; <a href=#nothing onclick="showCoverageStatus('targcoverdiv')">${mapping.target}:
            <span id="targcovernumber">0</span>%
        </a>)
        </strong>
      </div>
      <div style="float: right; text-align: right; width: 300px">
        <strong>Mirror Mapping (<a href=#nothing onclick="showMirrorMapping()">${mapping.mirror}</a>)
        </strong>
      </div>
    </div>
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
  <div id="basecoverdiv" title="Coverage Status" style="font-size: 95%; overflow: auto; border: 1px solid gray" hidden></div>

  <div id="targcoverdiv" title="Coverage Status" style="font-size: 95%; overflow: auto; border: 1px solid gray" hidden></div>

  <div id="mirrormappingdiv" title="${mapping.mirror}: Matches Established"
    style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 440px; padding: 3px" hidden>
    <!-- Matches included here by ajax -->
  </div>

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
          <td>(Element) Risk Plan<br /> <b>[E]</b> <br /> (Concept) Plan of Risks
          </td>
        </tr>
        <tr>
          <td><b>[P] PART OF</b></td>
          <td><b>A [P] O</b></td>
          <td>A is Part of O<br /> Element A represents a notion that <b>is part of</b> the notion represented by Concept O.<br/>(O includes A)</td>
          <td rowspan="3"><b>Composition Matches</b><br/>Apply to matches involving complex notions such as complex objects, events, and collective agents (e.g., Artifacts, Processes/Activities, and Teams).</td>
          <td>(Element) Risk Plan<br /> <b>[P]</b> <br /> (Concept) Project Plan
          </td>
        </tr>
        <tr>
          <td><b>[W] WIDER</b></td>
          <td><b>A [W] O</b></td>
          <td>A is Wider than O.<br /> Element A represents a notion that <b>is wider than</b> the notion represented by Concept O.<br/>(A includes O)</td>
          <td>(Element) Risk Plan<br /> <b>[W]</b> <br /> (Concept) Mitigation Plan<br /> <br /> <b>{contingency actions not covered}</b>
          </td>
        </tr>
        <tr>
          <td><b>[O] OVERLAP</b></td>
          <td><b>A [O] O</b></td>
          <td>A has Overlap with O.<br /> Element A represents a notion that <b>has overlap with</b> the notion represented by Concept O.<br/> (A and O include P)</td>
          <td>(Element) Requirements Verification and Validation<br /> <b>[O]</b> <br /> (Concept) Requirements Validation and Agreement<br /> <br /> <b>{verification not covered}</b>
          </td>
        </tr>
        <tr>
          <td><b>[S] SPECIALIZATION</b></td>
          <td><b>A [S] O</b></td>
          <td>A is a Specialization of O.<br /> Element A represents a notion that <b>specializes</b> the notion represented by Concept O.</td>
          <td rowspan="2"><b>Specialization/Generalization Matches</b><br/>Apply preferably for objects and agents (e.g., Artifacts and Stakeholders).</td>
          <td>(Element) Software Designer <br /> <b>[S]</b> <br /> (Concept) Developer
          </td>
        </tr>
        <tr>
          <td><b>[G] GENERALIZATION</b></td>
          <td><b>A [G] O</b></td>
          <td>A is a Generalization of O.<br /> Element A represents a notion that <b>generalizes</b> the notion represented by Concept O.<br/>(O specializes A)</td>
          <td>(Element) Requirement <br /> <b>[G]</b> <br /> (Concept) Functional Requirement
          </td>
        </tr>
        <tr>
          <td><b>[A] ACTS</b></td>
          <td><b>A [A] O</b></td>
          <td>A Acts as O.<br /> Element A represents a notion that can <b>act as</b> the <i>role</i> represented by Concept O.<br/></td>
          <td rowspan="2"><b>Role-related Matches</b><br/>Apply when one of the notions is a role, usually objects and agents (e.g., Artifacts and Stakeholders roles).</td>
          <td>(Element) System Analyst <br /> <b>[A]</b> <br /> (Concept) Requirements Reviewer <br/> <small><i><br/>(a System Analyst can play the role of Requirements Reviewer)</i></small>
          </td>
        </tr>
        <tr>
          <td><b>[B] IS ACTED BY</b></td>
          <td><b>A [B] O</b></td>
          <td>A is acted By O.<br /> Element A represents the notion of a <i>role</i> that can be <b>acted by</b> the notion represented by Concept O.<br/>(O acts as A)</td>
          <td>(Element) Requirements Agreement <br /> <b>[B]</b> <br /> (Concept) Client E-mail <br/> <small><i><br/>(a Client E-mail can play the role of Requirements Agreement)</i></small>
          </td>
        </tr>
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