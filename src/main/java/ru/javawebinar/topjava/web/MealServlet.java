package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.dao.Dao;
import ru.javawebinar.topjava.dao.MealDao;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;
import ru.javawebinar.topjava.storage.MealDataSource;
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
    private static final Dao<Meal> dao = new MealDao();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        switch ((action == null) ? "" : action) {
            case "add": {
                log.debug("doGet -> add");

                Meal meal = new Meal(LocalDateTime.now().withSecond(0).withNano(0), "", 0);
                request.setAttribute("meal", meal);
                request.getRequestDispatcher("/meal.jsp").forward(request, response);
                break;
            }

            case "edit": {
                log.debug(String.format("doGet -> edit (id = %s)",
                                        request.getParameter("id")));

                Meal meal = dao.getById(getIdFromRequest(request));
                request.setAttribute("meal", meal);
                request.getRequestDispatcher("/meal.jsp").forward(request, response);
                break;
            }

            case "delete": {
                log.debug(String.format("doGet -> delete (id = %s)",
                                        request.getParameter("id")));

                dao.delete(getIdFromRequest(request));
                response.sendRedirect("meals");
                break;
            }

            default: {
                log.debug("doGet -> default (get all meals)");

                List<MealTo> mealsTo = MealsUtil.getAllByStreams(dao.getAll(), MealsUtil.DEFAULT_CALORIES_PER_DAY);
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
        switch ((submit == null) ? "Отменить" : submit) {
            case "Сохранить": {
                log.debug("doPost -> Сохранить");

                int id = getIdFromRequest(request);

                String description = request.getParameter("description");
                if (description != null) {
                    description = description.trim();
                }

                LocalDateTime dateTime = null;
                try {
                    dateTime = LocalDateTime.parse(request.getParameter("dateTime"));
                }
                catch (Exception e) {
                    log.debug(String.format("MealsServlet -> Exception at parse of dateTime (%s)",
                                            request.getParameter("dateTime")));
                }

                int calories = 0;
                try {
                    calories = Integer.parseInt(request.getParameter("calories"));
                }
                catch (NumberFormatException e) {
                    log.debug(String.format("MealsServlet -> NumberFormatException at parse of calories (%s)",
                                            request.getParameter("calories")));
                }

                Meal meal = new Meal(id, dateTime, description, calories);
                if (!dao.isValid(meal)) {
                    log.debug(String.format("doPost -> meal is not valid (%s)", meal));
                    String criteria = dao.getVerificationCriteria();
                    request.setAttribute("meal", meal);
                    request.setAttribute("criteria", criteria);
                    request.getRequestDispatcher("/meal.jsp").forward(request, response);
                    return;
                }

                if (meal.isNew()) {
                    log.debug(String.format("doPost -> add meal (%s)", meal));
                    dao.add(meal);
                } else {
                    log.debug(String.format("doPost -> edit meal (%s)", meal));
                    dao.update(meal);
                }
                /*falls through*/
            }

            case "Отменить":
            default: {
                response.sendRedirect("meals");
                break;
            }
        }
    }

    private int getIdFromRequest(HttpServletRequest request) {
        int id = 0;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        }
        catch (NumberFormatException e) {
            log.debug(String.format("MealsServlet -> NumberFormatException at parse of id (%s)",
                    request.getParameter("id")));
        }
        return id;
    }
}
