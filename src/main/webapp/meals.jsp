<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Моя еда</title>
    <style>
        tr:nth-of-type(even){
            background-color: #dfe2e5;
        }
    </style>
</head>
<body style="margin: auto 10%; background-color: #e9ecef; font-family: 'Century Gothic',monospace">
<h3><a href="index.html">Home</a></h3>
<hr>
<h2 style="text-align: center">Моя еда</h2>
<p>
<form method="GET" action="${pageContext.request.contextPath}/meals">
    <button type="submit" name="action" value="add" style="font-family: 'Century Gothic',monospace">Добавить</button>
</form>
<p>
<table style="width: 100%; text-align: left">
    <thead>
    <tr style="height: 40px; font-weight: bold">
        <th style="width: 20%">Дата/Время</th>
        <th style="width: 55%">Описание</th>
        <th style="width: 10%">Калории</th>
        <th style="width: 15%" colspan="2"></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${requestScope.allMeals}" var="meal">
        <tr style="color:${meal.excess ? 'red' : 'green'}; height: 40px">
            <td><c:out value="${meal.dateTime.format(requestScope.dateTimeFormatter)}"/></td>
            <td><c:out value="${meal.description}"/></td>
            <td><c:out value="${meal.calories}"/></td>
            <td><a href="meals?action=edit&id=<c:out value="${meal.id}"/>">Edit</a></td>
            <td><a href="meals?action=delete&id=<c:out value="${meal.id}"/>">Delete</a></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>