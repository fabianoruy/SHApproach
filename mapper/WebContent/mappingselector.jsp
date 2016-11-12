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

	/* Calls the Servlet via Ajax for importing the astah images. */
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
  <h1 align="center">Mapping Selection</h1>

  <h2>Select the mapping to do:</h2>
  

  <!-- ##### Reading Blocks ##### -->
  <form id="form" enctype="multipart/form-data">
    <label for="textlabel">Please, provide a title for your harmonization initiative (e.g.: "Quality Assurance").</label>
    <input id="textinput" type="text" /> <br />
    <label for="filelabel">Select your .astah file</label>
    <input id="fileinput" type="file" accept=".asta" /> <br />
    <input id="uploadbutton" type="button" value="Start Parsing"></input>
  </form>


  <div style="display: inline-block; overflow: auto; border: 1px solid blue; width: 100%; height: 400px">
    <div id="astahprocessingdiv"></div>
  </div>
  <div style="text-align: center">
    <button id="mappingbutton">Start Mapping</button>
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