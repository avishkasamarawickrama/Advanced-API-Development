import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "ItemServlet", urlPatterns = {"/pages/item"})
public class ItemServlet extends HttpServlet {
    private static final String URL = "jdbc:mysql://localhost:3306/shop";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ijse@123";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            if (req.getParameter("option") != null) {
                PreparedStatement pstm = connection.prepareStatement(
                        "SELECT id FROM item ORDER BY CAST(id AS SIGNED) DESC LIMIT 1"
                );
                ResultSet rst = pstm.executeQuery();

                int nextId = 1;
                if (rst.next()) {
                    nextId = rst.getInt("id") + 1;
                }

                JsonObjectBuilder response = Json.createObjectBuilder()
                        .add("id", nextId);

                resp.setContentType("application/json");
                resp.getWriter().write(response.build().toString());
                return;
            }

            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM item");
            ResultSet rst = pstm.executeQuery();

            JsonArrayBuilder items = Json.createArrayBuilder();
            while (rst.next()) {
                JsonObjectBuilder item = Json.createObjectBuilder()
                        .add("id", rst.getInt("id"))
                        .add("name", rst.getString("name"))
                        .add("price", rst.getDouble("price"))
                        .add("stock", rst.getInt("stock"))
                        .add("category", rst.getString("category"));
                items.add(item);
            }

            resp.setContentType("application/json");
            resp.getWriter().write(items.build().toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObjectBuilder error = Json.createObjectBuilder()
                    .add("message", "Error: " + e.getMessage());
            resp.getWriter().write(error.build().toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement pstm = connection.prepareStatement(
                    "INSERT INTO item (id, name, price, stock, category) VALUES (?,?,?,?,?)"
            );

            pstm.setInt(1, Integer.parseInt(req.getParameter("id")));
            pstm.setString(2, req.getParameter("name"));
            pstm.setDouble(3, Double.parseDouble(req.getParameter("price")));
            pstm.setInt(4, Integer.parseInt(req.getParameter("stock")));
            pstm.setString(5, req.getParameter("category"));

            if (pstm.executeUpdate() > 0) {
                resp.getWriter().write("Item Added Successfully");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement pstm = connection.prepareStatement(
                    "UPDATE item SET name=?, price=?, stock=?, category=? WHERE id=?"
            );

            pstm.setString(1, req.getParameter("name"));
            pstm.setDouble(2, Double.parseDouble(req.getParameter("price")));
            pstm.setInt(3, Integer.parseInt(req.getParameter("stock")));
            pstm.setString(4, req.getParameter("category"));
            pstm.setInt(5, Integer.parseInt(req.getParameter("id")));

            if (pstm.executeUpdate() > 0) {
                resp.getWriter().write("Item Updated Successfully");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement pstm = connection.prepareStatement(
                    "DELETE FROM item WHERE id=?"
            );

            pstm.setInt(1, Integer.parseInt(req.getParameter("id")));

            if (pstm.executeUpdate() > 0) {
                resp.getWriter().write("Item Deleted Successfully");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}