<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.ArrayList" %>
<html>
<head>
    <title>Random Dog</title>
    <!-- Подключаем Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
            padding: 30px;
        }

        h2 {
            color: #333;
        }

        img {
            border-radius: 10px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            margin-bottom: 20px;
        }

        .comments {
            list-style: none;
            padding: 0;
            margin-bottom: 20px;
        }

        .comments li {
            background-color: #fff;
            padding: 10px 15px;
            border-radius: 10px;
            margin-bottom: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .form-container {
            background-color: #fff;
            padding: 20px;
            border-radius: 15px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
            max-width: 500px;
        }

        .form-container input[type="text"] {
            width: 100%;
            padding: 10px;
            border-radius: 10px;
            border: 1px solid #ccc;
            margin-bottom: 10px;
            box-sizing: border-box;
        }

        .form-container button {
            padding: 10px 20px;
            border-radius: 10px;
            border: none;
            background-color: #4CAF50;
            color: white;
            cursor: pointer;
        }

        .form-container button:hover {
            background-color: #45a049;
        }

        .like-label {
            display: flex;
            align-items: center;
            cursor: pointer;
            font-size: 24px;
            margin-bottom: 15px;
        }

        .like-label input {
            display: none; /* скрываем стандартный чекбокс */
        }

        .heart {
            font-size: 28px;
            margin-right: 10px;
            color: #888; /* серое по умолчанию */
            transition: 0.3s;
        }

        /* Красное при лайке */
        .like-label input:checked + .heart {
            color: red;
            transform: scale(1.2);
        }
    </style>
</head>

<body>
<%
String dogUrl = (String) request.getAttribute("dogUrl");
ArrayList<String> comments = (ArrayList<String>) request.getAttribute("comments");
    Boolean liked = (Boolean) request.getAttribute("isliked"); // лайк уже стоит или нет
    if (liked == null) liked = false;
    %>

    <h2>Случайная собака</h2>
    <img src="<%= dogUrl %>" alt="Dog" width="400">

    <ul class="comments">
        <%
        for (var v : comments){
        %>
        <li><%= v %></li>
        <%
        }
        %>
    </ul>

    <div class="form-container">
        <form action="/dog" method="post">
            <label class="like-label">
                <input type="checkbox" name="liked" <%= liked ? "checked" : "" %> >
                <i class="fa-solid fa-heart heart"></i> Лайк
            </label>

            <h3>Добавить комментарий</h3>
            <input type="text" name="message" placeholder="Введите комментарий">

            <input type="hidden" name="dogUrl" value="<%= dogUrl %>">
            <button type="submit">Следующая собака</button>
        </form>
    </div>

</body>
</html>
