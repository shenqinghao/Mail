<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="inc.tech.persistent.entity.KjDictIdentity"%>
<%@page import="inc.tech.persistent.entity.KjGroupMember1"%>
<%@page import="inc.tech.persistent.entity.Employee,inc.tech.persistent.entity.KjUser"%>
<%@page import="inc.tech.util.ParamUtil"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fckeditor.net/tags-fckeditor" prefix="fck" %>
<%@ include file="/umeditor/jsp/umeditor.jsp"%>

<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
List<KjGroupMember1> researchSecList = (List<KjGroupMember1>)request.getAttribute("researchSecList");
//zz 按照李处指示 邮件、短信功能暂停开发

String mailType = ParamUtil.getAttribute(request,"mailType");
String otherModuleType = ParamUtil.getAttribute(request,"otherModuleType");
String doGiveup = ParamUtil.getAttribute(request,"doGiveup");
String buttonName = "发    送";
if(doGiveup.length()>0) buttonName = "强制放弃";

//zz 2017-10-19 更换富文本编辑框
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>sys/">
    
    <title>发送消息</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
<!--Css+Jquery//-->	
<link rel="StyleSheet" href="<%=basePath%>css/style.css" type="text/css"/>
<script language="JavaScript" type="text/javascript" src="<%=basePath%>js/jquery.pack.js"></script>
<script language="JavaScript" type="text/javascript" src="<%=basePath%>js/cal_util.js"></script>
<script language="javascript">
var um_content;

function hintLength()
{ 
	var content = document.all("content").value;
	var contentLength = DataLength(content);
	var sendSmsCount = parseInt(parseInt(contentLength + 14) / 140 +1);
	var leftCount = 140 - (contentLength + 14)%140;
	var leftCHNCount = parseInt(leftCount/2);
	document.all("sendSmsCount").value = sendSmsCount;
	document.all("left_span").innerHTML = "共计"+contentLength/2+"中文字符，剩余"+leftCHNCount+"中文（"+leftCount+"字符），合计" + sendSmsCount +"条短信";
}
function isSendSms()
{
	if(document.getElementById("sendSmsCheckBox").checked)
	{
		document.all("sendSms").value="true";
		//document.all("content").value="";
		document.all("left_span").style.display = "";
		hintLength();
	}
	else
	{
		document.all("sendSms").value="";
		document.all("sendSmsCount").value="";
		document.all("left_span").innerHTML="";
		//location.reload();
	}
}

function isSendMail()
{
	if(document.getElementById("sendMailCheckBox").checked)
	{
		document.all("sendMail").value="true";
	}
	else
	{
		document.all("sendMail").value="";
	}
}



</script>
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
  </head>
  <body>
 <table width="800" border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#F9FBFD" class="table_gray_all">
 <tr>
 <td>
<html:form action="/mail.do?method=sendMail" method="post" styleId="sysUser">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr bgcolor="#FFFFFF">
    <td class="font_title14" height="32" background="<%=basePath%>images/top_BG.jpg"><table width="200%"  border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td width="10">&nbsp;</td>
        <td width="24"><img src="../images/reg4.gif"></td>
        <td>&nbsp;&nbsp;<span class="font_big_menu">发消息</span></td>
      </tr>
    </table></td>
  </tr>
