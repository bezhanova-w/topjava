package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.dao.Dao;
import ru.javawebinar.topjava.dao.MealInMemoryDao;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static final Logger log = getLogger(MealServlet.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Dao<Meal> dao;

    @Override
    public void init() throws ServletException {
        super.init();
        dao = new MealInMemoryDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        switch ((action == null) ? "" : action) {
            case "add":
            case "edit": {
                    Meal meal;
                    if (action.equals("add")) {
                        log.debug("doGet -> add");
                        meal = new Meal(LocalDateTime.now().withSecond(0).withNano(0), "", 0);
                    } else {
                        log.debug(String.format("doGet -> edit (id = %s)", request.getParameter("id")));
                        meal = dao.getById(getIdFromRequest(request));
                    }

                request.setAttribute("meal", meal);
                request.getRequestDispatcher("/meal.jsp").forward(request, response);
                break;
            }

            case "delete": {
                log.debug(String.format("doGet -> delete (id = %s)", request.getParameter("id")));

                dao.delete(getIdFromRequest(request));
                response.sendRedirect("meals");
                break;
            }

            default: {
                log.debug("doGet -> default (get all meals)");

                List<MealTo> mealsTo = MealsUtil.filteredByStreams(dao.getAll(), LocalTime.MIN, LocalTime.MAX, MealsUtil.DEFAULT_CALORIES_PER_DAY);
                request.setAttribute("allMeals", mealsTo);
                request.setAttribute("dateTimeFormatter", dateTimeFormatter);
                request.getRequestDispatcher("/meals.jsp").forward(request, response);
                break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String submit = request.getParameter("submit");
        if ("Сохранить".equals(submit)) {
            log.debug("doPost -> Сохранить");

            int id = getIdFromRequest(request);
            String description = request.getParameter("description").trim();
            LocalDateTime dateTime = LocalDateTime.parse(request.getParameter("dateTime"));
            int calories = Integer.parseInt(request.getParameter("calories"));

            Meal meal = new Meal(id, dateTime, description, calories);
            if (meal.isNew()) {
                log.debug(String.format("doPost -> add meal (%s)", meal));
                dao.add(meal);
            } else {
                log.debug(String.format("doPost -> edit meal (%s)", meal));
                dao.update(meal);
            }
        }
        response.sendRedirect("meals");
    }

    private int getIdFromRequest(HttpServletRequest request) {
        return Integer.parseInt(request.getParameter("id"));
    }
}
