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
			performAjaxSubmit();
		});
	});

	function performAjaxSubmit() {
		var text = $('#textinput').val();
		var file = $('#fileinput')[0].files[0];

		$('#astahprocessingdiv').append("Please wait. Astah file is being processed. It can take some seconds.");

		var formdata = new FormData();
		formdata.append("text", text);
		formdata.append("file", file);
		//formdata.append("action", "Parse Astah");
		//console.log(formdata);

		var xhr = new XMLHttpRequest();
		xhr.open("POST", "AstahUploader", true);
		xhr.send(formdata);

		xhr.onload = function(e) {
			if (this.status == 200) {
				$('#astahprocessingdiv').append("<br/>" + this.responseText);
				//alert();
			}
		};

		// Importing the images
		//importAstahImages();
	}

	/* Calls the Servlet via Ajax for importing the astah images. */
	// NOT USED
	function importAstahImages() {
		$.ajax({
			type : 'POST',
			url : 'AstahUploader',
			data : {
				action : 'import',
			},
			success : function(responseXml) {
				console.log(responseXml);
				$('#astahprocessingdiv').append("<br/>" + this.responseText);
			}
		});
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
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">Astah Model Reader</h1>

  <h2><b>Content Mapping</b></h2>
  <h2>Map the Standards' Models to the Domain Ontologies</h2>
  <p align="justify"><b>The standards' elements shall be mapped to the domain ontologies' concepts (vertical
      mapping).</b><br /> This tool supports the mapping by providing features for selecting the desired elements and
    concepts and establishing the allowed types of matches between then. Select a element from the left-hand side model
    (the Standard Model) and select a concept from the right-hand side model (the SEON View). Then, choose the suitable
    match type and add comments for the match (required for PARTIAL and INTERSECTION). When the matches are finished,
    list the not covered elements, which will be used in the next activity.</p>

  <!-- ##### Reading Blocks ##### -->
  <form id="form" enctype="multipart/form-data">
    <label for="textlabel">Please, provide a title for your harmonization initiative (e.g.: "Quality
      Assurance").</label> <input id="textinput" type="text" /> <br /> <label for="filelabel">Select your .astah file</label>
    <input id="fileinput" type="file" accept=".asta" /> <br /> <input id="uploadbutton" type="button"
      value="Start Parsing"></input>
  </form>


  <div style="display: inline-block; overflow: auto; border: 1px solid blue; width: 100%; height: 400px">
    <div id="astahprocessingdiv"></div>
  </div>
  <div style="text-align: center">
    <button id="mappingbutton" onclick="location.href='verticalmapper.jsp'">Start Mapping</button>
  </div>
  <!-- ***** Reading Blocks ***** -->


  <!-- ##### Dialog Boxes ##### -->
  <!-- Simple Message -->
  <div id="dialog-message" title="Message" hidden>
    <p><span class="ui-icon ui-icon-circle-check" style="float: left; margin: 0 7px 50px 0;"></span>
    <div id="messageText"></div>
    </p>
  </div>

</BODY>
</HTML>