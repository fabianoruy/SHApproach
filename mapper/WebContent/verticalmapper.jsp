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
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/maphilight.js"></script>

<script>
  var json = JSON.parse('${json}');
  //console.log(json);

  $(document).ready(function() {
    $('#matchbutton').click(function() {
      if (checkFields()) {
        doMatch();
      }
    });
  });

  /* Calls (the servlet via ajax) for creating a Match. */
  function doMatch() {
    $.ajax({
      type : 'POST',
      url : 'VerticalMapServlet',
      data : {
        action : 'match',
        elem : $('#elementidfield').val(),
        conc : $('#conceptidfield').val(),
        cover : $('#coveringfield').val(),
        comm : $('#commentsfield').val(),
      },
      success : function(responseXml) {
        console.log(responseXml);
        var question = $(responseXml).find('questiontext').html();
        if (question != "") {
          showCompositeMatchQuestion(question, doCompositeMatch);
        }
        $('#matchingsdiv').html($(responseXml).find('matchestable').html());
        $('#messagediv').html($(responseXml).find('messagetext').html());
      }
    });
  }

  /* Calls (the servlet via ajax) for creating a Composite Match. */
  function doCompositeMatch(cover) {
    $.ajax({
      type : 'POST',
      url : 'VerticalMapServlet',
      data : {
        action : 'compositeMatch',
        elem : $('#elementidfield').val(),
        cover : cover,
      },
      success : function(responseXml) {
        console.log(responseXml);
        $('#matchingsdiv').html($(responseXml).find('matchestable').html());
        $('#messagediv').html($(responseXml).find('messagetext').html());
      }
    });
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
      var name = json.elements[e.target.id].name;
      $('#elementidfield').val(e.target.id);
      $('#elementfield').val(name);
    });
    // Fill the Concept field from the map click
    $('#OntologyMap').click(function(e) {
      var name = json.concepts[e.target.id].name;
      $('#conceptidfield').val(e.target.id);
      $('#conceptfield').val(name);
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
    if (elem == '' || conc == '') {
      showMessage("Select an element from each diagram.");
      return false;
    }
    if (comm == '' && (relc == 'WIDER' || relc == 'INTERSECTION')) {
      showMessage("WIDER and INTERSECTION matches require a comment explaining the non-covered part(s).");
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

  /* Shows a question message dialog. */
  function showCompositeMatchQuestion(text, compositeFunction) {
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

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(4) Vertical Mappings</h1>

  <h2><b>Content Vertical Mapping</b></h2>

  <h2>Map the Standards' Models to the Domain Ontologies</h2>
  <p align="justify"><b>The standards' elements shall be mapped to the domain ontologies' concepts (vertical
      mapping).</b> <br /> This tool supports the mapping by providing features for selecting the desired elements and
    concepts and establishing the allowed types of matches between then. Select a element from the left-hand side model
    (the Standard Model) and select a concept from the right-hand side model (the SEON View). Then, choose the suitable
    match type and add comments for the match (required for PARTIAL and INTERSECTION). When the matches are finished,
    list the not covered elements, which will be used in the next activity.</p>


  <!-- ##### Diagrams Blocks ##### -->
  <div>
    <div style="width: 50%; display: inline-block">
      <b>Standard</b>
    </div>
    <div style="width: 49%; display: inline-block">
      <b>Ontology</b>
    </div>
  </div>

  <div style="width: 100%; height: 100%">
    <div style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid blue">
      <IMG src="images/${standard.diagram.name}.png" width="${standard.diagram.astahDiagram.boundRect.width}"
        class="map" usemap="#Standard">
      <MAP id="StandardMap" name="Standard">
        <c:forEach var="entry" items="${stdCoords}">
          <area shape="rect" coords="${entry.value}" id="${entry.key.id}">
        </c:forEach>
      </MAP>
    </div>

    <div style="width: 49%; height: 600px; overflow: auto; display: inline-block; border: 3px solid red">
      <IMG src="images/${ontology.diagram.name}.png" width="${ontology.diagram.astahDiagram.boundRect.width}"
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
  <h3>How do the Standard's Elements cover the Ontology's Concepts?</h3>
  <div style="display: inline-block; width: 100%">
    <div style="width: 320px; display: inline-block">
      <b>Standard Element</b> <br /> <input id="elementidfield" type="hidden"> <input id="elementfield"
        type="text" value="(select an element)" title="Select an Element from the Standard model" size="40"
        readonly="readonly" />
    </div>

    <div style="width: 140px; display: inline-block">
      <b>Coverage</b> <br /> <select id="coveringfield" title="Which is the coverage of the Element on the Concept?"
        onchange="cleanOC(this)">
        <option value="EQUIVALENT">[E] EQUIVALENT</option>
        <option value="PARTIAL">[P] PART OF</option>
        <option value="WIDER">[W] WIDER</option>
        <option value="INTERSECTION">[I] INTERSECTION</option>
        <option value="NOCOVERAGE">[-] NO COVERAGE</option>
      </select>
    </div>

    <div style="width: 320px; display: inline-block">
      <b>Ontology Concept</b> <br /> <input id="conceptidfield" type="hidden"> <input id="conceptfield"
        type="text" value="(select a concept)" title="Select a Concept from the Ontology model." size="40"
        readonly="readonly" />
    </div>

    <div style="display: inline-block">
      <button id="matchbutton">Match</button>
    </div>

    <br /> <br />
    <div style="width: 600px">
      <b>Covering Comments</b> <br />
      <textarea id="commentsfield" title="Describe the non-covered portions of the Element." rows="4" cols="108"></textarea>
    </div>
  </div>

  <br />
  <div style="display: inline-block; overflow: auto; width: 100%; height: 100px">
    <strong>Message</strong>:
    <div id="messagediv" style="font-size: 90%"></div>
  </div>

  <br />
  <div style="display: inline-block; overflow: auto; border: 1px solid blue; width: 100%; height: 400px">
    <strong>Matches Established</strong>:
    <div id="matchingsdiv" style="font-size: 95%"></div>
  </div>
  <!-- ***** Match Blocks ***** -->


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

  <!-- Composite Match Question Message -->
  <div id="dialog-composite" title="Is it a Composite Match?" hidden>
    <p>
    <div id="compositeText"></div>
    </p>
  </div>

  <!-- ***** Dialog Boxes ***** -->
</BODY>
</HTML>