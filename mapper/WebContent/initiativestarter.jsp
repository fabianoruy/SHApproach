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
      <div style="width: 680px; margin: auto; border: 1px solid gray; padding: 10px">
        <table>
          <tr>
            <td width="75px"><b>Domain</b></td>
            <td><input type="text" name="domain" size="79px" value="${initiative.domain}" disabled></td>
          </tr>
          <tr>
            <td><b>Status</b></td>
            <td><input type="text" name="status" size="79px" value="${initiative.status}" disabled></td>
          </tr>
          <tr>
            <td><b>Purpose</b></td>
            <td><textarea name="purpose" rows="4" cols="80" placeholder="Initiative Purpose">${initiative.purpose}</textarea></td>
          </tr>
          <tr>
            <td><b>Scope</b></td>
            <td><textarea name="scope" rows="4" cols="80" placeholder="Initiative Harmonization Scope">${initiative.scope}</textarea></td>
          </tr>
          <tr>
            <td><b>People</b></td>
            <td><textarea name="people" rows="3" cols="80" placeholder="People involved">${initiative.people}</textarea></td>
          </tr>
          <tr>
            <td colspan="2" style="text-align:center"><button type="submit">Access Menu</button></td>
          </tr>
        </table>
      </div>
    </form>
  </div>
</BODY>
</HTML>