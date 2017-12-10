package com.ld.model;

import com.hankcs.hanlp.corpus.tag.Nature;

public enum QuestionType {
	
	HUMAN("人物"), LOCATION("地点"),TIME("时间"),NUMBER("数字"),OBJECT("实体"),DESCRIPTION("描述"),OTHER("其他");
	
	public Nature getPos() {
        
		Nature pos = null;
		//nr 人名
        if (QuestionType.HUMAN == this) {
            pos = Nature.nr;
        }
    	//ns 地名
        if (QuestionType.LOCATION == this) {
            pos = Nature.ns;
        }
        //nt 团体机构名
//        if (QuestionType.ORGANIZATION_NAME == this) {
//            pos = "nt";
//        }
        //m=数词
        //mh=中文数词
        //mb=百分数词
        //mf=分数词
        //mx=小数词
        //mq=数量词
//        if (QuestionType.NUMBER == this) {
//            pos = Nature.m;
//        }
        //t=时间词
        //tq=时间量词
        //tdq=日期量词
        if (QuestionType.TIME == this) {
            pos = Nature.t;
        }

        return pos;
    }
    
    private QuestionType(String des){
        this.des = des;
    }
    private final String des;

    public String getDes() {
        return des;
    }
}
