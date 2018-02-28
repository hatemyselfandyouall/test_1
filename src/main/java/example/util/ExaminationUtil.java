package example.util;

import example.model.dataobject.Examination;
import example.model.dataobject.UserExamination;

import java.util.Date;
import java.util.List;

/**
 * Created by YS-GZD-1495 on 2018/2/28.
 */
public class ExaminationUtil {

    public static void payCheck(List<UserExamination> userExaminations,Examination examination){
        if (!userExaminations.isEmpty()) {
            Date now=new Date();
            if (now.compareTo(DateUtil.dateAfter1Day(userExaminations.get(0).getCreateTime()))<0) {//超时
                examination.setCharged(1);
                examination.setChargeFinalTime(DateUtil.dateAfter1Day(userExaminations.get(0).getCreateTime()));
            } else {
                examination.setCharged(0);
            }
        } else {
            examination.setCharged(0);
        }
    }
}
