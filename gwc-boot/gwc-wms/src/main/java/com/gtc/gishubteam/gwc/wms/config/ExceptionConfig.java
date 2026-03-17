package cn.geoair.geoairteam.gwc.wms.config;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import cn.geoair.base.data.result.GiResult;
import cn.geoair.base.exception.GirException;
import cn.geoair.base.exception.*;
import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;

@ControllerAdvice
public class ExceptionConfig  implements ApplicationRunner {

	
	
	protected final GiLogger logger = GirLogger.getLoger(ExceptionConfig.class);


	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		
		//GwExceptionResultConverter.regExceptionCode(GwValidateException.class, 499);
		//GwExceptionResultConverter.regExceptionAlertType(GwValidateException.class, GwEmAlertType.无需关闭的错误3.value());
	}
	
	
	
	/** 验证异常*/
	/*
	@ExceptionHandler(value = BindException.class)
	@ResponseBody
	public GwResult<String> BindExceptionHandler(HttpServletResponse response, BindException ex) {
		
		List<FieldError> fieldErrors = ex.getFieldErrors();
		StringBuffer msg = new StringBuffer();
		for(FieldError fieldError : fieldErrors) {
		//FieldError fieldError = ex.getFieldError();
			String dmsg = fieldError.getDefaultMessage();
			if (dmsg != null && dmsg.contains("java.lang.NumberFormatException")) {
				Pattern p = Pattern.compile("for property '.+'");
				Matcher m = p.matcher(dmsg);
				msg.append("参数格式错误");
				if (m.find()) {
					String group = m.group();
					msg.append(":").append(group);
				}
			}else {
				msg.append(dmsg).append(";");
			}
		}
		GwException extraException = new GwException(msg.toString(),ex);
		return gtcExceptionHandler(response,extraException);
	} 

	数据库记录重复异常 
	@ExceptionHandler(value = DuplicateKeyException.class)
	@ResponseBody
	public DataResult DuplicateKeyExceptionHandler(HttpServletResponse response, DuplicateKeyException ex) {
		RxException extraException = new RxException();
		//MySQLIntegrityConstraintViolationException mcv = (MySQLIntegrityConstraintViolationException) ex.getCause();
		//String msg = mcv.getMessage();
		//msg = msg.substring("Duplicate entry".length(), msg.lastIndexOf("for key"));
		//msg += " 已存在";
		extraException.setAlertMsg("记录重复");
		extraException.setData(StackTraceMsg(ex));
		response.setStatus(extraException.getCode());
		return extraException;
	}
	
	 上传文件过大异常 
	@ExceptionHandler(value = MaxUploadSizeExceededException.class)
	@ResponseBody
	public GwResult<String> DuplicateKeyExceptionHandler(HttpServletResponse response, MaxUploadSizeExceededException ex) {
		long fs = ex.getMaxUploadSize();
		String mb = (fs == 0 ? 0 : fs / 1024 / 1024) + "MB";
		GwException extraException = new GwException("上传文件应不大于" + mb,ex);
		return gtcExceptionHandler(response,extraException);
	}
	*/
	
	
	//是否改变http状态码
	public static boolean changeResponseStatus = false;
	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public GiResult<Exception> exceptionHandler(HttpServletResponse response, Exception ex) {
		
		GiResult<Exception> res= GirExceptionResultConverter.convert(ex);
		
		if(changeResponseStatus) {
			response.setStatus(res.code());
		}
		logger.error(ex);
		return res;
	}
	
	/** 其他异常 或者 Error */
	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public GiResult<Exception> throwableHandler(HttpServletResponse response, Throwable ex) {

		GirException extraException = new GirException("系统发生错误:"+ex.getMessage(),ex);
		return exceptionHandler(response,extraException);
	}




}
