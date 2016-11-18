<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<script src="js/jquery.form.js"></script>

<script>
  $(document).ready(
      function() {
        $('#uploadbutton').click(
            function() {
              $.ajax({
                type : 'POST',
                url : 'AstahParseServlet',
                data : new FormData($('#upform')[0]),
                xhr : function() { // Custom XMLHttpRequest
                  return $.ajaxSettings.xhr();
                },
                //Ajax events
                beforeSend : function() {
                  if ($('#fileinput').val() == "") {
                    $('#astahparsingdiv').append("Please, select an Astah file.<br/>");
                    return false;
                  } else {
                    $('#astahparsingdiv').empty().append(
                        "Please wait. Astah file is being processed. It can take some seconds.<br/>");
                  }
                  $('#menubutton').prop('disabled', true);
                },
                success : function(servletResponse) {
                  $('#astahparsingdiv').append(servletResponse);
                  importImages();
                },
                error : {},
                //telling jQuery not to process data or worry about content-type.
                cache : false,
                contentType : false,
                processData : false
              });
            });
      });

  /* Imports the diagrams' images from the astha file. */
  function importImages() {
    $.ajax({
      type : 'POST',
      url : 'AstahParseServlet',
      data : {
        action : 'images',
      },
      beforeSend : function() {
        $('#astahparsingdiv').append("Importing images... ");
      },
      success : function(servletResponse) {
        $('#astahparsingdiv').append(servletResponse);
        $('#menubutton').prop('disabled', false);
        //$('#logfile').prop('hidden', false);
      }
    });
  }
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">(2) Model Reading</h1>

  <h2><b>Astah Model Reader</b></h2>
  <div style="width: 100%; display: inline-block">
    <div style="display: inline-block; width: 60%">
      <p align="justify">In this step, <b>the Astah file will be read and parsed</b>, gathering the models and
        information to support the mapping activities.
      </p> The submitted file must follow the structure presented in the figure. <br /> Regardless of other packages, this
      tool will read the packages named <i>Initiative</i> and <i>Standards Structural Models</i>.
      <ul>
        <li><i>Initiative</i> package: must have a single subpackage for your initiative (e.g. <i>${initiative.domain}</i>)
          with 3 subpackages:
          <ul>
            <li><i>1.SEON View</i>: with a single diagram representing this view, and the selected portion of SEON
              (in subpackages).</li>
            <li><i>2.Structure</i>: with a diagram for each Standard and one for the Integrated Structural Model
              (ISM). The ISM additional elements stay here.</li>
            <li><i>3.Content</i>: with a package for selected Standard (each one containing the Standard elements
              and a single diagram); and the resulting diagram Integrated Content Model (ICM) with the added new
              elements.</li>
          </ul></li>
        <li><i>Standards Structural Models</i> package: must have a subpackage for each Standard, each one
          containing a single diagram and the related concepts.</li>
      </ul>
      Always try to avoid not used classes and relations.<br/>
      The definitions set for each element/class will be shown during the mappings to support the comparisons.        
    </div>
    <div style="display: inline-block; text-align: right; float: right">
      <IMG src="images/AstahStructure.png" />
    </div>
  </div>
  <br />

  <!-- ##### Reading Blocks ##### -->
  <form id="upform" enctype="multipart/form-data">
    <label for="filelabel">Select the .astah file for your harmonization initiative.</label> <input id="fileinput"
      type="file" name="file" accept=".asta" /> <br /> <input id="uploadbutton" type="button" value="Start Parsing" />
  </form>

  <div style="display: inline-block; overflow: auto; border: 1px solid blue; width: 100%; height: 580px">
    <div id="astahparsingdiv" style="font-size: 90%"></div>
  </div>

  <a id="logfile" href="${logfile}" target="_blank"><code>log file</code></a>

  <div style="text-align: center; margin: 10px 0 0 0">
    <form action="PhaseSelectServlet" method="POST">
      <input type="hidden" name="action" value="openSelection">
      <button id="menubutton">Back to Menu</button>
    </form>
  </div>
  <!-- ***** Reading Blocks ***** -->

</BODY>
</HTML>