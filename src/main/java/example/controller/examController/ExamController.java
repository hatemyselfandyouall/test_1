package example.controller.examController;

import example.model.dataobject.*;
import example.model.service.ChapterExaminationService;
import example.model.service.ExaminationService;
import example.model.service.QuestionService;
import example.model.service.UserExaminationService;
import example.util.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by YS-GZD-1495 on 2017/12/18.
 */
@Controller
@RequestMapping("/home/mainPage.htm")
public class ExamController {
    @Autowired
    ExaminationService examinationService;

    @Autowired
    QuestionService questionService;
    @Autowired
    ChapterExaminationService chapterExaminationService;

    @Autowired
    UserExaminationService userExaminationService;
    /**
     * 测试主页面
     *
     * @param request
     * @param response
     * @param map
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String index(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
        String id = request.getParameter("id");
        Examination examination = examinationService.getByKey(id);
        User user=(User)request.getSession().getAttribute(ConstantsUtil.ADMINUSER);
        Map<String, Object> param = new HashMap<>();

        if (!examination.getPrice().equals(0.0)) {
            param.put("examinationId", examination.getId());
            param.put("userId", user.getId());
            param.put("orderByStr", "create_time desc");
            List<UserExamination> userExaminations = userExaminationService.findEntitys(param);
            ExaminationUtil.payCheck(userExaminations, examination);
            param.clear();
            if (examination.getCharged() == 0) {
                return "redirect:/home/fail.htm";
            }
        }

        Map<Integer, List<Question>> questionMap;
        Map<Integer,Map<Integer, List<Question>>> examinationQuestionMap = (Map<Integer,Map<Integer, List<Question>>>) request.getSession().getAttribute("examinationQuestionMap");

        if (examinationQuestionMap==null||!examinationQuestionMap.keySet().contains(examination.getId())) {

            param.put("examinationId", examination.getId());
            List<Question> questions = questionService.findEntitys(param);
            List<ChapterExamination> chapterExaminations = chapterExaminationService.findEntitys(param);
            //题库生成
            questionMap = questions.stream().collect(Collectors.groupingBy(item -> item.getQuestionType()));
            questionMap = makeQuestions(questionMap, chapterExaminations);
            examinationQuestionMap=new HashMap<>();
            examinationQuestionMap.put(examination.getId(),questionMap);

            request.getSession().setAttribute("examinationQuestionMap", examinationQuestionMap);
            request.getSession().setAttribute("startExamTime", new Date());
            map.put("question", questionMap.get(1).get(0));
        } else {
            //时间判断
            Date startTime=(Date)request.getSession().getAttribute("startExamTime");
            Date nowTime=new Date();
            Long times=nowTime.getTime() - startTime.getTime();
            if((examination.getExamTime()-times/(1000*60))<0){
                return "redirect:/home/mainPage.htm?action=finalPage&id="+examination.getId();
            }else{
                request.getSession().setAttribute("remainTime", examination.getExamTime() - times / (1000 * 60));
            }

            //session中保存答案库
            Map<Integer, String> answerMap;
            Map<Integer,Map<Integer,String>> examinationAnswerMap=(HashMap<Integer,Map<Integer,String>> )request.getSession().getAttribute("examinationAnswerMap");
            if (examinationAnswerMap!= null&&examinationAnswerMap.get(examination.getId())!=null) {
                answerMap = examinationAnswerMap.get(examination.getId());
            } else {
                answerMap = new HashMap<>();
            }

            //取答题参数
            examinationQuestionMap = (Map<Integer,Map<Integer, List<Question>>>) request.getSession().getAttribute("examinationQuestionMap");
            questionMap=examinationQuestionMap.get(examination.getId());

            Integer questionSort = Integer.valueOf(request.getParameter("questionSort") != null ? request.getParameter("questionSort") : answerMap.keySet().size()+1+"");
            Integer questionType = Integer.valueOf(request.getParameter("questionType") != null ? request.getParameter("questionType") : "1");
            String answerSort = request.getParameter("answersort");
            String answer = request.getParameter("answer");



            //做过的题目显示答案，没做过的题目将答案存入记录
            if (StringUtil.isBlank(answerMap.get(questionSort)) && answer != null) {
                //多选题需进行answer的转化
                if (questionType == 3) {
                    answer = StringUtil.examAnswer2questionAnswer(answer);
                }
                answerMap.put(Integer.valueOf(answerSort), answer);
            } else {
                map.put("answer", answerMap.get(questionSort));
            }

            //更新session中的答题记录
            if (examinationAnswerMap!=null){
                examinationAnswerMap.put(examination.getId(),answerMap);
            }else {
                examinationAnswerMap=new HashMap<>();
                examinationAnswerMap.put(examination.getId(),answerMap);
            }
            request.getSession().setAttribute("examinationAnswerMap", examinationAnswerMap);

            //根据序号获取题目,若序号已超范围，则跳转结果页面
            if (questionSort >= questionMap.get(0).size()) {
                return "redirect:/home/mainPage.htm?action=finalPage&id="+examination.getId();
            }

            Question nowQuesTion = questionMap.get(0).get(questionSort - 1);
            map.put("question", nowQuesTion);
        }

        map.put("examination", examination);
        return "/home/neoexaminationPage";
    }

    @RequestMapping(method = RequestMethod.POST, params = "action=upLoadAnswer")
    public void upLoadAnswer(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws IOException {
        JSONObject result = new JSONObject();
        String id = request.getParameter("id");
        Examination examination = examinationService.getByKey(id);
        Map<Integer, List<Question>> questionMap;
        Map<Integer,Map<Integer, List<Question>>> examinationQuestionMap;
        examinationQuestionMap = (Map<Integer,Map<Integer, List<Question>>>) request.getSession().getAttribute("examinationQuestionMap");
        questionMap=examinationQuestionMap.get(examination.getId());

        Integer questionSort = Integer.valueOf(request.getParameter("questionSort") != null ? request.getParameter("questionSort") : "1");
        Integer questionType = Integer.valueOf(request.getParameter("questionType") != null ? request.getParameter("questionType") : "1");
        String answerSort = request.getParameter("answersort");
        //获取答案保存
        String answer = request.getParameter("answer");

        Map<Integer, String> answerMap;
        Map<Integer,Map<Integer,String>> examinationAnswerMap=(HashMap<Integer,Map<Integer,String>> )request.getSession().getAttribute("examinationAnswerMap");
        if (examinationAnswerMap!= null&&examinationAnswerMap.get(examination.getId())!=null) {
            answerMap = examinationAnswerMap.get(examination.getId());
        } else {
            answerMap = new HashMap<>();
        }

        Date startTime=(Date)request.getSession().getAttribute("startExamTime");
        Date nowTime=new Date();
        Long times=nowTime.getTime() - startTime.getTime();
        if (times>1000*60*examination.getExamTime()){
            result.put("ret_code", "5");
            result.put("ret_msg", "答题超时");
            ResultUtil.writeResult(response, result.toString());
            return;
        }else {
            request.getSession().setAttribute("remainTime",examination.getExamTime()-times/(1000*60));
        }

        //根据序号获取题目,若序号已超范围，则跳转结果页面
        if (questionSort >= questionMap.get(0).size()) {
            String undoList = "这是最后一题了，您还有序号为：";
            for (int i = 0; i < questionSort; i++) {
                if (StringUtil.isBlank(answerMap.get(i))) {
                    undoList += i;
                    undoList += "、";
                }
            }
            undoList = undoList.substring(0, undoList.length() - 1);
            undoList += "的题目未完成，确定要提交吗？";
            result.put("ret_code", "2");
            result.put("ret_msg", undoList);
            ResultUtil.writeResult(response, result.toString());
            return;
        }


        //做过的题目显示答案，没做过的题目将答案存入记录
        if (!StringUtil.isBlank(answerMap.get(Integer.valueOf(answerSort)))){
            result.put("ret_code", "1");
            result.put("ret_msg", "已完成题目");
            ResultUtil.writeResult(response, result.toString());
            return;
        }



        if(StringUtil.isBlank(answer)){
            result.put("ret_code", "4");
            result.put("ret_msg", "跳过本题");
            ResultUtil.writeResult(response, result.toString());
            return;
        }

        if (questionType == 3) {
            answer = StringUtil.examAnswer2questionAnswer(answer);
        }
        answerMap.put(Integer.valueOf(answerSort),answer);

        //更新session中的答题记录
        if (examinationAnswerMap!=null){
            examinationAnswerMap.put(examination.getId(),answerMap);
        }else {
            examinationAnswerMap=new HashMap<>();
            examinationAnswerMap.put(examination.getId(),answerMap);
        }
        request.getSession().setAttribute("examinationAnswerMap", examinationAnswerMap);


        result.put("ret_code", "3");
        result.put("ret_msg", "第一次完成");
        ResultUtil.writeResult(response, result.toString());
        return;
    }

    @RequestMapping(method = RequestMethod.GET, params = "action=finalPage")
    public String finalPage(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws IOException {
        String id = request.getParameter("id");
        Examination examination = examinationService.getByKey(id);
        Map<Integer, List<Question>> questionMap;
        Map<Integer,Map<Integer, List<Question>>> examinationQuestionMap;
        examinationQuestionMap= (Map<Integer, Map<Integer, List<Question>>>) request.getSession().getAttribute("examinationQuestionMap");
        questionMap=examinationQuestionMap.get(examination.getId());
        //获取答案保存
        String answer;
        Map<Integer, String> answerMap;
        Map<Integer,Map<Integer,String>> examinationAnswerMap=(HashMap<Integer,Map<Integer,String>> )request.getSession().getAttribute("examinationAnswerMap");
        if (examinationAnswerMap!= null&&examinationAnswerMap.get(examination.getId())!=null) {
            answerMap = examinationAnswerMap.get(examination.getId());
        } else {
            answerMap = new HashMap<>();
        }
        Integer rightCount=0;
        Integer wrongCount=0;
        Integer undoCount=0;
        Double totalScore=0.0;
        for (int i=0;i<questionMap.get(0).size();i++){
            answer=answerMap.get(i+1);
            if (StringUtil.isBlank(answer)){
                undoCount++;
            }else {
                switch (questionMap.get(0).get(i).getQuestionType()){
                    case 1:
                        if (answer.equals(questionMap.get(0).get(i).getAnswerChoice())){
                            rightCount++;
                            totalScore+=examination.getJudgeScore();
                        }else {
                            wrongCount++;
                        }
                        break;
                    case 2:
                        if (answer.equals(questionMap.get(0).get(i).getAnswerChoice())){
                            rightCount++;
                            totalScore+=examination.getSingleScore();
                        }else {
                            wrongCount++;
                        }
                        break;
                    case 3:
                        if (answer.equals(questionMap.get(0).get(i).getAnswerChoice())){
                            rightCount++;
                            totalScore+=examination.getMultScore();
                        }else {
                            wrongCount++;
                        }
                        break;
                    default:
                }
            }

        }
        String resultDetail="共"+questionMap.get(0).size()+"道题，您答对了"+rightCount+"道题，答错了"+wrongCount+"道题，未完成"+undoCount+"道题，获得的总分为"+totalScore+"分";
        map.put("detail",resultDetail);
        map.put("examination",examination);
        return "/home/examinationFinal";
    }


    @RequestMapping(method = RequestMethod.GET, params = "action=restartPage")
    public String restartPage(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws IOException {
        String id = request.getParameter("id");
        Examination examination = examinationService.getByKey(id);
        Map<Integer,Map<Integer, List<Question>>> examinationQuestionMap;
        examinationQuestionMap= (Map<Integer, Map<Integer, List<Question>>>) request.getSession().getAttribute("examinationQuestionMap");
        if(examinationQuestionMap!=null&&examinationQuestionMap.get(examination.getId())!=null){
            examinationQuestionMap.remove(examination.getId());
        }
        Map<Integer,Map<Integer,String>> examinationAnswerMap=(HashMap<Integer,Map<Integer,String>> )request.getSession().getAttribute("examinationAnswerMap");
        if(examinationAnswerMap!=null&&examinationAnswerMap.get(examination.getId())!=null){
            examinationAnswerMap.remove(examination.getId());
        }
        request.getSession().removeAttribute("startExamTime");
        return "redirect:/home/mainPage.htm?id="+examination.getId();
    }

    /**
     * 将题目按照给与的章节限定进行随机,生成一套试卷
     */
    public Map<Integer, List<Question>> makeQuestions(Map<Integer, List<Question>> questionMap,List<ChapterExamination> chapterExaminations){
        int count=1;
        List<Question> total=new ArrayList<>();
        for (int i=1;i<=4;i++){
            List<Question> judgeQuestions=questionMap.get(i);
            judgeQuestions=classfyQuestion(judgeQuestions, chapterExaminations, i);
            for (int j=0;j<judgeQuestions.size();j++){
                judgeQuestions.get(j).setSort(count);
                count++;
            }
            questionMap.put(i,judgeQuestions);
            total.addAll(judgeQuestions);
        }
        questionMap.put(0,total);
        return questionMap;
    }