</table>
<table width="100%" border="1" cellspacing="0">
   <tr valign="middle">
   <input type="hidden" id="mailType" name="mailType" value="${kjMailForm.mailType}"/>
			  	<td class="formlabel">&nbsp;&nbsp;消息名称：&nbsp;&nbsp;</td>
			    <td><input type="text" name="title" size="60" value="${kjMailForm.title}" ></td>
			     <input type="hidden" id="mailId" name="mailId"/>
			     <input type="hidden" id="doGiveup" name="doGiveup" value="<%=doGiveup %>"/>
			  </tr>
			  <tr valign="middle">
			  <input type="hidden" id="fromAddressId" name="fromAddressId" value="${kjMailForm.fromAddressId}"/>
			    <td align="left" class="formlabel">&nbsp;&nbsp;发送人：&nbsp;&nbsp;</td>
			    <td><input type="text" name="fromAddress" size="60" value="${kjMailForm.fromAddress}" readonly="readonly"></td>
			  </tr>
			  <!-- 
			  <tr valign="middle">
			    <td align="left" class="formlabel">&nbsp;&nbsp;发送时间：&nbsp;&nbsp;</td>
			     <td><input type="text" name="date" size="30" value="" readonly="readonly"></td>
			  </tr>
			   -->
			  <logic:equal name="identity"  value="Admin" property="sign" scope="session">
			  <tr valign="middle">
			    <td align="left" class="formlabel">&nbsp;&nbsp;发送类型：&nbsp;&nbsp;</td>
			     <td id="sendTypeTd">
			     <input type="checkbox" id="sendMailCheckBox" onclick="javascript:isSendMail();" checked>发送邮件
			     <input type="hidden" name="sendMail" value="true" id="sendMail"/>
			     
			     <input type="checkbox" id="sendSmsCheckBox" onclick="javascript:isSendSms();">发送短信
			     <input type="hidden" name="sendSms" value="" id="sendSms"/>
			     </td>
			  </tr>
			  </logic:equal>
			   
			   
			  <tr valign="middle">
			  
			  <input type="hidden" id="tableIds" name="tableIds" value="${kjMailForm.tableIds}"/>
			   <input type="hidden" id="secIds" name="secIds" />
			    <td align="left" class="formlabel" colspan="2">&nbsp;&nbsp;收件人列表：&nbsp;&nbsp;</td>
			    <tr>
				    <td colspan="4" class="formbody2">
						<table width="100%" border="0" cellspacing="0" cellpadding="3" class="defaulettable" >
						  <tr align="center" valign="middle" bgcolor="#E3E9F1">
						    <td class="formlabel" width="10%" align="left"><input type='checkbox' onclick="selectAllMail();" id="checkBoxAllMail" checked/>全选</td>
						    <td class="formlabel" width="30%">名称</td>
						    <td class="formlabel" width="20%">邮件收件人</td>
						    <td class="formlabel" width="20%">邮箱地址</td>
						    <td class="formlabel" width="20%">手机<input type="hidden" id="toListIdMail" name="toListIdMail" /></td>
						  </tr>
						  <%
						  List items =(List) request.getAttribute("toList");
						  List names = (List) request.getAttribute("toListName");
						  List idList = (List) request.getAttribute("toListId");
						  List adds = (List) request.getAttribute("toAdd");
						  List smsList = (List) request.getAttribute("toSms");
						  for(int i=0;i<items.size();i++)
						  {
							  if(adds.get(i)!=null)
							  {
								  String itemName =(String)items.get(i);
								  String toName = (String)names.get(i);
								  String add = (String)adds.get(i);
								  String sms = (String)smsList.get(i);
								  String toId = ""; if(idList.size()>i) toId = (String)idList.get(i);
							  
						  %>
						  <tr align="center" valign="middle" bgcolor="#F9FBFD">
						    <td class="table_td" width="10%" align="left"><input type='checkbox' onclick="selectOneMail();" value="<%=toId%>" name="checkBoxMail" checked/></td>
						    <td class="table_td" width="30%"><%=itemName%></td>
						    <td class="table_td" width="20%"><%=toName%></td>
						    <td class="table_td" width="20%"><%=add%></td>
						    <td class="table_td" width="20%"><%=sms%></td>
						  </tr>
						 <%
						 }
						  }
						  %> 
						  
						</table>
				    </td>
				 </tr>
				  <tr valign="middle">
    						<td colspan="4" align="left" class="formlabel">&nbsp;&nbsp;<font color = "red">抄送人：(多个抄送人请用英文逗号隔开)</font></td>
  						 </tr>
  						<tr valign="middle">
  							<td colspan="5" class="formlabel"><textarea cols="141" rows="5" name="CCList"></textarea></td>
  						</tr>
				<logic:equal name="identity"  value="Admin" property="sign" scope="session">
				 <tr valign="middle">
    				<td colspan="5" align="left" class="formlabel">&nbsp;&nbsp;科研秘书<input type='checkbox' onclick="selectAll();" id="checkBoxAll">：</td>
  				</tr> 
	 			<tr>
				    <td colspan="5" class="formbody2">
						<table width="100%" border="0" cellspacing="0" cellpadding="3" class="table_gray_all">
						  <%
								for(int i=0;i<researchSecList.size();i++)
								{
									//maofm 2012/12/13  将kjuser改为employee
									Employee researchSec = researchSecList.get(i).getEmployee();
									// KjUser researchSec = researchSecList.get(i).getKjUser();
									if(researchSec != null)
									{
										if(i%10 == 0)
										{
											out.print("<tr>");
											
										}
									%>
									<td> <input type='checkbox' name="checkbox" id="checkbox_<%=i %>" onclick="setIds();"  value="<%=researchSec.getStaffId()%>"><%=researchSec.getName() %>
									<br>
									<%=researchSec.getEmail() %>
							        </td>
									<%
										if(i%10 == 9)
										{
											out.print("</tr>");
										}
									
									}
								}
						  %> 
						</table>
				    </td>
				 </tr>
			</logic:equal>
			
    <tr valign="middle">
    <td colspan="3" align="left" >&nbsp;&nbsp;<b>消息内容：（如果同时发送短信，请尽量避免消息内容太长）  <span id="left_span" style="display: none;color: red">剩余63中文（126字符）</span></b>
    <input type="hidden" name="sendSmsCount"/>
    </td>
  </tr>
  <tr valign="middle">
    <td colspan="4" class="formbody">
    	<%
    	if(mailType!=null && (mailType.equals("patentFee") || otherModuleType.equalsIgnoreCase("fundFile")))
    	{
    	%>
    	 <html:hidden property="content" value=""/>
    	 <script type="text/plain" id="content_1" style="width:1000px;height:140px;max-height:140px;overflow:auto;">${kjMailForm.content}
    	 </script>  
		<%
    	}
    	else
    	{
    	%>
    	<textarea cols="100" rows="5" name="content" onkeyup="javascript:hintLength();">${kjMailForm.content}</textarea>
    	<%	
    	}
		%>
    </td>
  </tr>  
 </table>
 </html:form> 
 <table width="100%" border="0" cellspacing="0"	cellpadding="0">
		<tr bgcolor="#FFFFFF">
       		<td height="32" align="center" valign="middle" class="formbody">
				<input type="button"  name="sendMailButten" onClick="sendEmail();" value="<%=buttonName %>"/>
				<span id="sendSpan" style="display:none; "> 发送中... ... ...</span>
			</td>
		</tr>
