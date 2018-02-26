package example.controller.examController;

import example.model.dataobject.Examination;
import example.model.service.ExaminationService;
import example.model.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by YS-GZD-1495 on 2018/2/6.
 */
@Controller
@RequestMapping("/home/examinationusercenter.htm")
public class ExaminationUserCenterController {

    @Autowired
    ExaminationService examinationService;

    @Autowired
    QuestionService questionService;

    /**
     * 题库列表
     *
     * @param request
     * @param response
     * @param map
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String examManager(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        map.put("isDelete",0);
        List<Examination> examinations=examinationService.findEntitys(map);
        map.put("examinations",examinations);
        return "/home/examinationUserCenter";
    }
}
