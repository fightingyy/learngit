package com.ld.IO.File;

import java.util.Properties;
import org.elasticsearch.index.engine.Engine.Get;
import org.python.antlr.PythonParser.return_stmt_return;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：PythonFile   
* 类描述：  java调用python脚本中的函数
* 创建人：ludan   
* 创建时间：2017年4月30日 上午10:21:34   
* @version        
*/
public class PythonFile {

	public PythonFile() {
		// TODO Auto-generated constructor stub
	}
	
	public static PyFunction getPyFuntion(String path,String function){
		Properties props = new Properties();
        props.put("python.console.encoding", "UTF8"); // Used to prevent: console: Failed to install '': java.nio.charset.UnsupportedCharsetException: cp0.
        props.put("python.security.respectJavaAccessibility", "false"); //don't respect java accessibility, so that we can access protected members on subclasses
        props.put("python.import.site","false");
        Properties preprops = System.getProperties();
        PythonInterpreter.initialize(preprops, props, new String[0]);
        PySystemState sys = Py.getSystemState();  
        if(path.contains("home")){
        	sys.path.add("/home/keg/qa/jython/Lib");
	        sys.path.add("/home/keg/qa/jython/Lib/site-packages");	  
	        System.out.println(path);
        }
        else{
	        sys.path.add("F:\\jython2.5.4rc1\\Lib");                       
	        sys.path.add("F:\\jython2.5.4rc1\\Lib\\site-packages");		   
        }
     
		PythonInterpreter interpreter = new PythonInterpreter(); 
		
        interpreter.execfile(path);  
        PyFunction func = (PyFunction)interpreter.get(function,PyFunction.class);
        
        return func;
        
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
