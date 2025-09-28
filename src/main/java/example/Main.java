package example;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

public class Main extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        URL url = new URL("https://dog.ceo/api/breeds/image/random");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode==200){
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder builder = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null){
                builder.append(line);
            }
            reader.close();
            ObjectMapper mapper = new ObjectMapper();

            DogResponse dogResponse = mapper.readValue(builder.toString(), DogResponse.class);

            String dogUrl = dogResponse.getMessage();

            request.setAttribute("dogUrl", dogUrl);
            request.getRequestDispatcher("/home.jsp").forward(request, response);

        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String likedP = request.getParameter("liked");
        String message = request.getParameter("message");
        String url = request.getParameter("dogUrl");
        boolean liked = likedP != null;
        System.out.println(liked);
        System.out.println(message);
        System.out.println(url);

        try {
            saveToDataBase(url, message, liked);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


        response.sendRedirect("/dog");
    }

    private void saveToDataBase(String url, String message, boolean liked) throws SQLException, ClassNotFoundException {
        Properties props = new Properties();
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")){
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String urlD =props.getProperty("db.url");
        String login = props.getProperty("db.login");
        String password = props.getProperty("db.password");
        Class.forName("org.postgresql.Driver");
        Connection cn = DriverManager.getConnection(urlD, login, password);
        Statement stmt = cn.createStatement();
        String Selectprep = "SELECT * FROM dogs WHERE url = ?";
        PreparedStatement select = cn.prepareStatement(Selectprep);
        select.setString(1, url);
        ResultSet rs = select.executeQuery();
        if (!rs.next()){
            String Insertprep = "INSERT INTO dogs(url, liked) VALUES(?, ?)";
            PreparedStatement insert = cn.prepareStatement(Insertprep);
            insert.setString(1, url);
            insert.setBoolean(2, liked);
            insert.executeUpdate();
            insert.close();
            System.out.println("Задача успешно добавлена!");
        }
        else if(rs.next()){
            String Updateprep = "UPDATE dogs SET liked = ? WHERE url = ?";
            PreparedStatement update = cn.prepareStatement(Updateprep);
            update.setBoolean(1, liked);
            update.setString(2, url);
            update.executeUpdate();
            update.close();
            System.out.println("обновление записи");
        }
        rs.close();
        stmt.close();
        cn.close();
    }
}