</table>
</body>
</html>
<script language="javascript">

um_content = UM.getEditor('content_1');

	function reloadParent()
	{
		var url = "<%=basePath%>sys/dict.do?method=main&module=sys&actionName=mail&methodName=showMail";
		window.opener.parent.location.href = url;
	}
	
	function sendEmail(){
		
		var sendMail = $('#sendMail').val();
		var sendSms = $('#sendSms').val();
		
		if(sendMail != "true" && sendSms !="true"){
			$('#sendTypeTd').css("border","1px solid red");
			alert("必须选择一种发送类型！邮件或者短信");
			return false;
		}
		
		var content;
		if(um_content) content = um_content.getContent().replace("<br/><br/><br/>","");
		if(document.all("content")) document.all("content").value = content;
		
		//var content = document.all("content").value;
		if(content.length<10)
		{
			if(!window.confirm("消息内容太少，确认发送？"))
			{
				return false;
			}
		}
		// get chaosongren CClist
		var CCList = document.all("CCList").value;
		//get all member which need send mail to
		var toMailMember = document.getElementsByName("checkBoxMail");
		var toListIdMail = "";
		for(var k=0;k<toMailMember.length;k++)
		{
		      if(toMailMember[k].checked)
		      {
			      toListIdMail += toMailMember[k].value + ";";
		      }
		}
		
		document.getElementById("toListIdMail").value = toListIdMail;
		
		var info = "确认发送消息？";
		
		if(document.getElementById("sendSmsCheckBox") && document.getElementById("sendSmsCheckBox").checked)
		{
			var allSendCount = 0;
			var sendSmsCount = document.all('sendSmsCount').value;
			var tableIds = document.all('tableIds').value;
			if(tableIds.length>0) allSendCount = allSendCount + tableIds.split(",").length;
			var secIds = document.all('secIds').value;
			if(secIds.length>0) allSendCount = allSendCount + secIds.split(",").length;
			if(parseInt(allSendCount)>0)
			{
				var allSum = sendSmsCount*allSendCount;
				info = info + ",向"+allSendCount+"人发送共计"+allSum+"条短信？";
			}
			//else info = info + "并发送短信？";
		}
		if(!window.confirm(info))
		{
			return false;
		}
	   
		if(window.opener.document.all('mailCount')){
			var count = window.opener.document.all('mailCount').value;
			count++;
			window.opener.document.all('mailCount').value = count;
		}
		
		document.all("sendMailButten").disabled=true;
		document.all('sendSpan').style.display = "";
		var mailType = document.getElementById("mailType").value;
		if(mailType!=null && mailType == "patentFee"){
			document.forms[0].action="<%=path%>/sys/mail.do?method=sendMailEach";
		}else{
			document.forms[0].action="<%=path%>/sys/mail.do?method=sendMail";
		}
		document.forms[0].submit();
		
}
function setIds()
{
	var selectedCount = 0;
	var selectedSec="";
	for(i=0; i<35; i++)
		{
			if(document.getElementById("checkbox_"+i) )
			{
				if(document.getElementById("checkbox_"+i).checked)
				{
					if(selectedCount>0)
					{	
						selectedSec += ",";
					}
					selectedSec += document.getElementById("checkbox_"+i).value;
					selectedCount++;
				}
			}
		}
		document.all("secIds").value = selectedSec;
}
function selectAll()
{
if(document.getElementById("checkBoxAll").checked)
		{
			for(i=0; i<35; i++)
			{
				if(document.getElementById("checkbox_"+i) )
				{
					document.getElementById("checkbox_"+i).checked = true;
				}
			}
		}
		else
		{
			for(i=0; i<35; i++)
			{
				if(document.getElementById("checkbox_"+i) )
				{
					document.getElementById("checkbox_"+i).checked = false;
				}
			}
		}
		setIds();
}

function selectAllMail()
{
    var allChoice = document.getElementsByName("checkBoxMail");
    if(document.getElementById("checkBoxAllMail").checked)
    {      
        for(var i=0;i<allChoice.length;i++)
        {
            allChoice[i].checked = true;
        }
    }
    else 
    {
       for(var i=0;i<allChoice.length;i++)
       {
            allChoice[i].checked = false;
       }
    }
}
function selectOneMail()
{
    var allChoice = document.getElementsByName("checkBoxMail");
    var flag = 1;
    for(var i=0;i<allChoice.length;i++)
    {
       if(!allChoice[i].checked)
          flag=0;
    }
   if(flag==1)
    document.getElementById("checkBoxAllMail").checked = true;
   else 
    document.getElementById("checkBoxAllMail").checked = false;
}
</script>

