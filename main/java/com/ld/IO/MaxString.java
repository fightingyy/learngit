package com.ld.IO;


/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：MaxString   
* 类描述：  两个字符串间的比较
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:32:09   
* @version        
*/
public class MaxString {
	
	
	public String maxCommonStr="";
	public String maxStr="";
	double maxScore=0;
	public static int right_word_index;/**最长公共子序列的第一个字在问句中的位置*/
	public static int left_word_index;/**最长公共子序列最后个字在问句中的位置*/
	public static int right_word_index_answer;/**最长公共子序列的第一个字在答案中的位置*/
	public static int left_word_index_answer;/**最长公共子序列的最后个字在答案中的位置*/

	public static void main(String[] args) {
        String aa = "不同的器官按一定的次序组合起来具有特定的生理功能的结构叫什么";
        String bb = "由不同器官按照一定的次序组合在一起，能够共同完成一种或几种生理功能的结构";
        String maxString=longestCommonSubsequence(aa, bb);
        System.out.println(maxString);
    }
	
	/**
	* 两个字符串的最长公共子序列
	* @param str1
	* @param str2
	* @return
	*/
	public static String longestCommonSubsequence(String str1, String str2)
    {
		char[] str1Array=str1.toCharArray();
		char[] str2Array=str2.toCharArray();
        int substringLength1 = str1.length();
        int substringLength2 = str2.length();
 
        // 构造二维数组记录子问题A[i]和B[j]的LCS的长度
        int[][] opt = new int[substringLength1 + 1][substringLength2 + 1];
 
        // 从后向前，动态规划计算所有子问题。也可从前到后。
        for (int i = substringLength1 - 1; i >= 0; i--){
        	
            for (int j = substringLength2 - 1; j >= 0; j--)
            {
                if (str1Array[i] == str2Array[j])
                    opt[i][j] = opt[i + 1][j + 1] + 1;// 状态转移方程
                else
                    opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);// 状态转移方程
            }
        }
        String CommonSubsequence="";
 
        int i = 0, j = 0;
        int count=0;
        
        while (i < substringLength1 && j < substringLength2)
        {
            if (str1Array[i] == str2Array[j])
            {
            	if(count==0){
            		left_word_index=i;
            		left_word_index_answer=j;
            		count++;
            	}
            	else{
            		right_word_index=i;
            		right_word_index_answer=j;
            	}
            	CommonSubsequence+=String.valueOf(str1Array[i]);
                i++;
                j++;
            }
            else if (opt[i + 1][j] >= opt[i][j + 1])
                i++;
            else
                j++;
        }
        return CommonSubsequence;
    }

    /**
     * 两个字符串的最长公共子串
     * @param str1
     * @param str2
     * @return
     */
    public static String getMaxString(String str1, String str2) {
    	 String maxStr="";
    	if(str1.equals("")||str2.equals(""))
    		return maxStr;
        //把字符串转成字符数组
        char[] arr1 = str1.toCharArray();
        char[] arr2 = str2.toCharArray();
        // 把两个字符串分别以行和列组成一个二维矩阵
        int[][] temp = new int[arr1.length][arr2.length];
        // 存储最长公共子串长度
        int length = 0;
        //start表明最长公共子串的起始点，end表明最长公共子串的终止点
        int end = 0;
        int start = 0;
        ////初始化二维矩阵中的第一行
        for (int i = 0; i < arr2.length; i++) {
            temp[0][i] = (arr1[0] == arr2[i]) ? 1 : 0;
        }
        //初始化二维矩阵中的第一列
        for (int j = 0; j < arr1.length; j++) {
            temp[j][0] = (arr2[0] == arr1[j]) ? 1 : 0;
        }
        //嵌套for循环：比较二维矩阵中每个点对应行列字符中否相等，相等的话值设置为1，否则设置为0
        for (int i = 1; i < arr1.length; i++) {
            for (int j = 1; j < arr2.length; j++) {
                if (arr1[i] == arr2[j]) {
                    temp[i][j] = temp[i - 1][j - 1] + 1;

                    if (temp[i][j] > length) {
                        length = temp[i][j];
                        end = j;
                    }
                } else
                    temp[i][j] = 0;

            }
        }
        //求出最长公共子串的起始点
        start=end-length+1;
       
        //通过查找出值为1的最长对角线就能找到最长公共子串
        for (int j = start; j < end+1; j++) {
            maxStr+=arr2[j];
        }

        if(maxStr.endsWith("的"))maxStr=maxStr.substring(0,maxStr.length()-1);
        return maxStr;
    }
    
    
    public static String subString(String mark,String item,String commonStr){
    	
    	String subStr="";
    	String[] array=item.split(mark);
    	for(int i=0;i<array.length;i++){
    		if(array[i].contains(commonStr))
    			subStr=array[i];
    	}
//    	int index=item.lastIndexOf(mark, item.indexOf(commonStr));
//		int index1=item.indexOf(mark, item.indexOf(commonStr));		        			
//		if(index<0) index=index1;
//		
//		if(index>0)
//			subStr=item.substring(index+1);
//		if(index1>0)
//			subStr=item.substring(0,index);
		return subStr;
    }
    
