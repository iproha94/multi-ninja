package com.wordpress.ilyaps.frontendServlets;

import com.wordpress.ilyaps.ThreadSettings;
import com.wordpress.ilyaps.accountService.UserProfile;
import com.wordpress.ilyaps.frontendService.FrontendService;
import com.wordpress.ilyaps.frontendService.UserState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import com.wordpress.ilyaps.utils.PageGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by v.chibrikov on 13.09.2014.
 */
public class RegisterServlet extends HttpServlet {
    private static final int STATUSTEAPOT = 418;
    private static final String INCOGNITTO = "Incognitto";

    @NotNull
    static final Logger LOGGER = LogManager.getLogger(RegisterServlet.class);
    @NotNull
    private FrontendService feService;

    public RegisterServlet(@NotNull FrontendService feService) {
        this.feService = feService;
    }

    @Override
    public void doGet(@NotNull HttpServletRequest request,
                      @NotNull HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        Map<String, Object> pageVariables = new HashMap<>();

        String nameInSession = (String) request.getSession().getAttribute("name");

        try (PrintWriter pw = response.getWriter()) {
            if (checkNameInSession(pageVariables, nameInSession)) {
                pw.println(PageGenerator.getPage("auth/signup.html", pageVariables));
            } else {
                pw.println(PageGenerator.getPage("authresponse.txt", pageVariables));
            }
        }
    }

    @Override
    public void doPost(@NotNull HttpServletRequest request,
                       @NotNull HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");


        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String nameInSession = (String) request.getSession().getAttribute("name");

        Map<String, Object> pageVariables = new HashMap<>();
        UserState state = feService.checkState(email);


        if (checkNameInSession(pageVariables, nameInSession) &&
                checkState(pageVariables, state) &&
                checkParameters(pageVariables, name, email, password) )
        {
            LOGGER.info("start registration");
            feService.registerUser(name, email, password);

            pageVariables.put("status", HttpServletResponse.SC_NOT_MODIFIED);
            pageVariables.put("info", "wait completed registration");
        }

        try (PrintWriter pw = response.getWriter()) {
            pw.println(PageGenerator.getPage("authresponse.txt", pageVariables));
        }
    }

    boolean checkNameInSession(Map<String, Object> pageVariables, String name) {
        if (name != null && !INCOGNITTO.equals(name)) {
            LOGGER.info("the user has already been authenticated");
            pageVariables.put("status", STATUSTEAPOT);
            pageVariables.put("info", "you has already been authenticated");
            return false;
        }

        return true;
    }

    boolean checkState(Map<String, Object> pageVariables, UserState state) {
        if (state == UserState.SUCCESSFUL_REGISTERED) {
            LOGGER.info("successful registration");
            pageVariables.put("status", HttpServletResponse.SC_OK);
            pageVariables.put("info", "thank you for registration");
            return false;
        } else if (state == UserState.UNSUCCESSFUL_REGISTERED) {
            LOGGER.warn("user with this name or email already exists");
            pageVariables.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pageVariables.put("info", "user with this name or email already exists");
            return false;
        } else if (state == UserState.PENDING_REGISTRATION) {
            LOGGER.warn("user pands registration");
            pageVariables.put("status", HttpServletResponse.SC_NOT_MODIFIED);
            pageVariables.put("info", "your registration not ready.");
            return false;
        }

        return true;
    }

    boolean checkParameters(Map<String, Object> pageVariables, String name, String email, String password) {
        if (name == null || password == null || email == null) {
            LOGGER.info("name or email or password is null");
            pageVariables.put("status", HttpServletResponse.SC_BAD_REQUEST);
            pageVariables.put("info", "name or email or password is null");
            return false;
        } else if (name.length() < 4 || email.length() < 4 || password.length() < 4) {
            LOGGER.info("name or email or password is short");
            pageVariables.put("status", HttpServletResponse.SC_BAD_REQUEST);
            pageVariables.put("info", "name or email or password is short");
            return false;
        } else if (Pattern.matches(".*[А-Яа-я]+.*", name) || Pattern.matches(".*[А-Яа-я]+.*", email)) {
            LOGGER.info("not supported Cyrillic");
            pageVariables.put("status", HttpServletResponse.SC_BAD_REQUEST);
            pageVariables.put("info", "not supported Cyrillic");
            return false;
        }

        return true;
    }
}
