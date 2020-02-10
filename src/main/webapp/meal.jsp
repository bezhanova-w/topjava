<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title><c:out
            value="${(requestScope.meal == null || requestScope.meal.isNew()) ? \"Добавить еду\" : \"Редактировать еду\"}"/></title>
</head>
<body style="margin: auto 10%; background-color: #e9ecef; font-family: 'Century Gothic',monospace">

<h2><c:out
        value="${(requestScope.meal == null || requestScope.meal.isNew()) ? \"Добавить еду\" : \"Редактировать еду\"}"/></h2>

<form method="POST" action="${pageContext.request.contextPath}/meals">
    <p><input type="text" name="id" hidden value="<c:out value="${requestScope.meal.id}"/>"/></p>

    <p><label for="dateTime">Дата/Время</label></p>
    <p><input type="datetime-local" id="dateTime" name="dateTime" placeholder="Дата/Время"
              value="<c:out value="${requestScope.meal.dateTime}"/>"
              style="width: 400px"/></p>

    <p><label for="description">Описание</label></p>
    <p><input type="text" id="description" name="description" maxlength="120" placeholder="Описание"
              value="<c:out value="${requestScope.meal.description}"/>"
              style="width: 400px"/></p>

    <p><label for="calories">Калории</label></p>
    <p><input type="number" id="calories" name="calories" min="0" step="1" placeholder="1000"
              value="<c:out value="${requestScope.meal.calories}"/>"
              style="width: 400px"/></p>

    <p><input type="button" value="Отменить" onclick="location.href='${pageContext.request.contextPath}/meals'"
              style="width: 100px">
        <input type="submit" name="submit" value="Сохранить" style="width: 100px"></p>
</form>
</body>
</html>