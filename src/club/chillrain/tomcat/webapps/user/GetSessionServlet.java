package club.chillrain.tomcat.webapps.user;

import club.chillrain.servlet.servlet.HttpServlet;
import club.chillrain.servlet.servlet.ServletRequest;
import club.chillrain.servlet.servlet.ServletResponse;
import club.chillrain.servlet.annotation.WebServlet;
import club.chillrain.tomcat.webapps.user.bean.User;

import java.io.IOException;

/**
 * @author ChillRain 2023 08 17
 */
@WebServlet("/getSession")
public class GetSessionServlet extends HttpServlet {
    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException {
        User loginUser = (User)request.getSession().getAttribute("loginUser");
        response.getWriter().write(loginUser.toString());
    }
}
