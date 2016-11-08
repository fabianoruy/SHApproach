<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SH Matcher - Astah Reader</title>

<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="css/jquery-ui.css">

<script src="js/jquery.min.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/jquery.form.js"></script>

<script>
	$(document).ready(function() {
		$('#uploadbutton').click(function() {
			$.ajax({
				url : 'AstahUploader', //Server script to process data
				type : 'POST',
				data : new FormData($('#upform')[0]),
				xhr : function() { // Custom XMLHttpRequest
					return $.ajaxSettings.xhr();
				},
				//Ajax events
				beforeSend : function() {
					$('#astahparsingdiv').append("Please wait. Astah file is being processed. It can take some seconds.<br/>");
				},
				success : function(servletResponse) {
					$('#astahparsingdiv').append(servletResponse);
				},
				error : {},
				//telling jQuery not to process data or worry about content-type.
				cache : false,
				contentType : false,
				processData : false
			});
		});
	});

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
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">Astah Model Reader</h1>

  <h2>
    <b>Content Mapping</b>
  </h2>
  <h2>Map the Standards' Models to the Domain Ontologies</h2>
  <p align="justify">
    <b>The standards' elements shall be mapped to the domain ontologies' concepts (vertical mapping).</b>
    <br />
    This tool supports the mapping by providing features for selecting the desired elements and concepts and
    establishing the allowed types of matches between then. Select a element from the left-hand side model (the Standard
    Model) and select a concept from the right-hand side model (the SEON View). Then, choose the suitable match type and
    add comments for the match (required for PARTIAL and INTERSECTION). When the matches are finished, list the not
    covered elements, which will be used in the next activity.
  </p>

  <!-- ##### Reading Blocks ##### -->
  <!-- <label for="textlabel">Please, provide a title for your harmonization initiative (e.g.: "Quality Assurance").</label> -->

  <form id="upform" enctype="multipart/form-data">
    <label for="filelabel">Select the .astah file for your harmonization initiative.</label>
    <input id="fileinput" type="file" name="file" accept=".asta" />
    <br />
    <input id="uploadbutton" type="button" value="Start Parsing" />
  </form>


  <div style="display: inline-block; overflow: auto; border: 1px solid blue; width: 100%; height: 500px">
    <div id="astahparsingdiv"></div>
  </div>
  <div style="text-align: center">
    <button id="mappingbutton" onclick="location.href='verticalmapper.jsp'">Start Mapping</button>
  </div>
  <!-- ***** Reading Blocks ***** -->


  <!-- ##### Dialog Boxes ##### -->
  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <p>
      <span class="ui-icon ui-icon-circle-check" style="float: left; margin: 0 7px 50px 0;"></span>
    <div id="messageText"></div>
    </p>
  </div>
  <!-- ***** Dialog Boxes ***** -->

</BODY>
</HTML>