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
        <select name="user" size="5">
            <option value="Quality Assurance">Quality Assurance</option>
            <option value="Configuration Management">Configuration Management</option>
            <option value="Design">Design</option>
            <option value="Requirements Development">Requirements Development</option>
        </select><br/>
<!--         <label><b>Password:</b></label><br/> -->
<!--         <input type="password" placeholder="Enter Password" name="pword" required><br/> -->
        <button type="submit">Open</button>
      </div>
    </form>
  </div>
</BODY>
</HTML>