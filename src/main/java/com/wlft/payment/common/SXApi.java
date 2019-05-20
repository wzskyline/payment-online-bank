package com.wlft.payment.common;

import com.wlft.payment.exception.HttpException; 
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SXApi {

    private static Logger logger = Logger.getLogger(SXApi.class);

    public static HttpClient httpClient = new HttpClient();

    public static final String PREFIX = Config.get("sx.payment.system.origin");
    //  登入payment 1.0
    private static String LOGIN_API = PREFIX + "/aLogin!login.do";
    //  登出payment 1.0
    private static String LOGOUT_API = PREFIX + "/aLogin!logout.do";
    //  取得銀行卡列表
    private static String PAYMENT_ACC_SEARCH = PREFIX + "/adminPaymentAcct!search.do";
    //  取得銀行卡明細
    private static String PAYMENT_ACC_LOAD = PREFIX + "/adminAcct!load.do";
    //  取得代理
    private static String PAYMENT_ACC_LOAD_PROXY_ASSIGNMENT = PREFIX + "/adminAcct!loadProxyAssignment.do";
    //  取得提現任務列表
    private static String PAYMENT_ACC_LIST_WITHDRAW = PREFIX + "/adminWF!listPaymentTask.do";
    //  取得自己任務清單
    private static String PAYMENT_WF_LIST_ASSIGNED_TASK = PREFIX + "/adminWF!listAssignedTask.do";
    //  取得所有任務清單
    private static String PAYMENT_WF_LIST_PAYMENT_TASK = PREFIX + "/adminWF!listPaymentTask.do";
    //  取得某任務的明細 （外部转）
    private static String PAYMENT_WF_LOAD_WITHDRAW_DISTRIBUTION_DETAIL = PREFIX + "/adminWF!loadPartialWithdrawPayment.do";  // loadPartialWithdrawPayment  // loadWithdrawDistributionDetails
    //  更新任務
    private static String PAYMENT_WF_UPDATE_TASK = PREFIX + "/adminWF!updateTask.do";
    // 领任务
    private static String PAYMENT_WF_CLAIM_TASK = PREFIX + "/adminWF!claimTask.do";
    //  取得某任務的明細 (内部转)
    private static String PAYMENT_WF_LOAD_TRANS = PREFIX + "/adminWF!loadTrans.do";
    //  確認任務是否有被領走
    private static String PAYMENT_WF_CHECK_FOR_PAYMENT_TASK_ASSIGNEE = PREFIX + "/adminWF!checkForPaymentTaskAssignee.do";

    
    // 验证码服务商 2captcha
    private static String URL_IN_2CAPTCHA = "http://2captcha.com/in.php";
    private static String URL_RES_2CAPTCHA = "http://2captcha.com/res.php";
    private static String KEY_2CAPTCHA  = "5e509a9dd13eced8ea4bfadd111c0944";
    
    public static void login(Operator operator) throws HttpException{
        logger.debug("Request:" + LOGIN_API);
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(LOGIN_API);
        NameValuePair[] data = {new NameValuePair("username", operator.getUsername()), new NameValuePair("password", operator.getPassword())};
        String res = null;
        StringBuffer sb = new StringBuffer();
        JSONObject json;

        try {
            postMethod.setRequestBody(data);
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            //  response status
            int statusCode = httpClient.executeMethod(postMethod);
            //  response
            res = postMethod.getResponseBodyAsString();
            //  cookie
            Cookie[] cookies = httpClient.getState().getCookies();

            //  Check status
            if(statusCode == 200){
                json = new JSONObject(res);
                int code = json.getInt("code");

                //  Check code
                logger.debug("code:" + code);
                if(code == 1){
                    for (Cookie c : cookies) {
                        sb.append(c.toString() + ";");
                    }
                    operator.setCookie(sb.toString());
                    operator.setData(json);
                }else{
                    throw new HttpException(json.getString("message"), code);
                }
            }else{
                throw new HttpException(res, statusCode);
            }
        }catch (HttpException e){
            logger.error(e);
            throw e;
        }catch (Exception e){
            logger.error(e);
            throw new HttpException("Network Fail!", -1);
        }
    }

    public static void logout(Operator operator) throws HttpException{
        Map<String,String> params = new HashMap<String,String>();
        params.put("_dc",System.currentTimeMillis() + "");

        HttpUtils.get(LOGOUT_API, params, operator.getCookie());
    }

    public static JSONArray getCardList (Operator operator) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("a.group.id", null),
                new NameValuePair("a.bank.id", null),
                new NameValuePair("a.batch.id", null),
                new NameValuePair("a.type.id", null),
                new NameValuePair("a.acctCode", null),
                new NameValuePair("searchingType", "")
                //new NameValuePair("searchingType", "withdrawal")
        };

        String res = HttpUtils.post(PAYMENT_ACC_SEARCH, data, operator.getCookie());
        return new JSONObject(res).getJSONArray("data");
    }

    public static JSONObject getCardDetail (Operator operator, Integer id) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("a.id", id.toString())
        };

        String res = HttpUtils.post(PAYMENT_ACC_LOAD, data, operator.getCookie());
        return new JSONObject(res).getJSONObject("data");
    }

    public static JSONArray getProxySetting (Operator operator, Integer id) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("acctId", id.toString())
        };

        String res = HttpUtils.post(PAYMENT_ACC_LOAD_PROXY_ASSIGNMENT, data, operator.getCookie());
        return new JSONObject(res).getJSONArray("data");
    }

    public static JSONArray getWithdrawList(Operator operator) throws HttpException{
        Map<String,String> params = new HashMap<String,String>();

        params.put("_dc",System.currentTimeMillis()+"" );
        params.put("wfs","WP");
        params.put("page","1");
        params.put("start","0");
        params.put("limit","500");

        String res = HttpUtils.get(PAYMENT_ACC_LIST_WITHDRAW, params, operator.getCookie());
        return new JSONObject(res).getJSONArray("data");
    }

    public static JSONArray getAssignedTask(Operator operator) throws HttpException{
        Map<String,String> params = new HashMap<String,String>();

        params.put("_dc",System.currentTimeMillis()+"" );
        params.put("page","1");
        params.put("start","0");
        params.put("limit","500");

        String res = HttpUtils.get(PAYMENT_WF_LIST_ASSIGNED_TASK, params, operator.getCookie());
        return new JSONObject(res).getJSONArray("data");
    }

    public static JSONArray getPaymentTask(Operator operator) throws HttpException{
        Map<String,String> params = new HashMap<String,String>();

        params.put("_dc",System.currentTimeMillis()+"" );
        params.put("wfs[]","WP");
        params.put("page","1");
        params.put("start","0");
        params.put("limit","500");

        String res = HttpUtils.get(PAYMENT_WF_LIST_PAYMENT_TASK, params, operator.getCookie());
        return new JSONObject(res).getJSONArray("data");
    }

    public static JSONObject loadWithdrawDistributionDetails (Operator operator, BigDecimal id, String ref) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("Task.id", id.toString()),
                new NameValuePair("Task.ref", ref),
                new NameValuePair("Task.state.state",null),
                new NameValuePair("bankId", null)
        };

        String res = HttpUtils.post(PAYMENT_WF_LOAD_WITHDRAW_DISTRIBUTION_DETAIL, data, operator.getCookie());
        System.out.println(res);

        return new JSONObject(res).getJSONObject("data");
    }

    public static String updateTask (Operator operator, String bankFree,String taskId, String taskRef, String state, String remark ) throws HttpException{
        bankFree = bankFree.trim();
        NameValuePair[] data = {
                new NameValuePair("task.field2",  bankFree),
                new NameValuePair("Task.id",  taskId),
                new NameValuePair("Task.ref", taskRef),
                new NameValuePair("Task.state.state",state),
                new NameValuePair("Task.remark", remark),
                new NameValuePair("messageSending", " false")
        };

        String res = HttpUtils.post(PAYMENT_WF_UPDATE_TASK, data, operator.getCookie());
        return new JSONObject(res).getString("message");
    }

    public static String updateTask2 (Operator operator, String field3,String field4,String taskId, String taskRef, String state, String remark, String bankFree, String amount) throws HttpException{
        amount = amount.replaceAll("$","");
        NameValuePair[] data = {
                new NameValuePair("task.field7",  amount),
                new NameValuePair("task.field8",  bankFree),
                new NameValuePair("task.additionalInfo0",  bankFree),
                new NameValuePair("task.field3",  field3),
                new NameValuePair("task.field4",  field4),  // N
                new NameValuePair("Task.id",  taskId),
                new NameValuePair("Task.ref", taskRef),
                new NameValuePair("Task.state.state",state), // Y  F
                new NameValuePair("Task.remark", remark),
                new NameValuePair("messageSending", "false"),
        };

        System.out.println(Arrays.toString(data));
        String res =    HttpUtils.post(PAYMENT_WF_UPDATE_TASK, data, operator.getCookie()); // by test  = "{message:'success'}"; //
        return new JSONObject(res).getString("message");
    }

    public static String claimTask (Operator operator, String id,String taskName) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("task.id",  id),
                new NameValuePair("taskName",  taskName),
        };

        String res = HttpUtils.post(PAYMENT_WF_CLAIM_TASK, data, operator.getCookie());
        return new JSONObject(res).getBigDecimal("code").toString();
    }

    public static JSONObject loadTrans(Operator operator, BigDecimal id, String ref) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("task.id",  id.toString()),
                new NameValuePair("task.ref",  ref),
        };

        String res = HttpUtils.post(PAYMENT_WF_LOAD_TRANS, data, operator.getCookie());
        return new JSONObject(res).getJSONObject("data");
    }

    public static Boolean checkTaskAssignee(Operator operator, BigDecimal id) throws HttpException{
        NameValuePair[] data = {
                new NameValuePair("task.id",  id.toString())
        };

        String res = HttpUtils.post(PAYMENT_WF_CHECK_FOR_PAYMENT_TASK_ASSIGNEE, data, operator.getCookie());
        return new JSONObject(res).getBoolean("data");
    }
    
    public static String  sendBase64To2captcha(String imageBase64) throws HttpException {
    	
    	  String codeId = "";
    	  NameValuePair[] data = {
                  new NameValuePair("method",  "base64"),
                  new NameValuePair("key",  KEY_2CAPTCHA),
                  new NameValuePair("body",  imageBase64)
          };

          String res = HttpUtils.post(URL_IN_2CAPTCHA, data, "");
          if(res.indexOf("|")>0) {
        	  codeId = res.split("\\|")[1];  
          }
          
    	return codeId;
    } 
   public static String  getCodeFrom2captcha(String codeId) throws HttpException {
    
    	  String code = "";
          String res = HttpUtils.sendGet(URL_RES_2CAPTCHA, "key="+KEY_2CAPTCHA+"&"+"action=get&id="+codeId);
        
          if(res.indexOf("|")>0) {
        	  code = res.split("\\|")[1];  
          } 
    	return code;
    } 
   public static void main(String[] args) throws Exception {
	   
	   System.out.println(getCodeFrom2captcha("61343890712")); //xhe3
 	  }
    
}
