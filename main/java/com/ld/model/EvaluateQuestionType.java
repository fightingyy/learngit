package com.ld.model;

public class EvaluateQuestionType {

	private QuestionType questionType;
	private double probability;
	
	public EvaluateQuestionType() {
		// TODO Auto-generated constructor stub
	}
	public EvaluateQuestionType(QuestionType qType,double probability){
		
		this.questionType=qType;
		this.probability=probability;
	}
	
	public QuestionType getQuestionType() {
		return questionType;
	}
	public void setQuestionType(QuestionType questionType) {
		this.questionType = questionType;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}

}
