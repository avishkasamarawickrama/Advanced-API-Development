import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.IOException;
import java.sql.*;
import java.util.stream.Collectors;

@WebServlet(name = "OrderServlet", urlPatterns = {"/pages/order"})
public class OrderServlet extends HttpServlet {
    private static final String URL = "jdbc:mysql://localhost:3306/shop";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ijse@123";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            String option = req.getParameter("option");
            if (option != null) {
                switch (option) {
                    case "GETID":
                        handleGetNextId(connection, resp);
                        break;
                    case "CUSTOMERS":
                        handleGetCustomers(connection, resp);
                        break;
                    case "ITEMS":
                        handleGetItems(connection, resp);
                        break;
                    default:
                        handleGetOrders(connection, resp);
                }
                return;
            }

            handleGetOrders(connection, resp);

        } catch (Exception e) {
            sendErrorResponse(resp, e);
        }
    }

    private void handleGetNextId(Connection connection, HttpServletResponse resp) throws SQLException, IOException {
        PreparedStatement pstm = connection.prepareStatement(
                "SELECT id FROM orders ORDER BY CAST(SUBSTRING(id, 2) AS SIGNED) DESC LIMIT 1"
        );
        ResultSet rst = pstm.executeQuery();

        String nextId = "O1";
        if (rst.next()) {
            String lastId = rst.getString("id");
            int id = Integer.parseInt(lastId.substring(1)) + 1;
            nextId = "O" + id;
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", nextId);

        sendJsonResponse(resp, response.build().toString());
    }

    private void handleGetCustomers(Connection connection, HttpServletResponse resp) throws SQLException, IOException {
        PreparedStatement pstm = connection.prepareStatement("SELECT * FROM customer");
        ResultSet rst = pstm.executeQuery();

        JsonArrayBuilder customers = Json.createArrayBuilder();
        while (rst.next()) {
            JsonObjectBuilder customer = Json.createObjectBuilder()
                    .add("id", rst.getString("id"))
                    .add("name", rst.getString("name"))
                    .add("contact", rst.getString("contact"));
            customers.add(customer);
        }

        sendJsonResponse(resp, customers.build().toString());
    }

    private void handleGetItems(Connection connection, HttpServletResponse resp) throws SQLException, IOException {
        PreparedStatement pstm = connection.prepareStatement("SELECT * FROM item");
        ResultSet rst = pstm.executeQuery();

        JsonArrayBuilder items = Json.createArrayBuilder();
        while (rst.next()) {
            JsonObjectBuilder item = Json.createObjectBuilder()
                    .add("id", rst.getString("id"))
                    .add("name", rst.getString("name"))
                    .add("price", rst.getDouble("price"))
                    .add("stock", rst.getInt("stock"));
            items.add(item);
        }

        sendJsonResponse(resp, items.build().toString());
    }

    private void handleGetOrders(Connection connection, HttpServletResponse resp) throws SQLException, IOException {
        PreparedStatement pstm = connection.prepareStatement(
                "SELECT o.*, c.name as customer_name, " +
                        "COUNT(oi.item_id) as item_count, " +
                        "SUM(oi.quantity * oi.unit_price) as total_amount " +
                        "FROM orders o " +
                        "JOIN customer c ON o.customer_id = c.id " +
                        "LEFT JOIN order_items oi ON o.id = oi.order_id " +
                        "GROUP BY o.id"
        );
        ResultSet rst = pstm.executeQuery();

        JsonArrayBuilder orders = Json.createArrayBuilder();
        while (rst.next()) {
            JsonObjectBuilder order = Json.createObjectBuilder()
                    .add("id", rst.getString("id"))
                    .add("date", rst.getString("date"))
                    .add("customerId", rst.getString("customer_id"))
                    .add("customerName", rst.getString("customer_name"))
                    .add("itemCount", rst.getInt("item_count"))
                    .add("totalAmount", rst.getDouble("total_amount"));
            orders.add(order);
        }

        sendJsonResponse(resp, orders.build().toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String requestBody = req.getReader().lines().collect(Collectors.joining());
            JsonReader jsonReader = Json.createReader(new java.io.StringReader(requestBody));
            var orderObj = jsonReader.readObject();

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            connection.setAutoCommit(false);

            try {
                // Insert order header
                PreparedStatement orderStmt = connection.prepareStatement(
                        "INSERT INTO orders (id, customer_id, date) VALUES (?, ?, ?)"
                );
                orderStmt.setString(1, orderObj.getString("orderId"));
                orderStmt.setString(2, orderObj.getString("customerId"));
                orderStmt.setString(3, orderObj.getString("orderDate"));
                orderStmt.executeUpdate();

                // Insert order items and update inventory
                var items = orderObj.getJsonArray("items");
                PreparedStatement itemStmt = connection.prepareStatement(
                        "INSERT INTO order_items (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)"
                );
                PreparedStatement updateStockStmt = connection.prepareStatement(
                        "UPDATE item SET stock = stock - ? WHERE id = ?"
                );

                for (var item : items) {
                    var itemObj = item.asJsonObject();

                    // Add order item
                    itemStmt.setString(1, orderObj.getString("orderId"));
                    itemStmt.setString(2, itemObj.getString("itemId"));
                    itemStmt.setInt(3, itemObj.getInt("quantity"));
                    itemStmt.setDouble(4, itemObj.getJsonNumber("price").doubleValue());
                    itemStmt.executeUpdate();

                    // Update stock
                    updateStockStmt.setInt(1, itemObj.getInt("quantity"));
                    updateStockStmt.setString(2, itemObj.getString("itemId"));
                    updateStockStmt.executeUpdate();
                }

                connection.commit();
                resp.getWriter().write("Order placed successfully");

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

        } catch (Exception e) {
            sendErrorResponse(resp, e);
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().write(json);
    }

    private void sendErrorResponse(HttpServletResponse resp, Exception e) throws IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        JsonObjectBuilder error = Json.createObjectBuilder()
                .add("message", "Error: " + e.getMessage());
        resp.getWriter().write(error.build().toString());
    }
}