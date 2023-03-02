import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Stream;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine engine;

    @Override
    public void init() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        engine = new TemplateEngine();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ZoneId zoneId = ZoneId.of(parseTimeZone(req));
        Clock clock = Clock.system(zoneId);
        String currentTime = LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + zoneId;
        resp.setContentType("text/html; charset=utf-8");
        resp.addCookie(new Cookie("lastTimezone", zoneId.toString()));

        Context simpleContext = new Context(
                req.getLocale(),
                Map.of("time", currentTime)
        );

        engine.process("time", simpleContext, resp.getWriter());

        resp.getWriter().close();
    }
    public String parseTimeZone(HttpServletRequest request) {
        String defaultTimeZone = "UTC";

        String result = (request.getCookies() == null) ? defaultTimeZone :
                Stream.of(request.getCookies())
                        .filter(c -> "lastTimezone".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findAny().orElseGet(() -> defaultTimeZone);

        if (request.getParameterMap().containsKey("timezone")) {
            return (request.getParameter("timezone").replace(" ", "+").length() < 1) ?
                    result : request.getParameter("timezone").replace(" ", "+").toUpperCase();
        }

        return result;
    }
}