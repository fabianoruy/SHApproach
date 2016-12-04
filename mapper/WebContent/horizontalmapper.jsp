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
  min-height: 140px;
}

.targetbox {
  border-radius: 8px;
  border: 2px solid #6600cc;
  padding: 8px;
  min-height: 140px;
}
</style>
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/maphilight.js"></script>

<script>
  var baseJson = JSON.parse('${baseJson}');
  var targJson = JSON.parse('${targJson}');
  //console.log(json);

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
        cover : $('#coveringfield').val(),
        comm : $('#commentsfield').val(),
        force : forceBT
      // force Basetype
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
      url : 'HorizontalMappingServlet',
      data : {
        action : 'compositeMatch',
        source : elemSavedId,
        cover : cover
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

  /* Calls (the servlet via ajax) for removing a Match. */
  function removeMatch(matchId) {
    console.log(matchId);
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
      showQuestion(question, function() {
        doMatch(true);
      });
      return;
      break;
    default:
      break;
    }
    $('#matchingsdiv').html($(responseXml).find('matchestable').html());
    $('#mirrormappingdiv').html($(responseXml).find('mirrormatchestable').html());
    $('#messagediv').html($(responseXml).find('messagetext').html());
    $('#messagediv').scrollTop(1E10);
    $('#coveringfield').prop("selectedIndex", 0);
    $('#commentsfield').val("");
    $('#basecoverdiv').html($(responseXml).find('basecovertable').html());
    $('#targcoverdiv').html($(responseXml).find('targcovertable').html());
    $('#basecovernumber').text($(responseXml).find('basecovernumber').html());
    $('#targcovernumber').text($(responseXml).find('targcovernumber').html());
    $('.icon').remove();
    $('#basediv').append($(responseXml).find('baseicons').html());
    $('#targdiv').append($(responseXml).find('targicons').html());

    if ($(responseXml).find('deduceresults').html() == 'ready') {
      $('#matchbutton').prop('disabled', true);
      $('#deducebutton').prop('hidden', false);
    } else {
      $('#matchbutton').prop('disabled', false);
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
    var relc = $('#coveringfield').val();
    var comm = $('#commentsfield').val();

    // verifying
    if (source == '' || (target == '' && relc != 'NOCOVERAGE')) {
      showMessage("Select an Element from each diagram.");
      return false;
    }
    if (comm == '' && (relc == 'PARTIAL' || relc == 'WIDER' || relc == 'INTERSECTION')) {
      showMessage("PARTIAL, WIDER and INTERSECTION matches require a comment explaining the non-covered part(s).");
      return false;
    }
    return true;
  }

  function showCoverageInfo() {
    $("#coverinfo").dialog({
      width : 1000,
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
    <div style="width: 410px; float: left">
      <label> <b>Source Element</b>
      </label> <br />
      <div class="sourcebox" title="Select an Element from the base model">
        <input id="sourceidfield" type="hidden" /> <span id="sourcefield" style="font-weight: bold">(select an
          element)</span> <br /> <span id="sourcebtfield"></span> <br /> <span id="sourcedeffield" style="font-size: 90%"></span>
      </div>
    </div>

    <div style="width: 140px; float: left; margin: 0 20px 0 20px">
      <div style="display: inline-block">
        <b>Coverage (<a href=#nothing onclick="showCoverageInfo()">?</a>)
        </b> <br /> <select id="coveringfield" title="Which is the coverage of the Source Element on the Target Element?"
          onchange="cleanOC(this)">
          <option value="EQUIVALENT">[E] EQUIVALENT</option>
          <option value="PARTIAL">[P] PART OF</option>
          <option value="WIDER">[W] WIDER</option>
          <option value="INTERSECTION">[I] INTERSECTION</option>
          <!--         <option value="NOCOVERAGE">[-] NO COVERAGE</option> -->
        </select>
      </div>
      <div style="display: inline-block; width: 140px; height: 138px; position: relative">
        <button id="matchbutton"
          style="width: 80px; height: 30px; font-weight: bold; position: absolute; top: 25px; right: 30px" disabled>MATCH!</button>
        <button id="deducebutton"
          style="width: 130px; height: 60px; font-weight: bold; position: absolute; top: 80px; right: 5px" hidden>Deduce
          Matches from previous Mappings</button>
      </div>
    </div>

    <div style="width: 410px; float: left">
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
    <!--     <strong>Matches Established.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; (Coverage:&nbsp;&nbsp;&nbsp; -->
    <%--       <a href=#nothing onclick="showCoverageStatus('basecoverdiv')">${mapping.base}: <span id="basecovernumber">0</span>%</a> --%>
    <!--         &nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; -->
    <%--       <a href=#nothing onclick="showCoverageStatus('targcoverdiv')">${mapping.target}: <span id="targcovernumber">0</span>%</a>) --%>
    <!--     </strong> -->
    <div id="matchingsdiv" style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 400px; padding: 3px">
      <!-- Matches included here by ajax -->
    </div>
  </div>

  <div style="text-align: center; width: 1000px; margin: 15px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="finishbutton">SAVE and Return to Menu</button>
    </form>
  </div>
  <!-- ***** Match Blocks ***** -->

  <!-- Information Dialog -->
  <div id="basecoverdiv" title="Coverage Status" style="font-size: 95%; overflow: auto; border: 1px solid gray" hidden></div>

  <div id="targcoverdiv" title="Coverage Status" style="font-size: 95%; overflow: auto; border: 1px solid gray" hidden></div>

  <div id="mirrormappingdiv" title="${mapping.mirror}: Matches Established"
    style="font-size: 95%; overflow: auto; border: 1px solid gray; height: 440px; padding: 3px" hidden>
    <!-- Matches included here by ajax -->
  </div>

  <!-- Information Dialog -->
  <div id="coverinfo" title="Coverage Relations" hidden>
    <p>Some symbols are used to establish a relation between a <b>Standard&rsquo;s Element</b> and an <b>Ontology&rsquo;s
        Concept</b> (or between two Elements from different Standards). It is always a binary relation comparing the <b>notions&rsquo;
        coverage</b> on the domain, i.e. <em>how the domain portion covered by an Element is related to the domain
        portion covered by a Concept (or by another Element</em>). <br /> For example, <b>A [P] O</b> (A is PART OF O),
      means that &ldquo;<em>Element A covers a portion of the domain that <b>is part of</b> the portion covered by
        Concept O
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
          <td>(Element) Risk Plan<br /> <b>[E]</b> <br /> (Concept) Plan of Risks
          </td>
        </tr>
        <tr>
          <td><b>[P] PART OF</b></td>
          <td><b>A [P] O</b></td>
          <td>A is Part of O<br /> Element A covers a portion of the domain that <b>is part of</b> the portion
            covered by Concept O (O includes A).
          </td>
          <td style="text-align: center"><IMG src="images/Partof.png"></td>
          <td>(Element) Risk Plan<br /> <b>[P]</b> <br /> (Concept) Project Plan
          </td>
        </tr>
        <tr>
          <td><b>[W] WIDER</b></td>
          <td><b>A [W] O</b></td>
          <td>A is Wider than O.<br /> Element A covers a portion of the domain that <b>is wider than</b> the
            portion covered by Concept O (A includes O).
          </td>
          <td style="text-align: center"><IMG src="images/Wider.png"></td>
          <td>(Element) Risk Plan<br /> <b>[W]</b> <br /> (Concept) Mitigation Plan<br /> <br /> <b>{contingency
              actions not covered}</b>
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
    <p>A Base Element that is EQUIVALENT or PART OF any Target Element is considered <b>fully covered</b> <img
      src="images/favicon-full.ico">. <br /> A Base Element that is WIDER than or have INTERSECTION with any
      Target Element is considered <b>partially covered</b> <img src="images/favicon-part.ico">.
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