//    public void getMaxCommon(GraphQueryResult result,Map courseMap, Map commonMap,String question,String course){
//    	
//    	boolean isAnswer=false;
//    	VirtuosoSearch virtuosoSearch=new VirtuosoSearch(course);
//    	String tempStr;
//        try {
//			while (result.hasNext()) { 
//				Statement statement = result.next();
//			    String subject=statement.getSubject().stringValue();
//			    String predicate=statement.getPredicate().stringValue();
//			    String value=statement.getObject().stringValue();
//			    if(predicate.contains("Topic_content")||(predicate.contains("label")&&!course.equals("biology"))||predicate.contains("description")||predicate.contains("category")||predicate.contains("common#source")||predicate.contains("type")||predicate.contains("categoryId")||predicate.contains("annotation")||predicate.contains("image")) continue;
//			    
//			    subject=virtuosoSearch.getLabel(subject);
//			    if(SesameSearch.isUrl(value)&&value.startsWith("http://")&&!value.contains("jpg")&&!value.contains("png")) 
//			    	value=virtuosoSearch.getLabel(value);
//			    boolean isPredicate=false;
//			    predicate=predicate.split("#")[1];
////            if(typeList.contains(predicate)&&) isPredicate=true;
//			    if(courseMap.get(predicate)!=null)
//					predicate=(String) courseMap.get(predicate);
//				if(commonMap.get(predicate)!=null)
//					predicate=(String) commonMap.get(predicate);
//			    tempStr=subject+"--"+predicate+":<br>"+value;
////	            tempStr=tempStr.replaceAll(" ", "");
//			    
//			    String commonStr=MaxString.getMaxString(question, tempStr);
//			    
//			    if(commonStr.length()>maxCommonStr.length()){
//			    	maxCommonStr=commonStr;
//			    	maxStr=tempStr;
////            	subjectName=subject;
//			    }
//			    else if(commonStr.length()==maxCommonStr.length()&&maxStr.contains(":")&&maxStr.substring(maxStr.indexOf(":")+1).equals("<br>"+value)){
//			    	if(maxStr.startsWith("null"))
//			    		maxStr=tempStr;
//			    }
//			    else if(commonStr.length()==maxCommonStr.length()&&commonStr.length()>3){
//	            	if(maxStr.startsWith(subject+"--")){
//	            		if(maxStr.contains(subject+"--名称")){
//		            		maxCommonStr=commonStr;
//			            	maxStr=tempStr;
//			            	isAnswer=true;
//	            		}
//	            	}
//	            	else if(maxCommonStr.startsWith("的")&&!commonStr.startsWith("的")){
//	            		maxCommonStr=commonStr;
//		            	maxStr=tempStr;
//		            	isAnswer=true;
//	            	}
//	            	else if(commonStr.startsWith("的")&&!maxCommonStr.startsWith("的")){
//	            		isAnswer=true;
//	            	}
//	            	if(!isAnswer&&maxCommonStr.length()>3){
//	            		
//			    		double score=0;
//			    		maxScore=semanticSimilarity.getSimilarity(question, maxStr);
//			    		score=semanticSimilarity.getSimilarity(question, tempStr);
//			    		if(score>maxScore&&maxScore>0){
//			    		maxCommonStr=commonStr;
//			        	maxStr=tempStr;
////	            	subjectName=subject;
//			        	maxScore=score;
//			    		}
//			    	}
//			    }
////	            j++;
//			}
//		} catch (QueryEvaluationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
//    public void getMaxCommon(Graph graph,Map courseMap, Map commonMap,String question,String course){
//    	
//        if(graph!=null&&!graph.isEmpty()){
//        	boolean isAnswer=false;
//        	VirtuosoSearch virtuosoSearch=new VirtuosoSearch(course);
//        	String tempStr;
//        	for (Iterator it = graph.find(Node.ANY, Node.ANY, Node.ANY); it.hasNext();){
//            	Triple t = (Triple)it.next();
//        		
//	            String subject=t.getSubject().toString();
//	            String predicate=t.getPredicate().toString();
//	            String value=t.getObject().toString();
//			    if(predicate.contains("Topic_content")||(predicate.contains("label")&&!course.equals("biology"))||predicate.contains("description")||predicate.contains("category")||predicate.contains("common#source")||predicate.contains("type")||predicate.contains("categoryId")||predicate.contains("annotation")||predicate.contains("image")) continue;
//			    
//			    subject=virtuosoSearch.getLabel(subject);
//				if(SesameSearch.isUrl(value)&&value.startsWith("http://")&&!value.contains("jpg")&&!value.contains("png")) 
//					value=virtuosoSearch.getLabel(value);
//
//			    predicate=predicate.split("#")[1];
////            if(typeList.contains(predicate)&&) isPredicate=true;
//			    if(courseMap.get(predicate)!=null)
//					predicate=(String) courseMap.get(predicate);
//				if(commonMap.get(predicate)!=null)
//					predicate=(String) commonMap.get(predicate);
//				if(value.startsWith("\"")&&value.endsWith("\""))
//					value=value.replaceAll("\"", "");
//			    tempStr=subject+"--"+predicate+":<br>"+value;
////	            tempStr=tempStr.replaceAll(" ", "");
//			    
//			    String commonStr=MaxString.getMaxString(question, tempStr);
//			    
//			    if(commonStr.length()>maxCommonStr.length()){
//			    	maxCommonStr=commonStr;
//			    	maxStr=tempStr;
////            	subjectName=subject;
//			    }
//			    else if(commonStr.length()==maxCommonStr.length()&&maxStr.contains(":")&&maxStr.substring(maxStr.indexOf(":")+1).equals("<br>"+value)){
//			    	if(maxStr.startsWith("null"))
//			    		maxStr=tempStr;
//			    }
//			    else if(commonStr.length()==maxCommonStr.length()&&commonStr.length()>3){
//	            	if(maxStr.startsWith(subject+"--")){
//	            		if(maxStr.contains(subject+"--名称")){
//		            		maxCommonStr=commonStr;
//			            	maxStr=tempStr;
//			            	isAnswer=true;
//	            		}
//	            	}
//	            	else if(maxCommonStr.startsWith("的")&&!commonStr.startsWith("的")){
//	            		maxCommonStr=commonStr;
//		            	maxStr=tempStr;
//		            	isAnswer=true;
//	            	}
//	            	else if(commonStr.startsWith("的")&&!maxCommonStr.startsWith("的")){
//	            		isAnswer=true;
//	            	}
//	            	if(!isAnswer&&maxCommonStr.length()>3){
//	            		
//			    		double score=0;
//			    		maxScore=semanticSimilarity.getSimilarity(question, maxStr);
//			    		score=semanticSimilarity.getSimilarity(question, tempStr);
//			    		if(score>maxScore&&maxScore>0){
//			    		maxCommonStr=commonStr;
//			        	maxStr=tempStr;
////	            	subjectName=subject;
//			        	maxScore=score;
//			    		}
//			    	}
//			    }
//        	}
//        }
//    }
    
