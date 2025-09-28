package example;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.tags.shaded.org.apache.xpath.operations.Bool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class Main extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String dogUrl = null;
            String isLiked= null;
            try {
                dogUrl = getDog()[0];
                isLiked = getDog()[1];
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            boolean isliked;
            if (isLiked.equals("true")){
                isliked = true;
            }
            else{
                isliked = false;
            }

            ArrayList<String> comments;
            try {
                comments = getComments(dogUrl);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            request.setAttribute("comments", comments);
            request.setAttribute("dogUrl", dogUrl);
            request.setAttribute("isliked", isliked);
            request.getRequestDispatcher("/home.jsp").forward(request, response);

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

        //первая табличка с собакой
        String Selectprep = "SELECT * FROM dogs WHERE url = ?";
        PreparedStatement select = cn.prepareStatement(Selectprep);
        select.setString(1, url);
        ResultSet rs = select.executeQuery();

        int id;
        if (!rs.next()){
            String Insertprep = "INSERT INTO dogs(url, liked) VALUES(?, ?)";
            PreparedStatement insert = cn.prepareStatement(Insertprep,  Statement.RETURN_GENERATED_KEYS);
            insert.setString(1, url);
            insert.setBoolean(2, liked);
            insert.executeUpdate();
            ResultSet rsInsert = insert.getGeneratedKeys();
            rsInsert.next();
            id = rsInsert.getInt("id");

            rsInsert.close();
            insert.close();
            System.out.println("Задача успешно добавлена!");
        }
        else{
            String Updateprep = "UPDATE dogs SET liked = ? WHERE url = ?";
            PreparedStatement update = cn.prepareStatement(Updateprep);
            update.setBoolean(1, liked);
            update.setString(2, url);
            update.executeUpdate();
            id = rs.getInt("id");
            update.close();
            System.out.println("обновление записи");
        }


        rs.close();
        //вторая табличка с комментариями к собаке
        if (!message.equals("")) {
            String Insertprep = "INSERT INTO dog_comments(dog_id, comment) VALUES (?, ?)";
            PreparedStatement insert = cn.prepareStatement(Insertprep);

            insert.setInt(1, id);
            insert.setString(2, message);
            insert.executeUpdate();
            System.out.println("Комментарий добавлен");
            insert.close();
        }
        stmt.close();
        cn.close();
    }

    private ArrayList<String> getComments(String url) throws ClassNotFoundException, SQLException {
        ArrayList<String> comments = new ArrayList<>();
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

        String totalSelect = "SELECT dog_comments.comment " +
                "FROM dogs " +
                "JOIN dog_comments ON dog_comments.dog_id = dogs.id " +
                "WHERE dogs.url=?";
        PreparedStatement select = cn.prepareStatement(totalSelect);

        select.setString(1, url);
        select.executeQuery();
        ResultSet rs = select.executeQuery();
        while(rs.next()){
            if (!rs.getString("comment").equals("")) {
                comments.add(rs.getString("comment"));
            }
        }
        return comments;
    }


    private String[] getDog() throws ClassNotFoundException, SQLException, IOException {
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
        String selectcount = "SELECT COUNT(*) FROM dogs";
        PreparedStatement countt = cn.prepareStatement(selectcount);

        ResultSet rs = countt.executeQuery();
        int count=0;
        String dog[] = new String[2];
        if (rs.next()){
            count=rs.getInt(1);
        }

        if (count<20){
            URL url = new URL("https://dog.ceo/api/breeds/image/random");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode==200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                ObjectMapper mapper = new ObjectMapper();

                DogResponse dogResponse = mapper.readValue(builder.toString(), DogResponse.class);

                dog[0] = dogResponse.getMessage();
                dog[1]="false";
            }
        }
        else{
            String rand = "SELECT * FROM dogs ORDER BY RANDOM() LIMIT 1";
            PreparedStatement random = cn.prepareStatement(rand);
            ResultSet rs1 = random.executeQuery();
            if (rs1.next()){
                dog[0] = rs1.getString("url");
                boolean isliked = rs1.getBoolean("liked");
                if (isliked == true){
                    dog[1] = "true";
                }
                else{
                    dog[1]="false";
                }
            }
        }

        return dog;
    }
}