//package com.ld.model;
//
//public class CandidateAnswer {
//
//	private int answerID;
//	private String answerText;
//	private double answerScore=1.0;
//	
//	public int getAnswerID() {
//		return answerID;
//	}
//	public void setAnswerID(int answerID) {
//		this.answerID = answerID;
//	}
//	public String getAnswerText() {
//		return answerText;
//	}
//	public void setAnswerText(String answerText) {
//		this.answerText = answerText;
//	}
//	public double getAnswerScore() {
//		return answerScore;
//	}
//	public void setAnswerScore(double answerScore) {
//		this.answerScore = answerScore;
//	}
//	
//	 public int compareTo(CandidateAnswer o) {
//	        if (o != null && o instanceof CandidateAnswer) {
//	            CandidateAnswer a = (CandidateAnswer) o;
//	            if (this.answerScore < a.answerScore) {
//	                return -1;
//	            }
//	            if (this.answerScore > a.answerScore) {
//	                return 1;
//	            }
//	            if (this.answerScore == a.answerScore) {
//	                return 0;
////	            }
//	        }
//	        throw new RuntimeException("�޷��Ƚϴ�С");
//	  }
//	 
//	 public boolean equals(Object obj) {
//	        if (obj == null) {
//	            return false;
//	        }
//	        if (!(obj instanceof CandidateAnswer)) {
//	            return false;
//	        }
//	        CandidateAnswer a = (CandidateAnswer) obj;
//	        return this.answerText.equals(a.answerText);
//	 }
//	 
//	 public int hashCode() {
//	        return this.answerText.hashCode();
//	    }
//
//}
