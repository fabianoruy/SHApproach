<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SH Matcher</title>

<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="css/jquery-ui.css">
<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/maphilight.js"></script>

<script>
	$(document).ready(function() {
		$('#matchbutton').click(function() {
			if (checkFields()) {
				doAjax();
			}
		});
	});

	/* Calls the Servlet via Ajax for processing each match data. */
	function doAjax() {
		$.ajax({
			type : 'POST',
			url : 'MatcherServlet',
			data : {
				action : 'match',
				elem : $('#elementfield').val(),
				conc : $('#conceptfield').val(),
				cover : $('#coveringfield').val(),
				comm : $('#commentsfield').val(),
			},
			success : function(responseXml) {
				console.log(responseXml);
				$('#matchingsdiv').html($(responseXml).find('data').html());
				//$("#matchingsdiv").empty().append(responseXml);
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
		$('map').click(function(e) {
			var name = e.target.id;
			name = name.substring(name.indexOf('_') + 1).replace(/\+/g, ' ');
			if (this.name == "Standard") {
				document.getElementById("elementfield").value = name;
			} else if (this.name == "Ontology") {
				document.getElementById("conceptfield").value = name;
			}
		});
	});

	/* Check if the fields are well filled. */
	function checkFields() {
		// getting values
		var elem = $('#elementfield').val();
		var conc = $('#conceptfield').val();
		var relc = $('#coveringfield').val();
		var comm = $('#commentsfield').val();

		// verifying
		if (elem == '' || conc == '') {
			//message("Select an element from each diagram.");
			confirmationMessage("In a Standard Model, distinct elements should have different meaning. You already matched A1 [E] O.<br/><b>Are you sure that also A2 [E] O, and, hence A2 [E] A1?</b><br/><i>It will merge A1 and A2 as a single element (A1 = A2).</i>");
			return false;
		}
		if (comm == '' && (relc == 'WIDER' || relc == 'INTERSECTION')) {
			message("WIDER and INTERSECTION matchings require a comment explaining the non-covered part(s).");
			return false;
		}
		return true;
	}

	/* Shows a message dialog. */
	function message(text) {
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

	/* Shows a confimation message dialog. */
	function confirmationMessage(text) {
		$('#confirmText').empty().append(text);
		$('#dialog-confirm').dialog({
			resizable : false,
			height : "auto",
			width : 600,
			modal : true,
			buttons : {
				Yes : function() {
					$(this).dialog('close');
					// do something
				},
				No : function() {
					$(this).dialog('close');
				}
			}
		});
	}

	/* Cleans the Ontology Concept when Not Covered is selected. */
	function cleanOC() {
		if ($('#coveringfield :selected').val() == 'NOTCOVERED') {
			$('#conceptfield').val('');
		}
	}
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">Quality Assurance Process</h1>

  <h2>
    <b>Content Mapping</b>
  </h2>
  <h2>Map the Standards' Models to the Domain Ontologies</h2>
  <p align="justify"><b>The standards' elements shall be mapped to the domain ontologies' concepts (vertical
      mapping).</b><br /> This tool supports the mapping by providing features for selecting the desired elements and
    concepts and establishing the allowed types of matches between then. Select a element from the left-hand side model
    (the Standard Model) and select a concept from the right-hand side model (the SEON View). Then, choose the suitable
    match type and add comments for the match (required for PARTIAL and INTERSECTION). When the matches are finished,
    list the not covered elements, which will be used in the next activity.</p>


  <!-- ##### Diagrams Blocks ##### -->
  <div>
    <div style="width: 48.5%; display: inline-block">
      <b>Standard</b>
    </div>
    <div style="width: 0.5%; display: inline-block"></div>
    <div style="width: 48.5%; display: inline-block">
      <b>Ontology</b>
    </div>
  </div>

  <div style="width: 100%; height: 100%">
    <div style="width: 49%; height: 780px; overflow: auto; display: inline-block; border: 3px solid red">
      <IMG src="images/CMMIQAModel.png" width="1201" class="map" usemap="#Standard">
      <MAP NAME="Standard" id="Standard">
        <area shape="rect" coords="246,345,512,375" id="CMMI_Process+and+Product+Quality+Assurance+PA">
        <area shape="rect" coords="84,261,319,291" id="CMMI_Purpose+of+Providing+Objective+Insight">
        <area shape="rect" coords="417,394,728,424" id="CMMI_Objectively+Evaluate+Processes+and+Work+Products">
        <area shape="rect" coords="417,542,577,572" id="CMMI_Provide+Objective+Insight">
        <area shape="rect" coords="573,492,771,522" id="CMMI_Objectively+Evaluate+Processes">
        <area shape="rect" coords="573,444,795,474" id="CMMI_Objectively+Evaluate+Work+Products">
        <area shape="rect" coords="573,591,876,621" id="CMMI_Communicate+and+Resolve+Noncompliance+Issues">
        <area shape="rect" coords="573,641,696,671" id="CMMI_Establish+Records">
        <area shape="rect" coords="992,320,1111,350" id="CMMI_Evaluation+Report">
        <area shape="rect" coords="992,357,1141,387" id="CMMI_Noncompliance+Report">
        <area shape="rect" coords="992,469,1110,499" id="CMMI_Corrective+Action">
        <area shape="rect" coords="992,555,1151,585" id="CMMI_Corrective+Action+Report">
        <area shape="rect" coords="992,593,1094,623" id="CMMI_Quality+Trends">
        <area shape="rect" coords="992,642,1094,672" id="CMMI_Evaluation+Log">
        <area shape="rect" coords="992,677,1157,707" id="CMMI_Quality+Assurance+Report">
        <area shape="rect" coords="992,713,1191,743" id="CMMI_Corrective+Action+Status+Report">
        <area shape="rect" coords="992,749,1135,779" id="CMMI_Quality+Trends+Report">
        <area shape="rect" coords="10,346,171,376" id="CMMI_Quality+Assurance+Group">
        <area shape="rect" coords="10,500,101,530" id="CMMI_Project+Staff">
        <area shape="rect" coords="10,606,80,636" id="CMMI_Manager">
        <area shape="rect" coords="854,493,986,523" id="CMMI_Performed+Process">
        <area shape="rect" coords="992,394,1134,424" id="CMMI_Noncompliance+Issue">
        <area shape="rect" coords="992,431,1104,461" id="CMMI_Lesson+Learned">
        <area shape="rect" coords="573,734,690,764" id="CMMI_Plan+the+Process">
        <area shape="rect" coords="417,690,679,720" id="CMMI_Institutionalize+a+Managed+(PPQA)+Process">
        <area shape="rect" coords="574,778,715,808" id="CMMI_Assign+Responsibility">
        <area shape="rect" coords="573,823,831,853" id="CMMI_Identify+and+Involve+Relevant+Stakeholders">
        <area shape="rect" coords="992,796,1144,826" id="CMMI_Quality+Assurance+Plan">
      </MAP>
    </div>
    <!-- 
	<div style="width:0.1%; height:780px; overflow:hidden; display:inline-block;"></div>
	-->

    <div style="width: 49%; height: 780px; overflow: auto; display: inline-block; border: 3px solid blue">
      <IMG id="fig1" src="images/QAPO.png" width="1093" class="map" usemap="#Ontology">
      <MAP NAME="Ontology" id="Ontology">
        <area shape="rect" coords="730,193,839,240" id="SM_Artifact">
        <area shape="rect" coords="550,335,676,365" id="SPO_Composite+Artifact">
        <area shape="rect" coords="628,299,732,329" id="SPO_Simple+Artifact">
        <area shape="rect" coords="705,387,805,417" id="SwO_Software+Item">
        <area shape="rect" coords="577,387,697,417" id="SwO_Software+Product">
        <area shape="rect" coords="814,387,927,417" id="SPO_Information+Item">
        <area shape="rect" coords="935,387,989,417" id="SPO_Model">
        <area shape="rect" coords="997,387,1075,417" id="SwO_Document">
        <area shape="rect" coords="126,10,258,40" id="SPO_Performed+Process">
        <area shape="rect" coords="265,327,454,357" id="SPO_Composite+Performed+Activity">
        <area shape="rect" coords="371,287,538,317" id="SPO_Simple+Performed+Activity">
        <area shape="rect" coords="45,76,224,106" id="SPO_General+Performed+Process">
        <area shape="rect" coords="149,122,330,152" id="SPO_Specific+Performed+Process">
        <area shape="rect" coords="329,194,464,240" id="SPO_Performed+Activity">
        <area shape="rect" coords="45,210,134,240" id="SPO_Stakeholder">
        <area shape="rect" coords="152,453,327,483" id="QAPO_Quality+Assurance+Process">
        <area shape="rect" coords="275,494,451,524" id="QAPO_Quality+Assurance+Planning">
        <area shape="rect" coords="275,536,418,566" id="QAPO_Adherence+Evaluation">
        <area shape="rect" coords="275,705,427,735" id="QAPO_Noncompliance+Control">
        <area shape="rect" coords="424,577,547,607" id="QAPO_Artifact+Evaluation">
        <area shape="rect" coords="424,618,553,648" id="QAPO_Process+Evaluation">
        <area shape="rect" coords="424,660,608,690" id="QAPO_Noncompliance+Identification">
        <area shape="rect" coords="424,750,595,780" id="QAPO_Noncompliance+Resolution">
        <area shape="rect" coords="424,792,577,822" id="QAPO_Noncompliance+Closing">
        <area shape="rect" coords="31,495,145,525" id="PMO_Project+Manager">
        <area shape="rect" coords="31,536,134,566" id="QAPO_Quality+Auditor">
        <area shape="rect" coords="685,453,774,483" id="PMO_Project+Plan">
        <area shape="rect" coords="685,494,837,524" id="QAPO_Quality+Assurance+Plan">
        <area shape="rect" coords="73,618,169,648" id="PMO_Project+Team">
        <area shape="rect" coords="894,572,1014,610" id="QAPO_Evaluated+Artifact">
        <area shape="rect" coords="745,536,864,566" id="QAPO_Evaluation+Report">
        <area shape="rect" coords="724,660,883,690" id="QAPO_Noncompliance+Register">
        <area shape="rect" coords="31,705,212,735" id="QAPO_Noncompliance+Responsible">
        <area shape="rect" coords="777,750,946,780" id="PMO_Corrective+Action+Register">
        <area shape="rect" coords="894,615,1020,653" id="QAPO_Evaluated+Process">
      </MAP>
    </div>
  </div>

  <!-- ***** Diagrams Blocks ***** -->

  <!-- ##### Match Blocks ##### -->
  <h3>How is this Standard covered by the Ontologies?</h3>
  <div style="display: inline-block; border: 1px solid red; width: 100%">
    <div style="width: 320px; display: inline-block">
      <b>Standard Element</b><br /> <input id="elementfield" type="text" value="Manager"
        title="Select an Element from the Standard model" size="40" readonly="readonly" />
    </div>

    <div style="width: 140px; display: inline-block;">
      <b>Coverage</b><br /> <select id="coveringfield" title="Which is the coverage of the Element on the Concept?"
        onchange="cleanOC(this)">
        <option value="EQUIVALENT">EQUIVALENT</option>
        <option value="PARTIAL">PARTIAL</option>
        <option value="WIDER">WIDER</option>
        <option value="INTERSECTION">INTERSECTION</option>
        <option value="NOTCOVERED">NOT COVERED</option>
      </select>
    </div>

    <div style="width: 320px; display: inline-block;">
      <b>Ontology Concept</b><br /> <input id="conceptfield" type="text" value="Project Manager"
        title="Select a Concept from the Ontology model" size="40" readonly="readonly" />
    </div>

    <div style="display: inline-block">
      <button id="matchbutton">Match</button>
    </div>

    <br /> <br />
    <div style="width: 600px">
      <b>Covering Comments</b><br />
      <textarea id="commentsfield" title="Describe the non-covered portions of the Element." rows="4" cols="108">Comments about the match</textarea>
    </div>
  </div>

  <br />
  <div style="display: inline-block; overflow: auto; border: 1px solid blue; width: 100%; height: 400px">
    <strong>Maches Established</strong>:
    <div id="matchingsdiv"></div>
  </div>
  <!-- ***** Match Blocks ***** -->


  <!-- ##### Dialog Boxes ##### -->
  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <p><span class="ui-icon ui-icon-circle-check" style="float: left; margin: 0 7px 50px 0;"></span>
    <div id="messageText"></div>
    </p>
  </div>

  <!-- Confirmation Message -->
  <div id="dialog-confirm" title="Confirmation" hidden>
    <p><span class="ui-icon ui-icon-alert" style="float: left; margin: 12px 12px 20px 0;"></span>
    <div id="confirmText"></div>
    </p>
  </div>
  <!-- ***** Dialog Boxes ***** -->


</BODY>
</HTML>