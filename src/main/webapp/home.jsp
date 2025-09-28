<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <title>Random Dog</title>
</head>



<body>
    <%
        String dogUrl = (String) request.getAttribute("dogUrl");
    %>

    <h2>Случайная собака</h2>
    <img src= "<%= dogUrl %>" alt="Dog" width="400">

    <form action="/dog" method="post">

        <h2>Лайк</h2>
        <input type="checkbox" name="liked">
        <h3>Добавить комментарий</h3>
        <input type="text" name="message" placeholder="Введите комментарий" size="50">
        <input type="hidden" name="dogUrl" value="<%=dogUrl%>">
        <button type="submit">Следующая собака</button>
    </form>

</body>
</html>