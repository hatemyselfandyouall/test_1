 
package example.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Integer;

import example.model.dao.ExaminationMapper;
import example.model.dataobject.Examination;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import example.model.dao.ChapterExaminationMapper;
import example.model.dataobject.ChapterExamination;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ChapterExaminationServiceImpl implements ChapterExaminationService{

	@Autowired
	private ChapterExaminationMapper dao;

	@Autowired
	private ExaminationMapper examinationMapper;
	
	public Integer insert(Object entity){
		return dao.insert((ChapterExamination)entity);
	}

	public Integer update(Object entity){
		return dao.update((ChapterExamination)entity);
	}
	
	public Integer deleteByKey( String key){
		return dao.deleteByKey(key);
	}
	
	public ChapterExamination getByKey( String key){
		return dao.getByKey(key);
	}
	 
	public List<ChapterExamination> findEntitys(Map<String ,Object>  param){
		return dao.findEntitys(param);
	}
	public Integer getEntitysCount(Map<String ,Object>  param){
		return dao.getEntitysCount(param);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer SaveChapSetting(ChapterExamination chapterExamination) {
		Map<String,Object> param=new HashMap<>();
		param.put("charpterId",chapterExamination.getCharpterId());
		param.put("examinationId",chapterExamination.getExaminationId());
		List<ChapterExamination> chapterExaminations=findEntitys(param);
		if (chapterExaminations.isEmpty()){
			chapterExamination.setIsDelete(0);
			insert(chapterExamination);
		}else {
			chapterExamination.setId(chapterExaminations.get(0).getId());
			update(chapterExamination);
		}
		Examination examination=examinationMapper.getByKey(chapterExamination.getExaminationId().toString());
		param.remove("charpterId");
		Integer judgeSize=0;
		Integer singleSize=0;
		Integer multSize=0;
		Integer saqSize=0;
		for (ChapterExamination chapterExamination1:findEntitys(param)){
			judgeSize+=chapterExamination1.getJudgeUse()!=null?chapterExamination1.getJudgeUse():0;
			singleSize+=chapterExamination1.getSingleUse()!=null?chapterExamination1.getSingleUse():0;
			multSize+=chapterExamination1.getMultUse()!=null?chapterExamination1.getMultUse():0;
			saqSize+=chapterExamination1.getSaqUse()!=null?chapterExamination1.getSaqUse():0;
		}
		examination.setJudgeUse(judgeSize);
		examination.setSingleUse(singleSize);
		examination.setMultUse(multSize);
		examination.setSaqUse(saqSize);
		return examinationMapper.update(examination);
	}


}

 