    /**
     * 对某个章节进行区分
     * @param unClassfyquestions
     * @param chapterExaminations
     */
    public List<Question> classfyQuestion(List<Question> unClassfyquestions,List<ChapterExamination> chapterExaminations,Integer type){
        if (unClassfyquestions==null||unClassfyquestions.isEmpty()){
            return  new ArrayList<>();
        }
        List<Question> resultQuestions=new ArrayList<>();
        Map<Integer, List<Question>> questionMap = unClassfyquestions.stream().collect(Collectors.groupingBy(item -> item.getChapterId()));
       for (ChapterExamination chapterExamination:chapterExaminations){
           List<Question>questions=questionMap.get(chapterExamination.getCharpterId());
           Integer totalQuestionSize=0;
           Integer questionUse=0;
           switch (type) {
               case 1:
                   totalQuestionSize = chapterExamination.getJudgeSize();
                   questionUse = chapterExamination.getJudgeUse();
                   break;
               case 2:
                   totalQuestionSize = chapterExamination.getSingleSize();
                   questionUse = chapterExamination.getSingleUse();
                   break;
               case 3:
                   totalQuestionSize = chapterExamination.getMultSize();
                   questionUse = chapterExamination.getMultUse();
                   break;
               case 4:
                   totalQuestionSize = chapterExamination.getSaqSize();
                   questionUse = chapterExamination.getSaqUse();
                   break;
               default:
           }
           questions=classfyQuestion(questions,totalQuestionSize,questionUse);
           resultQuestions.addAll(questions);
       }
       return resultQuestions;
    }
    public List<Question>  classfyQuestion(List<Question> questions,Integer totalQuestionSize,Integer questionUse) {
        if (questions==null||questions.isEmpty()||questionUse==0){
            return new ArrayList<>();
        }
        Random random=new Random();
        Set<Question> set=new HashSet<>();
        while (true){
            Integer temp=random.nextInt(questions.size());
            Question question=questions.get(temp);
            set.add(question);
            if (set.size()==questionUse){
                break;
            }
        }
        List<Question> list=new ArrayList<>();
        list.addAll(set);
        return list;
    }
}
