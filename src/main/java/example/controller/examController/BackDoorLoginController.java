package example.controller.examController;

import example.model.service.ExaminationService;
import example.model.service.QuestionService;
import example.util.ConstantsUtil;
import example.util.ResultUtil;
import example.util.StringUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by YS-GZD-1495 on 2017/12/18.
 */
@Controller
@RequestMapping("/exam/login.htm")
public class BackDoorLoginController {
    @Autowired
    ExaminationService examinationService;

    @Autowired
    QuestionService questionService;
    /**
     * 测试主页面
     * @param request
     * @param response
     * @param map
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String index(HttpServletRequest request,HttpServletResponse response,ModelMap map) {
        System.out.println("123");
        return "/exam/login";
    }

    @RequestMapping(method = RequestMethod.POST,params = "action=userLogin")
    public void userLogin(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws UnsupportedEncodingException {
        JSONObject result=new JSONObject();
        String username=request.getParameter("username");
        String password=request.getParameter("password");
        System.out.println(username+password);
        if ("admin".equals(username)&&"admin".equals(password)){
            request.getSession().setAttribute(ConstantsUtil.BACKGROUNDUSER,1);
            result.put("ret",1);
            result.put("msg","成功");
        }else {
            result.put("ret",0);
            result.put("msg","登录失败,用户名或密码错误");
        }
        try {
            ResultUtil.writeResult(response,result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
