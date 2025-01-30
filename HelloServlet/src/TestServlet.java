import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/test")
public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter out= resp.getWriter();

        out.println("<h1>Hello</h1>");
        out.println("<p>hi welcome</p>");
        out.println("<style>");
        out.println("body{");
        out.println(" background-color:gray");
        out.println("}");
        out.println("</style>");

    }
}
