<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SH Approach</title>

<link rel="stylesheet" href="css/style.css">
<script src="js/jquery.min.js"></script>

<script>
</script>
</HEAD>

<BODY>
  <h3 align="center">Approach for Harmonizing SE Standards</h3>
  <h1 align="center">Initiative Identification</h1>

  <h2><b>Initiative Access</b></h2>
  <div>
    <form action="InitiativeStartServlet" method="POST">
      <input type="hidden" name="action" value="login">
      <div style="width: 60%; margin: auto; border: 1px solid gray; padding: 10px">
        <b>Select an Initiative:</b><br/>
        <select name="user" size="5" style="width:250px" required>
            <option value="Quality Assurance">Quality Assurance (test)</option>
            <option value="Software Measurement">Software Measurement</option>
            <option value="Dynamic Testing">Dynamic Testing</option>
            <option value="Configuration Management">Configuration Management</option>
            <option value="Requirements Development">Requirements Development</option>
            <option value="Software Design">Software Design</option>
        </select><br/>
        <label><b>Password:</b></label><br/>
        <input type="password" placeholder="Enter Password" name="pword" required style="width:250px"><br/>
        <button type="submit">Open</button>
        <label style="color:red">${message}</label>
      </div>
    </form>
  </div>

  <div>
    <br/>
    <code>
      <p>
        <b>In this version, you can:</b><br />
        . Parse your astah file<br />
        . Import and process Structural Mappings<br/>
        . Do Vertical Mappings (simple and composite matches, disjoint and basetype validations)<br/>
        + Use eight distinct Match Types<br/>
        . Do ICM Mappings<br />
        + Justify decisions on uncovered elements <br/>
        . Monitor mappings' coverage<br/>
        . Save progress<br />
        . See initiative results<br />
      </p>
      <p>
        <b>In this version, you can't:</b><br />
        . Do Horizontal Mappings<br />
        . Deduce matches in Horizontal Mappings <br/>
      </p>
      <br/>
      <br/>
      <p><b> Version 0.7 - 2016-05-02</b></p>
    </code>
  </div>
</BODY>
</HTML>