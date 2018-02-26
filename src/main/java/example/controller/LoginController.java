package example.controller;

import example.model.service.UserService;
import example.util.StringUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

/**
 * Created by fox on 2017/9/3.
 */
@Controller
@RequestMapping("/login.htm")
public class LoginController extends ReportCrawlerPorxy {

    @Autowired
    UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public String index(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws UnsupportedEncodingException {
        response.setContentType("text/html; charset=UTF-8");
        JSONObject result=new JSONObject();
        String url=(String)request.getSession().getAttribute("url");
        String code=request.getParameter("code");
        String state=request.getParameter("state");
        url= StringUtil.replaceStr(url);
        if(!"123".equals(state)){
            result.put("ret_code",1);
            result.put("ret_msg","失败,state错误");
            map.put("result",result);
            return "result";
        }
        map.put("ret_msg", checkLogin(request, code));
        return "redirect:"+url;
//        return "test";
    }
}
