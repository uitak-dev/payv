<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>IAM Login</title>
</head>
<body>
<h2>Login</h2>

<c:if test="${not empty error}">
    <p style="color:red;">Login failed: ${error}</p>
</c:if>
<c:if test="${not empty logout}">
    <p>Logged out successfully.</p>
</c:if>
<c:if test="${not empty success}">
    <p>Login success.</p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/perform_login">
    <label>Email: <input type="email" name="email" required/></label><br/>
    <label>Password: <input type="password" name="password" required/></label><br/>
    <button type="submit">Login</button>
</form>

<c:if test="${not empty authenticatedUserId}">
    <h3>Authenticated</h3>
    <p>User ID: <code>${authenticatedUserId}</code></p>

    <form method="post" action="${pageContext.request.contextPath}/logout">
        <button type="submit">Logout</button>
    </form>
</c:if>

</body>
</html>
