<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="inc.tech.persistent.entity.KjMail"%>
<%@ page import="inc.tech.user.dao.KjUserDAO"%>
<%@ page import="inc.tech.sys.mail.dao.KjMailDAO"%>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
try{
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	String sendMailStatusString="";
	KjMail kjMail=null;
	String sendSmsStatusString="";
	if(request.getAttribute("kjMail")!=null)
	{
	  kjMail = (KjMail)request.getAttribute("kjMail");
	}
	if(request.getAttribute("sendMailStatusString")!=null)
	  sendMailStatusString = (String)request.getAttribute("sendMailStatusString");
	if(request.getAttribute("sendSmsStatusString")!=null)
      sendSmsStatusString = (String)request.getAttribute("sendSmsStatusString");
	String[] sendMailStatusList = null;
	if(sendMailStatusString!=null && sendMailStatusString.length()>0){
		sendMailStatusList = sendMailStatusString.split(",");
	}
	else if(kjMail!=null)
	{
		if(kjMail.getSendMailStatus()!=null) sendMailStatusList = kjMail.getSendMailStatus().split(",");
	}
	
	String[] sendSmsStatusList =null;
	if(sendSmsStatusString!=null && sendSmsStatusString.length()>0){
		sendSmsStatusList = sendSmsStatusString.split(",");
	}
	else if(kjMail!=null)
	{
		if(kjMail.getSendSmsStatus()!=null) sendSmsStatusList = kjMail.getSendSmsStatus().split(",");
	}
	String mailIds = "";
	if(request.getAttribute("mailId")!=null)
		mailIds = (String)request.getAttribute("mailId");
	KjMail AKjMail = KjMailDAO.getInstance().findByPk(Long.valueOf("195"));
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>project/">   
    <title>My JSP 'DetailCommission.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
<!--Css+Jquery//-->	
<link rel="StyleSheet" href="<%=basePath%>css/style.css" type="text/css"/>
<script language="JavaScript" type="text/javascript" src="<%=basePath%>js/jquery.pack.js"></script>
<!--Css+Jquery//-->	
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"><style type="text/css">
<!--
body {
	background-color: #E3E9F1;
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style>
  <link href="../css/css.css" rel="stylesheet" type="text/css">
  </head>
  <body>
 <table width="800" border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#F9FBFD" class="table_gray_all">
 <tr>
 <td>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr bgcolor="#FFFFFF">
    <td class="font_title14" height="32" background="<%=basePath%>images/top_BG.jpg"><table width="200%"  border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td width="10">&nbsp;</td>
        <td width="24"><img src="../images/reg4.gif"></td>
        <td>&nbsp;&nbsp;<span class="font_big_menu">消息详细信息</span></td>
      </tr>
    </table></td>
  </tr>
</table>

<%
if(mailIds != "")
{
	  List items =(List) request.getAttribute("items");
	  List adds = (List) request.getAttribute("toAdd");
	  List CCAdds = (List) request.getAttribute("toCCAdd");
	  List smsList = (List) request.getAttribute("toSms");
	  List userList = (List) request.getAttribute("userList");
	  String[] mailId = mailIds.split(",");
	  for(int t=0;t<mailId.length;t++)
	  {
		  String tempItems = items.get(t).toString();
		  String[] tempAdds = adds.get(t).toString().split(",");
		  String[] tempSmsList = smsList.get(t).toString().split(",");
		  String[] tempUserList = userList.get(t).toString().split(",");
		  
		  KjMail tempKjMail = KjMailDAO.getInstance().findByPk(Long.valueOf(mailId[t]));
		  
		  String[] sendMailStatusListTemp = null ;
		  String[] sendSmsStatusListTemp = null;
		  if(tempKjMail.getSendMailStatus()!=null)
		     sendMailStatusListTemp = tempKjMail.getSendMailStatus().split(",");
		  if(tempKjMail.getSendSmsStatus()!=null)
		   sendSmsStatusListTemp = tempKjMail.getSendSmsStatus().split(",");
	      
		  if(t>0)
		  {
			  %>
			  <br/>
			  <br/>
			  <hr/>
			  <%
		  }
		  
		  %>	  
		  <table width="100%" border="0" cellspacing="0">
			  <tr valign="middle">
			    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;标题：&nbsp;&nbsp;</td>
			    <td colspan="3" class="formbody"><%=tempKjMail.getTitle() %></td>
			  </tr>
			 <tr valign="middle">
			    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;创建人：&nbsp;&nbsp;</td>
			    <td colspan="3" class="formbody"><%=tempKjMail.getKjUser().getName() %></td>
			  </tr><tr valign="middle">
			    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;发送时间：&nbsp;&nbsp;</td>
			    <td colspan="3" class="formbody"><fmt:formatDate value="<%=tempKjMail.getCreateDate()%>"  pattern="yyyy-MM-dd HH:mm"/></td>
			  </tr><tr valign="middle">
			    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;内容：&nbsp;&nbsp;</td>
			    <td colspan="3" class="formbody"><%=tempKjMail.getContent() %></td>
			  </tr>
			
			  <%
			  if("Admin".equals(request.getAttribute("sign")))
			  {
			  %>
			  <tr valign="middle">
			    <td align="left" class="formlabel">&nbsp;&nbsp;收件人列表：&nbsp;&nbsp;</td>
				   <tr>
				    <td colspan="6" class="formbody2">
						<table width="100%" border="0" cellspacing="0" cellpadding="3" class="table_gray_all">
						  <tr align="center" valign="middle" bgcolor="#E3E9F1">
						    <td class="formlabel">名称</td>
						    <td class="formlabel">收件人</td>
						    <td class="formlabel">邮箱地址</td>
						    <td class="formlabel">抄送地址</td>
						    <td class="formlabel">手机</td>
						    <td class="formlabel">邮件发送情况</td>
						    <td class="formlabel">短信发送情况</td>
						  </tr>
						  <%
						  String itemName =tempItems;
						  for(int i=0;i<tempUserList.length && i<tempAdds.length ;i++)
						  {
							  
							  String name = KjUserDAO.getInstance().findByPk((String)tempUserList[i]).getName();
							  String add = (String)tempAdds[i];
							  String sms = (String)tempSmsList[i];
							  String mailStatus="无";
							  if(sendMailStatusListTemp!=null && sendMailStatusListTemp[i]!=null){
								  mailStatus = sendMailStatusListTemp[i];
							  }
							  String smsStatus="无";
							  if(sendSmsStatusListTemp!=null && sendSmsStatusListTemp[i]!=null){
								  smsStatus = sendSmsStatusListTemp[i];
							  }
						  %>
						  <tr align="center" valign="middle" bgcolor="#F9FBFD">
						    <td class="formlabel">&nbsp;<%=itemName%></td>
						    <td class="formlabel">&nbsp;<%=name%></td>
						    <td class="formlabel">&nbsp;<%if(add!=null){%><%=add%><%}else{ %>无<%} %></td>
						    <td class="formlabel">&nbsp;<%if(CCAdds.size()>0){for(int j =0; j<CCAdds.size();j++){%><%=CCAdds.get(j)%><%}}else{ %>无<%} %></td>
						    <td class="formlabel">&nbsp;<%if(sms!=null){%><%=sms%><%}else{ %>无<%} %></td>
						    <td class="formlabel"><%=mailStatus %> </td>
						    <td class="formlabel"><%=smsStatus %> </td>
						    <%	
						    }
						    %>
						  </tr>
						  </table>
				    </td>
				 </tr>
		  <% 
		  }
		  %>
		 </table>
		 <%  
		  
	  }
}
else{
%>

<table width="100%" border="0" cellspacing="0">
  <tr valign="middle">
    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;标题：&nbsp;&nbsp;</td>
    <td colspan="3" class="formbody">${AMail.title }</td>
  </tr>
 <tr valign="middle">
    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;创建人：&nbsp;&nbsp;</td>
    <td colspan="3" class="formbody">${AMail.kjUser.name}</td>
  </tr><tr valign="middle">
    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;发送时间：&nbsp;&nbsp;</td>
    <td colspan="3" class="formbody"><bean:write name="AMail" property="createDate" scope="request" format="yyyy-MM-dd HH:mm"/></td>
  </tr><tr valign="middle">
    <td width="150"  align="left" class="formlabel" >&nbsp;&nbsp;内容：&nbsp;&nbsp;</td>
    <td colspan="3" class="formbody">${AMail.content }</td>
  </tr>

  <%
  if("Admin".equals(request.getAttribute("sign")))
  {
  %>
  <tr valign="middle">
    <td align="left" class="formlabel">&nbsp;&nbsp;收件人列表：&nbsp;&nbsp;</td>
			    <tr>
				    <td colspan="6" class="formbody2">
						<table width="100%" border="0" cellspacing="0" cellpadding="3" class="table_gray_all">
						  <tr align="center" valign="middle" bgcolor="#E3E9F1">
						    <td class="formlabel">名称</td>
		 				    <td class="formlabel">收件人</td>	    
						    <td class="formlabel">邮箱地址</td>
						    <td class="formlabel">抄送地址</td>
						    <td class="formlabel">手机</td>
						    <td class="formlabel">邮件发送情况</td>
						    <td class="formlabel">短信发送情况</td>
						  </tr>
						  <%
						  List items =(List) request.getAttribute("items");
						  List adds = (List) request.getAttribute("toAdd");
						  List CCAdds = (List) request.getAttribute("toCCAdd");
						  List smsList = (List) request.getAttribute("toSms");
						  List userList = (List) request.getAttribute("userList");
						  String CC = "";
	  					  for(int i = 0;i <CCAdds.size();i++){
	  						CC+=CCAdds.get(i)+",";
	  					  }
						  for(int i=0;i<items.size() && i<adds.size() ;i++)
						  {
							  String itemName =(String)items.get(i);
							  String name = KjUserDAO.getInstance().findByPk((String)userList.get(i)).getName();
							  String add = (String)adds.get(i);
							  String sms = (String)smsList.get(i);
							  String mailStatus="无";
							  if(sendMailStatusList!=null && sendMailStatusList[i]!=null){
								  mailStatus = sendMailStatusList[i];
							  }
							  String smsStatus="无";
							  if(sendSmsStatusList!=null && sendSmsStatusList[i]!=null){
								  smsStatus = sendSmsStatusList[i];
							  }
						  %>
						  <tr align="center" valign="middle" bgcolor="#F9FBFD">
						    <td class="formlabel">&nbsp;<%=itemName%></td>
						    <td class="formlabel">&nbsp;<%=name%></td>
						    <td class="formlabel">&nbsp;<%if(add!=null){%><%=add%><%}else{ %>无<%} %></td>
						    
						  <td class="formlabel">&nbsp;<%if(CCAdds.size()>0){for(int j =0; j<CCAdds.size();j++){%><%=CCAdds.get(j)%></br><%}}else{ %>无<%} %></td>
						    <td class="formlabel">&nbsp;<%if(sms!=null){%><%=sms%><%}else{ %>无<%} %></td>
						    <td class="formlabel"><%=mailStatus %> </td>
						    <td class="formlabel"><%=smsStatus %> </td>
						    <%	
						    }
						    %>
						  </tr>
						  </table>
				    </td>
				 </tr>
  <% 
  }
  %>
 </table>
  <% 
  }
  %>
  </body>
</html>

<%
}
catch(Exception e)
{
	e.printStackTrace();
	}
%>
