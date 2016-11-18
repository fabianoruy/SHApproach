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
  <h1 align="center">(1) Initiative Identification</h1>

  <h2><b>Initiative Data</b></h2>
  <div>
    <form action="InitiativeStartServlet" method="POST">
      <input type="hidden" name="action" value="accessMenu">
      <div style="width: 60%; margin: auto; border: 1px solid gray; padding: 10px">
        <label><b>Domain:</b></label><input type="text" name="domain" value="${initiative.domain}" disabled><br/>
        <label><b>Purpose:</b></label><textarea name="purpose" rows="4" cols="80">${initiative.purpose}</textarea><br/>
        <label><b>Scope:</b></label><textarea name="purpose" rows="4" cols="80">${initiative.scope}</textarea><br/>
        <label><b>People:</b></label><input type="text" name="people" value="${initiative.people}" placeholder="People involved"><br/>
        <button type="submit">Access Menu</button>
      </div>
    </form>
  </div>
</BODY>
</HTML>