//    public void getMaxCommon(List<String> resultList,String question,String course){
//    	
//        if(!resultList.isEmpty()){
//        	boolean isAnswer=false;
//        	for (String tempStr:resultList){
//			    
//			    String commonStr=MaxString.getMaxString(question, tempStr);
//			    
//			    if(commonStr.length()>maxCommonStr.length()){
//			    	maxCommonStr=commonStr;
//			    	maxStr=tempStr;
//			    }
//			    else if(commonStr.length()==maxCommonStr.length()&&commonStr.length()>3){
//	            	if(maxCommonStr.startsWith("的")&&!commonStr.startsWith("的")){
//	            		maxCommonStr=commonStr;
//		            	maxStr=tempStr;
//		            	isAnswer=true;
//	            	}
//	            	else if(commonStr.startsWith("的")&&!maxCommonStr.startsWith("的")){
//	            		isAnswer=true;
//	            	}
//	            	if(!isAnswer&&maxCommonStr.length()>3){
//	            		
//			    		double score=0;
//			    		maxScore=semanticSimilarity.getSimilarity(question, maxStr);
//			    		score=semanticSimilarity.getSimilarity(question, tempStr);
//			    		if(score>maxScore&&maxScore>0){
//			    		maxCommonStr=commonStr;
//			        	maxStr=tempStr;
////	            	subjectName=subject;
//			        	maxScore=score;
//			    		}
//			    	}
//			    }
//        	}
//        }
//    }
    
	
	/**
	 * 最长公共子序列
	 * @param S1
	 * @param S2
	 * @return
	 */
