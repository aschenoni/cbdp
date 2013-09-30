import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;


public class WebAppMain extends AbstractHandler
{
	// use src file path during dev only.
	String STATIC_PAGE_ROOT = "src/main/resources/web";
	CassClient _cc;

	WebAppMain() {
		_cc = new CassClient();
	}

	public void handle(String target,
			Request baseRequest,
			HttpServletRequest request,
			HttpServletResponse response) 
		throws IOException, ServletException
	{
		if (target.equals("/favicon.ico"))
			return;

		long time_start = System.nanoTime();
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		RuntimeException e = null;
		String r;
		try {
			r = _handle(target, baseRequest);
		} catch (RuntimeException e_) {
			e = e_;
			r = e.toString();
		}
		System.out.printf("%s %s %s %f %s\n",
				baseRequest.getPathInfo(),
				baseRequest.getQueryString(),
				baseRequest.getRemoteHost(),
				(System.nanoTime() - time_start) / 1000000000.0,
				e == null ? "" : r);
		response.getWriter().println(r);
	}

	private String _handle(String target, Request req) {
		try {
			if (target.startsWith("/jquery/")) {
				String t_ = target.substring(8);
				if (t_.equals("get-date-list.json")) {
					return FileListJson.GetDateList(req.getParameter("day_or_week"));
				} else if (t_.equals("get-top-hashtags-by-date.json")) {
					return _cc.GetTopHashtags(
							req.getParameter("day_or_week"),
							req.getParameter("date"),
							Integer.parseInt(req.getParameter("max_result")));
				} else if (t_.equals("get-coord-tid-by-date-ht.json")) {
					return _cc.GetCoordTid(
							req.getParameter("day_or_week"),
							req.getParameter("date"),
							req.getParameter("hashtag"));
				} else if (t_.equals("get-tweet.json")) {
					return _cc.GetTweet(Long.parseLong(req.getParameter("tid")));
				}
			} else {
				if (target.equals("/"))
					return readFile(STATIC_PAGE_ROOT + "/index.html", Charset.defaultCharset());
				else
					return readFile(STATIC_PAGE_ROOT + target, Charset.defaultCharset());
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		throw new RuntimeException("REQUEST NOT HANDLED");
	}


	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}


	public static void main(String[] args) throws Exception {
		try {
			Server server = new Server(8080);
			HandlerCollection handlers = new HandlerCollection();
			RequestLogHandler requestLogHandler = new RequestLogHandler();
			NCSARequestLog requestLog = new NCSARequestLog("./logs/jetty-yyyy_mm_dd.request.log");
			requestLog.setAppend(true);
			requestLog.setExtended(false);
			requestLogHandler.setRequestLog(requestLog);
			handlers.setHandlers(new Handler[]{new WebAppMain(), requestLogHandler});
			server.setHandler(handlers);

			server.start();
			server.join();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
