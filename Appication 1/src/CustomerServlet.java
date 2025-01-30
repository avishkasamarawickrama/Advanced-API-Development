import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "CustomerServlet", urlPatterns = {"/pages/customer"})
public class CustomerServlet extends HttpServlet {
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
                        "SELECT id FROM customer ORDER BY CAST(id AS SIGNED) DESC LIMIT 1"
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

            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM customer");
            ResultSet rst = pstm.executeQuery();

            JsonArrayBuilder customers = Json.createArrayBuilder();
            while (rst.next()) {
                JsonObjectBuilder customer = Json.createObjectBuilder()
                        .add("id", rst.getInt("id"))
                        .add("name", rst.getString("name"))
                        .add("email", rst.getString("email"))
                        .add("contact", rst.getString("contact"))
                        .add("address", rst.getString("address"));
                customers.add(customer);
            }

            resp.setContentType("application/json");
            resp.getWriter().write(customers.build().toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObjectBuilder error = Json.createObjectBuilder()
                    .add("message", "Error: " + e.getMessage());
            resp.getWriter().write(error.build().toString());
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Connection connection = null;
        PreparedStatement checkStatement = null;
        PreparedStatement insertStatement = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            // First, check if email or contact exists
            String checkQuery = "SELECT COUNT(*) FROM customer WHERE email = ? OR contact = ?";
            checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, req.getParameter("email"));
            checkStatement.setString(2, req.getParameter("contact"));

            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            if (count > 0) {
                // Email or contact already exists
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\": \"Email or contact number already exists\"}");
                return;
            }

            // If no duplicates found, proceed with insertion
            String insertQuery = "INSERT INTO customer (id, name, email, contact, address) VALUES (?,?,?,?,?)";
            insertStatement = connection.prepareStatement(insertQuery);

            insertStatement.setInt(1, Integer.parseInt(req.getParameter("id")));
            insertStatement.setString(2, req.getParameter("name"));
            insertStatement.setString(3, req.getParameter("email"));
            insertStatement.setString(4, req.getParameter("contact"));
            insertStatement.setString(5, req.getParameter("address"));

            if (insertStatement.executeUpdate() > 0) {
                resp.setContentType("application/json");
                resp.getWriter().write("{\"message\": \"Customer Added Successfully\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            try {
                if (checkStatement != null) checkStatement.close();
                if (insertStatement != null) insertStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement pstm = connection.prepareStatement(
                    "UPDATE customer SET name=?, email=?, contact=?, address=? WHERE id=?"
            );

            pstm.setString(1, req.getParameter("name"));
            pstm.setString(2, req.getParameter("email"));
            pstm.setString(3, req.getParameter("contact"));
            pstm.setString(4, req.getParameter("address"));
            pstm.setInt(5, Integer.parseInt(req.getParameter("id")));

            if (pstm.executeUpdate() > 0) {
                resp.getWriter().write("Customer Updated Successfully");
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
                    "DELETE FROM customer WHERE id=?"
            );

            pstm.setInt(1, Integer.parseInt(req.getParameter("id")));

            if (pstm.executeUpdate() > 0) {
                resp.getWriter().write("Customer Deleted Successfully");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}