//	public static String longestCommonSubsequence(String S1, String S2) {
//		String CommonSubsequence="";
//		
//        int map[][] = new int[S1.length() + 1][S2.length() + 1];
//        for (int i = 0; i <= S1.length(); i++)
//            for (int j = 0; j <= S2.length(); j++) {
//                if (i == 0 || j == 0)
//                    map[i][j] = 0;
//                else if (S1.charAt(i - 1) == S2.charAt(j - 1)){
//                    map[i][j] = 1 + map[i - 1][j - 1];
//                    if(!CommonSubsequence.endsWith(String.valueOf(S1.charAt(i-1))))
//                    	CommonSubsequence+=S1.charAt(i-1);
//                }
//                else
//                    map[i][j] = map[i - 1][j] > map[i][j - 1] ? map[i - 1][j] : map[i][j - 1];
//            }
//        int i = S1.length(), j = S2.length();
//        ArrayList<Integer> left = new ArrayList<Integer>();
//        ArrayList<Integer> right = new ArrayList<Integer>();
//        while (i > 0 && j > 0) {
//            if (S1.charAt(i - 1) == S2.charAt(j - 1)) {
//                left.add(i - 1);
//                right.add(j - 1);
//                i--;
//                j--;
//            } else if (map[i - 1][j] > map[i][j - 1])
//                i--;
//            else
//                j--;
//        }
//        if (map[S1.length()][S2.length()] != 0) {
////            int leftRange = left.get(0) - left.get(left.size() - 1) + 1;
////            int rightRange = right.get(0) - right.get(right.size()-1) + 1;
////            double score=0;
////            float count=map[S1.length()][S2.length()];
////            score=count/ Math.sqrt(Math.abs(S2.length()-(float) rightRange)+1);
////            score=score*count/leftRange;
////            score=score*count;
//        	
////        	CommonSubsequence=print(map, S1, S2, S1.length(), S2.length());
//            
//            return CommonSubsequence;
//        } 
//        else
//            return CommonSubsequence;
////        int leftIndex=left.get(0);
////        int start=S1.lastIndexOf("。",S1.indexOf(S1.charAt(leftIndex)));
////        int end=S1.indexOf("。",S1.indexOf(S1.charAt(left.get(0))));
////        String commonString=S1.substring(start,end);
//    }


}
