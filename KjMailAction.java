package inc.tech.sys.mail.action;

import inc.tech.apply.dao.ApplyDAO;
import inc.tech.auth.dao.AuthDAO;
import inc.tech.fund.fundAllot.dao.FundAllotDAO;
import inc.tech.fund.fundFile.dao.FundFileDAO;
import inc.tech.patent.action.KjPatentFeeAction;
import inc.tech.patent.dao.KjPatentFeeDao;
import inc.tech.patent.dao.KjPatentGiveUpDao;
import inc.tech.patent.dao.KjProjectStaffDao;
import inc.tech.persistent.DAOException;
import inc.tech.persistent.entity.Employee;
import inc.tech.persistent.entity.KjApply;
import inc.tech.persistent.entity.KjDictIdentity;
import inc.tech.persistent.entity.KjFundAllot;
import inc.tech.persistent.entity.KjFundFile;
import inc.tech.persistent.entity.KjGroupMember1;
import inc.tech.persistent.entity.KjInvoice;
import inc.tech.persistent.entity.KjMail;
import inc.tech.persistent.entity.KjMeetingFund;
import inc.tech.persistent.entity.KjPatent;
import inc.tech.persistent.entity.KjPatentFee;
import inc.tech.persistent.entity.KjProject;
import inc.tech.persistent.entity.KjProjectstaff;
import inc.tech.persistent.entity.KjSnsMeetingBase;
import inc.tech.persistent.entity.KjSnsMeetingUser;
import inc.tech.persistent.entity.KjStudentAuth;
import inc.tech.persistent.entity.KjTableTemplate;
import inc.tech.persistent.entity.KjThesis;
import inc.tech.persistent.entity.KjUser;
import inc.tech.process.dao.ProjectDao;
import inc.tech.project.dao.KjInvoiceDao;
import inc.tech.project.dao.KjMeetingFundDao;
import inc.tech.project.dao.KjPatentDao;
import inc.tech.project.dao.KjThesisDao;
import inc.tech.social.dao.KjSnsMeetingBaseDAO;
import inc.tech.social.dao.KjSnsMeetingUserDAO;
import inc.tech.sys.group.dao.EmployeeDAO;
import inc.tech.sys.group.dao.MemberDAO;
import inc.tech.sys.init.TechSystem;
import inc.tech.sys.mail.dao.KjMailDAO;
import inc.tech.sys.mail.form.KjMailForm;
import inc.tech.sys.privilege.Privilege;
import inc.tech.sys.table.dao.KjTableTemplateDao;
import inc.tech.sys.user.UserBean;
import inc.tech.user.dao.KjUserDAO;
import inc.tech.util.DateUtil;
import inc.tech.util.PaginalBean;
import inc.tech.util.ParamUtil;
import inc.tech.util.SmsUtil;
import inc.tech.util.StringUtil;
import inc.tech.util.SysConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.xml.sax.SAXException;




public class KjMailAction extends DispatchAction{
	private static String MENU_SIGN="Project";	//项目管理
	private static String CAUDIT="CAudit";	//学院审核权限
	private static String UAUDIT="UAudit";	//学校审核权限
	public ActionForward showMail(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws DAOException {
		Privilege.privilege(request,MENU_SIGN,CAUDIT);
		Privilege.privilege(request,MENU_SIGN,UAUDIT);
		KjMailDAO dao=KjMailDAO.getInstance();
	    
	    HttpSession session=request.getSession();
	    
	    String sqlWhere = " as kj where kj.deleteMark='0' and ";
	    String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
		UserBean userbean = (UserBean) session.getAttribute("UserBean");
		String staffId = userbean.getUid();
		if(!"Admin".equals(sign))
		{
			sqlWhere = sqlWhere + "kj.kjUser='"+staffId+"'  ";
		}
		else
		{
			sqlWhere = sqlWhere + " 1=1 ";
		}
		sqlWhere = sqlWhere + "  order by mailId desc ";
	    
		session.setAttribute("sqlWhere",sqlWhere);
		
		int currentCount=SysConfig.START_COUNT;
		if(request.getParameter("page")!=null)
		currentCount=Integer.parseInt((request.getParameter("page")));
		int sumCount=dao.getCount(sqlWhere);
		PaginalBean paginalbean=new PaginalBean(currentCount,sumCount,SysConfig.PAGE_SIZE);
		request.setAttribute("mailList", dao.getPaginalList(paginalbean.getCurrentCount(), SysConfig.PAGE_SIZE,sqlWhere));
		request.setAttribute("PaginalBean", paginalbean);
	
		return new ActionForward("/mail/showMail.jsp");
		
	}
	public ActionForward showReceiveMail(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws DAOException {
		Privilege.privilege(request,MENU_SIGN,CAUDIT);
		Privilege.privilege(request,MENU_SIGN,UAUDIT);
		KjMailDAO dao=KjMailDAO.getInstance();
	    KjUserDAO Udao = KjUserDAO.getInstance();
	    
	    HttpSession session=request.getSession();
	    
	    String sqlWhere = " as kj where kj.deleteMark='0' and ";
	    String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
		UserBean userbean = (UserBean) session.getAttribute("UserBean");
		String staffId = userbean.getUid();
		String address = Udao.getAddress(staffId);
		sqlWhere = sqlWhere + "kj.toAddress like '%"+address+"%'  " + "order by mailId desc ";
	    
		session.setAttribute("sqlWhere",sqlWhere);
		
		int currentCount=SysConfig.START_COUNT;
		if(request.getParameter("page")!=null)
		currentCount=Integer.parseInt((request.getParameter("page")));
		int sumCount=dao.getCount(sqlWhere);
		PaginalBean paginalbean=new PaginalBean(currentCount,sumCount,SysConfig.PAGE_SIZE);
		request.setAttribute("mailList", dao.getPaginalList(paginalbean.getCurrentCount(), SysConfig.PAGE_SIZE,sqlWhere));
		request.setAttribute("PaginalBean", paginalbean);
	
		return new ActionForward("/mail/showReceiveMail.jsp");
	}
	
	/**
	 * 张仲：2013-08-19 根据邮件和模块，显示设计模块的数据<br>
	 * @param request
	 * @param kjMail
	 */
	public void parseMail(HttpServletRequest request,KjMail kjMail)
	{
		String module = kjMail.getMailType();
	    String idList = kjMail.getTableIds();
	  
	    List<String> tableNameList=new ArrayList<String>();
	    List<String> toSms = new ArrayList<String>();
	    List<String> toAdd = new ArrayList<String>();
	    List<String> userList = new ArrayList<String>();
	    KjUserDAO kjUserDao = KjUserDAO.getInstance();
	    KjUser kjUser = null;
	    
	    if(idList!=null && idList.length()>0)
	    {
	    	  String[] ids = idList.split(",");
	    	  for(int i=0;i<ids.length;i++){
			    	if("project".equals(module))
					{
			    		ProjectDao projectDao = null;
						KjProject kjProject = projectDao.getInstance().findByPk(Long.parseLong(ids[i]));
			    		tableNameList.add(kjProject.getProjectName());
			    		
			    		if(kjProject.getEmployeeByLeaderId()!=null && kjProject.getEmployeeByLeaderId().getName()!=null)
						{
							kjUser = kjUserDao.findByPk(kjProject.getEmployeeByLeaderId().getStaffId());
						}
						else if(kjProject.getKjUser()!=null)
						{
							kjUser = kjProject.getKjUser();
						}
					}
			    	else if("allot".equals(module))
					{
			    		FundAllotDAO allotDao = null;
						KjFundAllot allot = allotDao.getInstance().findByPk(Long.parseLong(ids[i]));
			    		tableNameList.add(allot.getKjProject().getProjectName());
			    		
			    		if(allot.getKjProject().getEmployeeByLeaderId()!=null  && allot.getKjProject().getEmployeeByLeaderId().getName()!=null)
						{
							kjUser = kjUserDao.findByPk(allot.getKjProject().getEmployeeByLeaderId().getStaffId());
						}
						else if(allot.getKjProject().getKjUser()!=null)
						{
							kjUser = allot.getKjProject().getKjUser();
						}
					}
					else if("invoice".equals(module))
					{
						KjInvoiceDao kjInvoiceDao = null;
						KjInvoice kjInvoice = kjInvoiceDao.getInstance().findByPk(Long.parseLong(ids[i]));
						tableNameList.add(kjInvoice.getKjProject().getProjectName());
						if(kjInvoice.getInvoiceUser()!=null)
						{
							kjUser = kjUserDao.findByPk(kjInvoice.getInvoiceUser().getStaffId());
						}
						else if(kjInvoice.getKjInvoiceUser()!=null)
						{
							kjUser = kjInvoice.getKjInvoiceUser();
						}
					}
					else if("thesis".equals(module))
					{
						KjThesisDao kjThesisDao = null;
						KjThesis kjThesis = kjThesisDao.getInstance().findByPk(Long.parseLong(ids[i]));
						tableNameList.add(kjThesis.getName());
						if(kjThesis.getAuthorId()!=null)
						{
							kjUser = kjUserDao.findByPk(kjThesis.getAuthorId().getStaffId());
						}
						else if(kjThesis.getKjAuthorId()!=null)
						{
							kjUser = kjThesis.getKjAuthorId();
						}
					}
					else if("meetingFund".equals(module))
					{
						KjMeetingFundDao kjMeetingFundDao = null;
						KjMeetingFund kjMeetingFund = kjMeetingFundDao.getInstance().findByPk(Long.parseLong(ids[i]));
						tableNameList.add(kjMeetingFund.getKjMeeting().getMeetingName());
						if(kjMeetingFund.getKjMeeting().getCreateUser()!=null)
						{
							kjUser = kjUserDao.findByPk(kjMeetingFund.getKjMeeting().getCreateUser().getStaffId());
						}
						else if(kjMeetingFund.getKjMeeting().getKjCreateUser()!=null)
						{
							kjUser = kjMeetingFund.getKjMeeting().getKjCreateUser();
						}
					}
					else if("patent".equals(module) || "patentFee".equals(module))
					{
						KjPatent kjPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(ids[i]));
						List<String> result = getUserId(ids[i], "patentFee");
						if(result!=null && result.size()>0){
							for(int p=0; p<result.size(); p++){
								KjUser thisUser = KjUserDAO.getInstance().findByPk(result.get(p));
								tableNameList.add(kjPatent.getName());
								toAdd.add(thisUser.getEmail());
								toSms.add(thisUser.getCellPhone());
								userList.add(result.get(p));
							}
						}
						tableNameList.add(kjPatent.getName());
					}
					else if("apply".equals(module))
					{
						ApplyDAO applyDAO = null;
						KjApply kjApply = applyDAO.getInstance().findByPk(Long.parseLong(ids[i]));
						tableNameList.add(kjApply.getEmployeeByLeaderId().getName());
						
						if(kjApply.getEmployeeByLeaderId()!=null)
						{
							kjUser = kjUserDao.findByPk(kjApply.getEmployeeByLeaderId().getStaffId());
						}
						else if(kjApply.getKjUserByLeaderId()!=null)
						{
							kjUser = kjApply.getKjUserByLeaderId();
						}
					}
					else if("user".equals(module))
					{
						KjUserDAO kjUserDAO = null;
						kjUser = kjUserDAO.getInstance().findByPk(ids[i]);
						tableNameList.add(kjUser.getName());
					}else if("meeting".equals(module))
					{
						KjSnsMeetingUser kjSnsMeetingUser = KjSnsMeetingUserDAO.getInstance().findByPk(Long.parseLong(ids[i]));
						tableNameList.add(kjSnsMeetingUser.getKjUser().getName());
						kjUser = kjSnsMeetingUser.getKjUser();
					}
					else if("auth".equals(module)){
						KjStudentAuth auth = AuthDAO.getInstance().findByPk(Long.parseLong(ids[i]));

						
					}
			    	if(!("patent".equals(module) || "patentFee".equals(module))){
				    	if(kjUser!=null && kjUser.getEmail()!=null && kjUser.getEmail().length()>0){
				    		toAdd.add(kjUser.getEmail());
				    	}
				    	else  { toAdd.add(" "); }
				    	
				    	if(kjUser!=null && kjUser.getCellPhone()!=null && kjUser.getCellPhone().length()>0){
				    		toSms.add(kjUser.getCellPhone());
				    	}
				    	else  { toSms.add(" "); }
				    	
				    	if(kjUser!=null){
				    		userList.add(kjUser.getStaffId());
				    	}
				    	else{
				    		userList.add("");
				    	}
			    	}
			    }
	    }
	    else
	    {
	    	tableNameList.add(" ");
	    }
	    
	    if("psw".equals(module) && kjMail.getContent().indexOf("密码")>0)
	    {
	    	kjMail.setContent(kjMail.getContent().substring(0,kjMail.getContent().indexOf("密码")) + "密码为 *********");
	    }
	    
	    request.setAttribute("AMail", kjMail);
	    request.setAttribute("items", tableNameList);
	    request.setAttribute("toAdd", toAdd);
	    request.setAttribute("toSms", toSms);
	    request.setAttribute("userList", userList);
	}
	
	/**
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward detailMail(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		    Long MailId=Long.parseLong(request.getParameter("id"));
		    KjMail kjMail = KjMailDAO.getInstance().findByPk(MailId);
		    
		    parseMail(request,kjMail);
		    
			HttpSession session=request.getSession();
		    String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
		    request.setAttribute("sign", sign);
		    
		    request.setAttribute("kjMail", kjMail);
		    return new ActionForward("/mail/detailMail.jsp");
	}
	public ActionForward searchMail(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
			 			
		   // String title = request.getParameter("searchTitle");
			
			KjMailForm mailsForm=(KjMailForm)form;
			//String toAddress=mailsForm.getToAddress();
			
			HttpSession session=request.getSession();
			String who = ParamUtil.getParameter(request, "Who") ;
			String sqlWhere=" as kj where kj.deleteMark='0' order by mailId desc";
			if (request.getMethod()=="GET")
			{
				if (session.getAttribute("sqlWhere")!=null)
					sqlWhere=session.getAttribute("sqlWhere").toString();
			}
			else
			{
				if (who.equals("Who"))
				{
					sqlWhere=session.getAttribute("sqlWhere").toString();
				}
				else
				{
					sqlWhere=" as kj where ";
					if (mailsForm.getTitle()  !=null && mailsForm.getTitle()!="")
						sqlWhere+="kj.title like '%"+mailsForm.getTitle()+"%' and ";
					if (mailsForm.getContent()!=null && mailsForm.getContent()!="")
						sqlWhere+="kj.content like '%"+mailsForm.getContent()+"%' and ";
					if (mailsForm.getMailType()!=null && mailsForm.getMailType()!="")
						sqlWhere+="kj.mailType like '%"+mailsForm.getMailType()+"%' and ";
					if(mailsForm.getCreateDate() != null && mailsForm.getCreateDate().length() > 0)
					{
						Date searchTime;
						if(mailsForm.getCreateDate().equals("0"))
							searchTime = new Date();
						else if(mailsForm.getCreateDate().equals("1"))
							searchTime = new Date(new Date().getTime()-(long)6 * 24 * 60 * 60 * 1000);
						else if(mailsForm.getCreateDate().equals("2"))
							searchTime = new Date(new Date().getTime()-(long)29 * 24 * 60 * 60 * 1000);
						else
							searchTime = new Date(new Date().getTime()-(long)11 * 29 * 24 * 60 * 60 * 1000);
						String formatsearchTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(searchTime); 
						sqlWhere += "kj.createDate>=to_date('" + formatsearchTime + "', 'yyyy-MM-dd HH24:mi:ss') and ";
					}
					if (mailsForm.getDeleteMark()!=null &&mailsForm.getDeleteMark()!=0)
					{	
						sqlWhere+="kj.deleteMark = "+mailsForm.getDeleteMark().toString()+" and ";
					}
					else
					{
						sqlWhere+="kj.deleteMark='0' and ";
					}
					
					String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
					UserBean userbean = (UserBean) session.getAttribute("UserBean");
					String staffId = userbean.getUid();
					if(!"Admin".equals(sign))
					{
						sqlWhere = sqlWhere + "kj.kjUser='"+staffId+"' and ";
					}
					
					sqlWhere+="1=1 order by mailId desc";
					
					sqlWhere=SysConfig.checkSQL(sqlWhere);
					session.setAttribute("sqlWhere",sqlWhere);
				}
			}
			
			int sumCount;
			try {
				sumCount = KjMailDAO.getInstance().getCount(sqlWhere);
				PaginalBean paginalbean=new PaginalBean();
				paginalbean.setPaginal(request, sumCount);
				request.setAttribute("mailList",KjMailDAO.getInstance().getPaginalList(paginalbean.getCurrentCount(),SysConfig.PAGE_SIZE,sqlWhere));
				
//				 System.out.println(",,,search,,,,sqlWhere:  "+sqlWhere);
				
				request.setAttribute("PaginalBean", paginalbean);
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
			return new ActionForward("/mail/showMail.jsp");
			 
	}
	public ActionForward searchReceiveMail(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
			 			
		   // String title = request.getParameter("searchTitle");
			
			KjMailForm mailsForm=(KjMailForm)form;
			KjUserDAO Udao = KjUserDAO.getInstance();
			//String toAddress=mailsForm.getToAddress();
			HttpSession session=request.getSession();
			String who = ParamUtil.getParameter(request, "Who") ;
			String sqlWhere=" as kj where kj.deleteMark='0' order by mailId desc";
			if (request.getMethod()=="GET")
			{
				if (session.getAttribute("sqlWhere")!=null)
					sqlWhere=session.getAttribute("sqlWhere").toString();
			}
			else
			{
				if (who.equals("Who"))
				{
					sqlWhere=session.getAttribute("sqlWhere").toString();
				}
				else
				{
					sqlWhere=" as kj where ";
					if (mailsForm.getTitle()  !=null && mailsForm.getTitle()!="")
						sqlWhere+="kj.title like '%"+mailsForm.getTitle()+"%' and ";
					if (mailsForm.getContent()!=null && mailsForm.getContent()!="")
						sqlWhere+="kj.content like '%"+mailsForm.getContent()+"%' and ";
					if (mailsForm.getMailType()!=null && mailsForm.getMailType()!="")
						sqlWhere+="kj.mailType like '%"+mailsForm.getMailType()+"%' and ";
					if(mailsForm.getCreateDate() != null && mailsForm.getCreateDate().length() > 0)
					{
						Date searchTime;
						if(mailsForm.getCreateDate().equals("0"))
							searchTime = new Date();
						else if(mailsForm.getCreateDate().equals("1"))
							searchTime = new Date(new Date().getTime()-(long)6 * 24 * 60 * 60 * 1000);
						else if(mailsForm.getCreateDate().equals("2"))
							searchTime = new Date(new Date().getTime()-(long)29 * 24 * 60 * 60 * 1000);
						else
							searchTime = new Date(new Date().getTime()-(long)11 * 29 * 24 * 60 * 60 * 1000);
						String formatsearchTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(searchTime); 
						sqlWhere += "kj.createDate>=to_date('" + formatsearchTime + "', 'yyyy-MM-dd HH24:mi:ss') and ";
					}
					if (mailsForm.getDeleteMark()!=null &&mailsForm.getDeleteMark()!=0)
					{	
						sqlWhere+="kj.deleteMark = "+mailsForm.getDeleteMark().toString()+" and ";
					}
					else
					{
						sqlWhere+="kj.deleteMark='0' and ";
					}
					UserBean userbean = (UserBean) session.getAttribute("UserBean");
					String staffId = userbean.getUid();
					String address = Udao.getAddress(staffId);
					sqlWhere = sqlWhere + "kj.toAddress like '%"+address+"%' and ";

					
					sqlWhere+="1=1 order by mailId desc";
					
					sqlWhere=SysConfig.checkSQL(sqlWhere);
					session.setAttribute("sqlWhere",sqlWhere);
				}
			}
			int sumCount;
			try {
				sumCount = KjMailDAO.getInstance().getCount(sqlWhere);
				PaginalBean paginalbean=new PaginalBean();
				paginalbean.setPaginal(request, sumCount);
				request.setAttribute("mailList",KjMailDAO.getInstance().getPaginalList(paginalbean.getCurrentCount(),SysConfig.PAGE_SIZE,sqlWhere));
				
//				 System.out.println(",,,search,,,,sqlWhere:  "+sqlWhere);
				
				request.setAttribute("PaginalBean", paginalbean);
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
			return new ActionForward("/mail/showReceiveMail.jsp");
			 
	}
	// 根据模块中表的主键获取对应的人员主键
	public List<String> getUserId(String id/*当前表的主键*/, String mailType)
	{
		List<String> resultList = new ArrayList<String>();
		String userId = "";
		if("project".equals(mailType))
		{
			KjProject kjProject = ProjectDao.getInstance().findByPk(Long.parseLong(id));
			if(kjProject!=null){
				
				userId = kjProject.getKjUser().getStaffId();	
			}
				
		}
		else if("allot".equals(mailType))
		{
			KjFundAllot allot = FundAllotDAO.getInstance().findByPk(Long.parseLong(id));
			if(allot!=null){
				
				userId = allot.getKjProject().getKjUser().getStaffId();	
			}
				
		}
		else if("invoice".equals(mailType))
		{
			KjInvoice kjInvoice = KjInvoiceDao.getInstance().findByPk(Long.parseLong(id));
			if(kjInvoice!=null){
				
				userId = kjInvoice.getKjCreateUser().getStaffId().toString();	
			}
		}
		else if("thesis".equals(mailType))
		{
			KjThesis kjThesis = KjThesisDao.getInstance().findByPk(Long.parseLong(id));
			if(kjThesis!=null){
				
				userId = kjThesis.getKjAuthorId().toString();	
			}
		}
		else if("meetingFund".equals(mailType))
		{
			KjMeetingFund kjMeetingFund = KjMeetingFundDao.getInstance().findByPk(Long.parseLong(id));
			if(kjMeetingFund!=null){
				
				userId = kjMeetingFund.getKjMeeting().getKjCreateUser().getIdentityNo().toString();	
			}
		}
		else if("user".equals(mailType))
		{
			KjUser kjUser = KjUserDAO.getInstance().findByPk(id);
			if(kjUser!=null){
				userId = kjUser.getStaffId().toString();	
			}
		}
		else if("meeting".equals(mailType))
		{
			// KJ_SNS_MEETING_USER
			KjSnsMeetingUser kjSnsMeetingUser = KjSnsMeetingUserDAO.getInstance().findByPk(Long.parseLong(id));
			if(kjSnsMeetingUser!=null){
				userId = kjSnsMeetingUser.getKjUser().getStaffId();	
			}
		}
		if(userId.length()>0)
		{
			resultList.add(userId);
		}
		
		if("patent".equals(mailType) || "patentFee".equals(mailType)){  //根据专利主键获取发送邮件的用户列表,这里默认的是所有发明人 
			//发明人信息列表,分为旧的数据和新的数据
			List<KjProjectstaff> projectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(Long.parseLong(id), 0L, "0");
			List<KjProjectstaff> newProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(Long.parseLong(id), 2L, "0");
			
			//如果没有更新专利信息,则依旧显示旧的专利发明人
			if(newProjectStaffList==null || newProjectStaffList.size() ==0){
				newProjectStaffList = projectStaffList;
			}

			if(newProjectStaffList!=null){
				for(int i=0;i<newProjectStaffList.size(); i++){
					if(newProjectStaffList.get(i)!=null){
						if(newProjectStaffList.get(i).getKjUser()!=null){
							userId = newProjectStaffList.get(i).getKjUser().getStaffId();
							resultList.add(userId);
						}
					}
				}
			}
		}
		return resultList;
	}
	
	/**
	 * zz 2013-08-19 给出邮件发送回执功能，且一个地址发送失败不会影响其他地址的发送
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public ActionForward sendMail(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ParseException, IOException{
		KjMailForm kjMailForm = (KjMailForm) form;
		String title = request.getParameter("title");
		String toList = request.getParameter("tableIds");
		String sendMailStatus = "";
		String sendSmsStatus = "";
		
		String toSec = request.getParameter("secIds");
		String content = request.getParameter("content");
		String mailType = request.getParameter("mailType");
		//2014-4-30 by zhangm 发送邮件或者短信，或者二者同时
		String sendSms = ParamUtil.getParameter(request,"sendSms");
		String sendMail = ParamUtil.getParameter(request,"sendMail");
		
		//获得发送人的信息
		KjUserDAO kjUserDao = KjUserDAO.getInstance();
    	Long mailId  = 0l;
    	
    	//System.out.println("start##################");
    	//获得收件人的信息
		if(toList!=null && toList.length()>0){
			String[] tableIds = toList.split(",");
			String[] secIds = toSec.split(",");
			
			List<String> toUserId = new ArrayList<String>();
			List<String> toTableId = new ArrayList<String>();//zz 2014-02-28 和 toUserId 完全平行的1个数组
			toList ="";                                      //lyb 2015-01-13 此处重新置空 因为一条专利需要给所有的发明人发邮件
			for (int i=0; i<tableIds.length; i++){
				List<String> thisUserIds =  getUserId(tableIds[i], mailType);
				if(thisUserIds!=null && thisUserIds.size()>0){
					for (int j = 0; j < thisUserIds.size(); j++){
						String thisUserId = thisUserIds.get(j);
						if(thisUserId.length()>0 && !toUserId.contains(thisUserId)){
							toUserId.add(thisUserId);
							toTableId.add(tableIds[i]);
							toList+=tableIds[i]+",";
						}
					}
				}
			}
			for (int i=0; i<secIds.length; i++){
				if(secIds[i]!="" && !toUserId.contains(secIds[i])){
					Employee sec = EmployeeDAO.getInstance().findByPk(secIds[i]);
					String secId = sec.getStaffId();
					toUserId.add(secId);
					//zz 这里 toTableId 补齐即可
					toTableId.add("");
				}
			}
			
		
			//start - 发送邮件
			String toAddressList = "";
			String sendMailStatusItem = "未提交发送请求";
			
			//System.out.println("0-发送邮件!");
			
			int sendCount = 0;
			
			if(toUserId.size()>0){                         
				for (int i = 0; i < toUserId.size(); i++){
					Boolean isSend = false;
					String thisTableId = toTableId.get(i);
					String thisUserId = toUserId.get(i);
					KjUser kjUser = kjUserDao.findByPk(thisUserId);
					//System.out.println("emailAdd"+kjUser.getEmail());
					if(sendMail.equalsIgnoreCase("true")){
						if (kjUser != null && kjUser.getEmail() != null && kjUser.getEmail().length() > 0){
							try{
								Properties p = new Properties(); // Properties p =
																	// System.getProperties();
								p.put("mail.smtp.auth", "true");
								p.put("mail.transport.protocol", "smtp");
								p.put("mail.smtp.host", TechSystem.getInstance() .getTechConfig().getFromServer());
								p.put("mail.smtp.port", "25");
								// 建立会话
								Session session = Session.getInstance(p);
								Message msg = new MimeMessage(session); // 建立信息
								
								// zz 2012-03-26
								String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
		
								msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddress(), fromMail));
		
								msg.addRecipient(Message.RecipientType.TO, new InternetAddress(kjUser.getEmail()));
							
								// msg.setSentDate(); // 发送日期
								if (title != null && title.length() > 0)
								{
									msg.setSubject(kjUser.getName()+":"+title); // 主题
								}
								if (content != null && content.length() > 0){
//									msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"【本邮件由科技系统 http://kj.tju.edu.cn 发出，无需回复】"); // 内容
									msg.setContent(content, "text/html;charset = gbk");  //zz 2015-04-30
								}else{
									if(mailType.equals("patentFee")){
										content = "您有专利即将到达缴费日期，请尽快登录科技信息系统查询";
//										msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"【本邮件由科技系统 http://kj.tju.edu.cn 发出，无需回复】"); // 内容
										msg.setContent(content, "text/html;charset = gbk");  //zz 2015-04-30
									}
								}
								
								if(toAddressList.equals("")){
									toAddressList = kjUser.getEmail();
								}
								else{
									toAddressList = toAddressList + "," + kjUser.getEmail();
								}
								
								// 邮件服务器进行验证,本地调试的时候要屏蔽
//								Transport tran = session.getTransport("smtp");
//								tran.connect(TechSystem.getInstance().getTechConfig() .getFromServer(),
//											TechSystem.getInstance() .getTechConfig().getFromAddress(), 
//											TechSystem .getInstance().getTechConfig() .getFromPassword() );
//								tran.sendMessage(msg, msg.getAllRecipients()); // 发送
								
								System.out.println("send - " + kjUser.getEmail() );
								sendCount ++;
								if(sendCount%50==0)
								{
									System.out.println(sendCount);
								}
								isSend = true;
							} 
							catch (AddressException e) {
								//sendMap.put(kjUser.getEmail(), "收件地址出错");
								sendMailStatusItem = "收件地址出错";
							}
							catch (MessagingException e) {
								//sendMap.put(kjUser.getEmail(), "邮件发送出错");
								sendMailStatusItem = "邮件发送出错";
							} // 发件人
						}else{
							//sendMap.put(thisUserId, "收件人或地址信息不完整");
							sendMailStatusItem = "收件人或地址信息不完整";
						}
						if(isSend){
							sendMailStatusItem = "邮件发送成功";
						}
						else{
							sendMailStatusItem = "邮件发送失败";
						}
					}
					if(i == 0){
						sendMailStatus = sendMailStatusItem;
					}else{
						sendMailStatus = sendMailStatus + "," +sendMailStatusItem;
					}
				} //end for i-loop 
			}
			
			System.out.println("send sum - "  + sendCount);
			//end - 发送邮件
			
			//TODO 2018-12-12 SQH 上面是根据用户id循环发送邮件，下面是根据用户自行录入的抄送，发送邮件
			//获取抄送人列表
			String CCList = ParamUtil.getParameter(request,"CCList");
			//System.out.println(CCList);
			if(!CCList.equals("")){
				String[] CClist = CCList.split(",");
				List<String> toCClist = new ArrayList<String>();
				for(int i = 0; i< CClist.length; i++){
					toCClist.add(CClist[i]);
				}
				if(toCClist.size()>0){
					for (int i = 0; i <toCClist.size(); i++){
//						System.out.println(toCClist.get(i));
						Boolean isSend = false;
						if(sendMail.equalsIgnoreCase("true")){
							try{
								Properties p = new Properties(); // Properties p =
																// System.getProperties();
								p.put("mail.smtp.auth", "true");
								p.put("mail.transport.protocol", "smtp");
								p.put("mail.smtp.host", TechSystem.getInstance() .getTechConfig().getFromServerP());
								p.put("mail.smtp.port", "25");
								// 建立会话
								Session session = Session.getInstance(p);
								Message msg = new MimeMessage(session); // 建立信息
								
								// zz 2012-03-26
								String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
	
								msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddressP(), fromMail));
		
								msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toCClist.get(i)));
							
								// msg.setSentDate(); // 发送日期
								if (title != null && title.length() > 0)
									msg.setSubject(title); // 主题
	//							if (content != null && content.length() > 0){
	//								msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"【本邮件由科技系统 http://kj.tju.edu.cn 发出，无需回复】"); // 内容
	//							}
	//							msg.setText(content); // 内容
								msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
								
								if(toAddressList.equals("")){
									toAddressList = toCClist.get(i);
								}
								else{
									toAddressList = toAddressList + "," + toCClist.get(i);
								}
								
								// 邮件服务器进行验证,本地调试的时候要屏蔽
	//							System.out.println(TechSystem.getInstance().getTechConfig() .getFromServerP());
	//							System.out.println(TechSystem.getInstance().getTechConfig() .getFromAddressP());
	//							System.out.println(TechSystem.getInstance().getTechConfig() .getFromPasswordP());
	//							
								
								Transport tran = session.getTransport("smtp");
								tran.connect(TechSystem.getInstance().getTechConfig() .getFromServerP(),
											TechSystem.getInstance() .getTechConfig().getFromAddressP(), 
											TechSystem .getInstance().getTechConfig() .getFromPasswordP() );
								tran.sendMessage(msg, msg.getAllRecipients()); // 发送
								
								isSend = true;
							} 
							catch (AddressException e) {
								//sendMap.put(kjUser.getEmail(), "收件地址出错");
								sendMailStatusItem = "收件地址出错";
								e.printStackTrace();
							}
							catch (MessagingException e) {
								//sendMap.put(kjUser.getEmail(), "邮件发送出错");
								sendMailStatusItem = "邮件发送出错";
								e.printStackTrace();
							} // 发件人
						}else{
							//sendMap.put(thisUserId, "收件人或地址信息不完整");
							sendMailStatusItem = "收件人或地址信息不完整";
						}
						if(isSend){
							sendMailStatusItem = "邮件发送成功";
						}
						else{
							sendMailStatusItem = "邮件发送失败";
						}
					}
				}
			}
			
			//start - 发送短信
			String sendSmsStatusItem = "未提交发送请求";
			//System.out.println("1-发送短信!");
			for (int i = 0; i < toUserId.size(); i++){
				Boolean isSmsSend = false;
				String thisTableId = toTableId.get(i);
				String thisUserId = toUserId.get(i);
				
				KjUser kjUser = kjUserDao.findByPk(thisUserId);
				
				if(sendSms.equalsIgnoreCase("true")){
					if (kjUser != null && kjUser.getCellPhone() != null && SmsUtil.isMobileNO(kjUser.getCellPhone())){
						String filterdContent = StringUtil.splitAndFilterString(content,200);
						
						Boolean sendOK = SmsUtil.sendOneSms(filterdContent, kjUser.getCellPhone());
						if( sendOK ){
							//sendSmsMap.put(kjUser.getCellPhone(), "短信发送成功");
							sendSmsStatusItem = "短信发送成功";
							isSmsSend = true;
						}
						else{
							//sendSmsMap.put(kjUser.getCellPhone(), "短信发送失败");
							sendSmsStatusItem = "短信发送失败";
						}
					}
					else{
						//sendSmsMap.put(thisUserId, "用户人或手机号信息有误");
						sendSmsStatusItem = "用户手机号码信息有误";
//						//System.out.println(toId.get(i)+ " - 用户人或手机号信息有误");
					}
					/**
					 * zz 2014-02-28 把tableid的保存成功信息 放到专利的2个map中
					 */
					if(isSmsSend){
						sendSmsStatusItem = "短信发送成功";
					}
					else{
						sendSmsStatusItem = "短信发送失败";
					}
					
				}
				if(i == 0){
					sendSmsStatus = sendSmsStatusItem;
				}else{
					sendSmsStatus = sendSmsStatus + "," +sendSmsStatusItem;
				}
			}//end for i-loop
			if( sendSms.equalsIgnoreCase("true") ){title = title +"（已发短信）";}
			//end - 发送短信		
			
			//发送记录存入数据库
			kjMailForm.setTableIds(toList);
			kjMailForm.setMailType(mailType);
			kjMailForm.setFromAddress(TechSystem.getInstance() .getTechConfig().getFromAddress());
			kjMailForm.setCreateDate(null);
			
			kjMailForm.setToAddress(toAddressList);
			kjMailForm.setContent(content);
			kjMailForm.setTitle(title);
			
			kjMailForm.setSendMailStatus(sendMailStatus);
			kjMailForm.setSendSmsStatus(sendSmsStatus);

			mailId = KjMailDAO.getInstance().saveKjMail(request,kjMailForm);
			parseMail(request, KjMailDAO.getInstance().findByPk(mailId));
	
		}//end if toList
		
		//要能看到这个邮件中收信人的收信状态 2013-08-19
		
		if(mailType.equals("patent")){
//			KjPatentCostDao.getInstance().setRemained(patentMailMap, patentSmsMap);
		}
		
		HttpSession session=request.getSession();
	    String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
	    request.setAttribute("sign", sign);
	    
	    request.setAttribute("kjMail", KjMailDAO.getInstance().findByPk(mailId) );
		return new ActionForward("/mail/detailMail.jsp");
	}
	
	/**
	 * zz 2015-10-27 群发专利年费邮件
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public ActionForward sendPatentMailBatch(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ParseException, IOException{
		KjMailForm kjMailForm = (KjMailForm) form;
		HttpSession session=request.getSession();
        UserBean ub=(UserBean)session.getAttribute("UserBean");
		
		//zz 2012-03-30 让邮件标题更有区别性
		String firstObjName = "";
		
		String userName = ub.getName();
		String userId = ub.getUid();
		List <String> Privlige = ub.getIdentity(ub);
		if(Privlige.contains("Admin"))
			request.setAttribute("privlige", "Admin");
			else request.setAttribute("privlige", "no");
		kjMailForm.setFromAddress(userName);
		kjMailForm.setFromAddressId(userId);
		String module = ParamUtil.getParameter(request, "module");

		kjMailForm.setMailType(module);
		
		List<String> selectedList = new ArrayList<String>();
		String selectedItems = ParamUtil.getParameter(request, "selectedItems");
		String unSelectedItems = ParamUtil.getParameter(request, "unSelectedItems");
        String tableIds = ParamUtil.getParameter(request, "tableIds");
		String otherModuleType = ParamUtil.getParameter(request, "otherModuleType");
		String otherModuleId = ParamUtil.getParameter(request, "otherModuleId");
		
		request.setAttribute("otherModuleType", otherModuleType);
		
		if(selectedItems!=null && unSelectedItems!=null && tableIds != null)
			selectedList = dealIds(tableIds, selectedItems, unSelectedItems);
		
		String itemsIds = "";
		
		for(int i=0;i<selectedList.size();i++){
			if(i>0){
				itemsIds +=",";
			}
			itemsIds += selectedList.get(i);	
		}
		
		String mailType = module;
		String sendMail = "true";
		
		//kjMailForm.setTableIds(itemsIds);
		//TODO 取各模块列表显示在页面上，并设置默认标题和内容
		
		String mailId = "";
		List<String> toItemList = new ArrayList<String>();
		//List<String> toListName  =new ArrayList<String>();
		List<String> userList  =new ArrayList<String>();
		List<String> toAdd  =new ArrayList<String>();
		List<String> toSms  =new ArrayList<String>();
		//String sendMailStatusString = "";
		//String sendSmsStatusString = "";
		
		KjUserDAO kjUserDao = KjUserDAO.getInstance();
		KjPatent kjPatent =new KjPatent();//DUAN 2014-02-27
		if(selectedList!=null && selectedList.size()>0){
			for (int i=0; i<selectedList.size(); i++){
				if(selectedList.get(i).length()==0){
					continue;
				}
				String tempSelectedId = selectedList.get(i);
				String tempToAdd = "";
				String tempToSms = "";
				String tempUserId = "";
				KjUser kjUser = null;
				StringBuffer contentStrBuf = new StringBuffer("");
				kjMailForm.setTitle("关于专利：");
				
				/*if("patent".equals(module) || "patentFee".equals(module))
				{
					
					String kjDictStatusId = ParamUtil.getParameter(request, "status");
					if("5".equals(kjDictStatusId)){
						contentStrBuf.append("您的下列专利等待审核：");
					}else if("6".equals(kjDictStatusId)){
						contentStrBuf.append("您的下列专利院级审核通过：");
					}else if("8".equals(kjDictStatusId)){
						contentStrBuf.append("您的下列专利校级审核通过：");	
					}else if("9".equals(kjDictStatusId)){
						contentStrBuf.append("您的下列专利审核驳回：");
					}
				}*/
				
				if("patent".equals(module) || "patentFee".equals(module)) //生成正文
				{
//					/KjPatentDao kjPatentDao = null;
					//inc.tech.patent.dao.KjPatentDao kjPatentDao =null;
					kjPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toItemList.add(kjPatent.getName());
					//contentStrBuf.append(kjPatent.getName() + " ，");
					/*if(kjPatent!=null && kjPatent.getKjFirstInventorId()!=null && kjPatent.getKjFirstInventorId().getName()!=null){
						toListName.add(kjPatent.getKjFirstInventorId().getName());
					}else{
						toListName.add("无第一发明人");
					}*/
					kjUser = kjPatent.getKjFirstInventorId();
			    	firstObjName = "《"+kjPatent.getName()+"》" + " 需尽快缴纳年费";
			    	
			    			KjTableTemplate kjTableTemplate = KjTableTemplateDao.getInstance().searchTable("专利费用", 15l);
			    			String thisContent = kjTableTemplate.getContent();
			    			thisContent = thisContent.replaceAll("param_patentName", kjPatent.getName()==null?"":kjPatent.getName());
			    			thisContent = thisContent.replaceAll("param_patentNo", kjPatent.getPatentNo()==null?"":kjPatent.getPatentNo());
			    			thisContent = thisContent.replaceAll("param_patentName", kjPatent.getKjFirstInventorId().getName());
			    			String remindStr = KjPatentFeeDao.getInstance().getRemindStringByPatentId(kjPatent.getPatentId());
			    			thisContent = thisContent.replaceAll("param_feeNameAndAmount", remindStr.substring(remindStr.indexOf("其中")));
			    			List<KjProjectstaff> newProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 2L, "0");
			    			List<KjProjectstaff> oldProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 0L, "0");
			    			if(newProjectStaffList==null || newProjectStaffList.size()==0) newProjectStaffList = oldProjectStaffList;
			    			String allUsername = "";
			    			if(newProjectStaffList!=null)
			    			{
			    				for (int j = 0; j < newProjectStaffList.size(); j++) 
			    				{
			    					if(j==0) allUsername = newProjectStaffList.get(j).getKjUser().getName();
			    					else allUsername = allUsername + "," + newProjectStaffList.get(j).getKjUser().getName();
								}
			    			}
			    			thisContent = thisContent.replaceAll("param_allInventors", allUsername==null?"":allUsername);
			    			String acceptDate = DateUtil.convertDateToString(kjPatent.getAcceptDate());
			    			thisContent = thisContent.replaceAll("param_applyDate", acceptDate==null?"":acceptDate);
			    			
			    			contentStrBuf.append(thisContent);
				 }
				
				 //String  sendMailStatusStringTemp = "";
			     //String  sendSmsStatusStringTemp  = "";
				 kjMailForm.setTitle(kjMailForm.getTitle() + firstObjName + " 的提醒");
				 kjMailForm.setContent(contentStrBuf.toString());
				 String content = contentStrBuf.toString();
				 String title = kjMailForm.getTitle();
				 String sendMailStatus = "";
				 String sendSmsStatus = "";
				 
				  //获取所有人员
				 List<String> toUserId = new ArrayList<String>();
				 List<String> toTableId = new ArrayList<String>();    
				 List<String> thisUserIds =  getUserId(tempSelectedId, mailType);
				 KjPatent thisPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(tempSelectedId));
				 if(thisUserIds!=null && thisUserIds.size()>0){
					for (int j = 0; j < thisUserIds.size(); j++){
						String thisUserId = thisUserIds.get(j);
						KjUser thisUser = KjUserDAO.getInstance().findByPk(thisUserId);
						if(thisUserId.length()>0 && !toUserId.contains(thisUserId)){
							if(toUserId.size()>0)
							{
								toUserId.add(thisUserId);
								toTableId.add(tempSelectedId);
								tempToAdd = tempToAdd + "," + thisUser.getCellPhone().toString();
								tempToSms = tempToSms + "," + thisUser.getEmail().toString();
								tempUserId = tempUserId + "," + thisUserId.toString();
							}
							else
							{
								toUserId.add(thisUserId);
								toTableId.add(tempSelectedId);
								tempToAdd = thisUser.getCellPhone().toString();
								tempToSms = thisUser.getEmail().toString();
								tempUserId = thisUserId.toString();
							}
						}
					}
				 }
				 toAdd.add(tempToAdd);
				 toSms.add(tempToSms);
				 userList.add(tempUserId);
				 //start - 发送邮件
				 String toAddressList = "";
				 String sendMailStatusItem = "未提交发送请求";
				 //send mails
				 if(toUserId.size()>0){                         
						for (int j = 0; j < toUserId.size(); j++){
							Boolean isSend = false;
							String thisTableId = toTableId.get(j);
							String thisUserId = toUserId.get(j);
							kjUser = kjUserDao.findByPk(thisUserId);
							//System.out.println("emailAdd"+kjUser.getEmail());
							if(sendMail.equalsIgnoreCase("true")){
								if (kjUser != null && kjUser.getEmail() != null && kjUser.getEmail().length() > 0){
									try{
										Properties p = new Properties(); // Properties p =
																			// System.getProperties();
										p.put("mail.smtp.auth", "true");
										p.put("mail.transport.protocol", "smtp");
										p.put("mail.smtp.host", TechSystem.getInstance() .getTechConfig().getFromServer());
										p.put("mail.smtp.port", "25");
										// 建立会话
										Session session2 = Session.getInstance(p);
										Message msg = new MimeMessage(session2); // 建立信息
										
										// zz 2012-03-26
										String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
				
										msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddress(), fromMail));
				
										msg.addRecipient(Message.RecipientType.TO, new InternetAddress(kjUser.getEmail()));
									
										// msg.setSentDate(); // 发送日期
										if (title != null && title.length() > 0)
											msg.setSubject(title); // 主题
//										if (content != null && content.length() > 0){
//											msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"【本邮件由科技系统 http://kj.tju.edu.cn 发出，无需回复】"); // 内容
//										}
//										msg.setText(content); // 内容
										msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
										
										if(toAddressList.equals("")){
											toAddressList = kjUser.getEmail();
										}
										else{
											toAddressList = toAddressList + "," + kjUser.getEmail();
										}
										
										// 邮件服务器进行验证,本地调试的时候要屏蔽
										Transport tran = session2.getTransport("smtp");
										tran.connect(TechSystem.getInstance().getTechConfig() .getFromServer(),
													TechSystem.getInstance() .getTechConfig().getFromAddress(), 
													TechSystem .getInstance().getTechConfig() .getFromPassword() );
										tran.sendMessage(msg, msg.getAllRecipients()); // 发送
										
										isSend = true;
									} 
									catch (AddressException e) {
										//sendMap.put(kjUser.getEmail(), "收件地址出错");
										sendMailStatusItem = "收件地址出错";
									}
									catch (MessagingException e) {
										//sendMap.put(kjUser.getEmail(), "邮件发送出错");
										sendMailStatusItem = "邮件发送出错";
									} // 发件人
								}else{
									//sendMap.put(thisUserId, "收件人或地址信息不完整");
									sendMailStatusItem = "收件人或地址信息不完整";
								}
								if(isSend){
									sendMailStatusItem = "邮件发送成功";
								}
								else{
									sendMailStatusItem = "邮件发送失败";
								}
							}
							if(j == 0){
								sendMailStatus = sendMailStatusItem;
							}else{
								sendMailStatus = sendMailStatus + "," +sendMailStatusItem;
							}
						}//end for i-loop 
				 }
				
				//发送记录存入数据库
				kjMailForm.setTableIds(tempSelectedId);
				kjMailForm.setMailType(mailType);
				kjMailForm.setFromAddress(TechSystem.getInstance() .getTechConfig().getFromAddress());
				kjMailForm.setCreateDate(null);
					
				kjMailForm.setToAddress(toAddressList);
				kjMailForm.setContent(content);
				kjMailForm.setTitle(title);
					
				kjMailForm.setSendMailStatus(sendMailStatus);
				kjMailForm.setSendSmsStatus(sendSmsStatus);
				
				if(i==0)
					mailId = KjMailDAO.getInstance().saveKjMail(request,kjMailForm).toString();
				else mailId = mailId + "," + KjMailDAO.getInstance().saveKjMail(request,kjMailForm).toString();
				//mailId.add(KjMailDAO.getInstance().saveKjMail(request,kjMailForm).toString());	
				//System.out.println("mialId:"+mailId);
			}
		}
	    String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
	    request.setAttribute("sign", sign);
	    
	    request.setAttribute("items", toItemList);
	    request.setAttribute("toAdd", toAdd);
	    request.setAttribute("toSms", toSms);
	    request.setAttribute("userList", userList);
	    request.setAttribute("mailId", mailId);
	   
		return new ActionForward("/mail/detailMail.jsp");
	}
	
	/*
	 * lyb-2015-01-13 专利的每个id存储一条邮件发送记录
	 * 给科研秘书发邮件暂时屏蔽
	 * zz 2017-09-05 更改为专利专用邮箱
	 */
	public ActionForward sendMailEach(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ParseException, IOException{
		KjMailForm kjMailForm = (KjMailForm) form;
		String title = request.getParameter("title");
		String toList = request.getParameter("tableIds");
	//	System.out.println(toList);
		String sendMailStatus = "";
		String sendSmsStatus = "";
		
		String toSec = request.getParameter("secIds");
		String content = request.getParameter("content");
		String mailType = request.getParameter("mailType");
		//2014-4-30 by zhangm 发送邮件或者短信，或者二者同时
		String sendSms = ParamUtil.getParameter(request,"sendSms");
		String sendMail = ParamUtil.getParameter(request,"sendMail");
		String doGiveup = ParamUtil.getParameter(request,"doGiveup");
		
//		System.out.println(" sendMailEach doGiveup " + doGiveup);//ok
		
		//获得发送人的信息
		KjUserDAO kjUserDao = KjUserDAO.getInstance();
    	Long mailId  = 0l;
    	
    	//用于存储之后的回显
    	List<String> tableNameList=new ArrayList<String>();
	    List<String> toSms = new ArrayList<String>();
	    List<String> toAdd = new ArrayList<String>();
	    List<String> toCCAdd = new ArrayList<String>();   //抄送人地址回显使用
	    String       sendMailStatusString = "";
	    String       sendSmsStatusString  = "";
	    List<String> userList = new ArrayList<String>();
    	HashMap patentIdMap = new HashMap<String, String>();
    	//每条专利单独存储一个发邮件历史记录
		if(toList!=null && toList.length()>0){
			String[] tableIds = toList.split(",");
		//	System.out.println(tableIds.length);
			String toListIdMail = (String)request.getParameter("toListIdMail");
			//System.out.println(toListIdMail);
			for (int k=0; k<tableIds.length; k++){       //针对每条专利,组装生产该专利的费用类型等提醒字段
				List<String> toUserId = new ArrayList<String>();
				List<String> toTableId = new ArrayList<String>();
				toList = tableIds[k]+",";       
//				if(content==null || content.length()==0){  //如果没有默认的或者自己修改过的内容则系统自动生成
//					content = KjPatentFeeDao.getInstance().getRemindStringByPatentId(Long.parseLong(tableIds[k]));
//				}
				List<String> thisUserIds =  getUserId(tableIds[k], mailType);
				KjPatent thisPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(tableIds[k]));
				//合并发送中判断是否是同一个发明人的邮件，若是，只发送一封
				String inventorName = KjPatentDao.getInstance().findByPk(Long.parseLong(tableIds[k])).getKjFirstInventorId().getName();
				if(k-1 >=0){
					if(inventorName.equals(KjPatentDao.getInstance().findByPk(Long.parseLong(tableIds[k-1])).getKjFirstInventorId().getName())){
						continue;
					}
				}
				if(thisUserIds!=null && thisUserIds.size()>0){
					for (int j = 0; j < thisUserIds.size(); j++){
						String thisUserId = thisUserIds.get(j);
						KjUser thisUser = KjUserDAO.getInstance().findByPk(thisUserId);
						if(thisUserId.length()>0 && !toUserId.contains(thisUserId) && toListIdMail.contains(thisUserId)){
							toUserId.add(thisUserId);
							toTableId.add(tableIds[k]);
							tableNameList.add(thisPatent.getName());
							toSms.add(thisUser.getCellPhone());
							toAdd.add(thisUser.getEmail());
							userList.add(thisUserId);
						}
					}
				}
				
				//start - 发送邮件
				String toAddressList = "";
				String sendMailStatusItem = "未提交发送请求";
				
				//System.out.println("0-发送邮件!");
				if(toUserId.size()>0){                         
					for (int i = 0; i < toUserId.size(); i++){
						Boolean isSend = false;
						String thisTableId = toTableId.get(i);
						String thisUserId = toUserId.get(i);
						KjUser kjUser = kjUserDao.findByPk(thisUserId);
						//System.out.println("emailAdd"+kjUser.getEmail());
						if(sendMail.equalsIgnoreCase("true")){
							if (kjUser != null && kjUser.getEmail() != null && kjUser.getEmail().length() > 0){
								try{
									Properties p = new Properties(); // Properties p =
																		// System.getProperties();
									p.put("mail.smtp.auth", "true");
									p.put("mail.transport.protocol", "smtp");
									p.put("mail.smtp.host", TechSystem.getInstance() .getTechConfig().getFromServerP());
									p.put("mail.smtp.port", "25");
									// 建立会话
									Session session = Session.getInstance(p);
									Message msg = new MimeMessage(session); // 建立信息
									
									// zz 2012-03-26
									String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
			
									msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddressP(), fromMail));
			
									msg.addRecipient(Message.RecipientType.TO, new InternetAddress(kjUser.getEmail()));
								
									// msg.setSentDate(); // 发送日期
									if (title != null && title.length() > 0)
										msg.setSubject(title); // 主题
//									if (content != null && content.length() > 0){
//										msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"【本邮件由科技系统 http://kj.tju.edu.cn 发出，无需回复】"); // 内容
//									}
//									msg.setText(content); // 内容
									msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
									
									if(toAddressList.equals("")){
										toAddressList = kjUser.getEmail();
									}
									else{
										toAddressList = toAddressList + "," + kjUser.getEmail();
									}
									
									// 邮件服务器进行验证,本地调试的时候要屏蔽
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromServerP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromAddressP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromPasswordP());
								
									
									Transport tran = session.getTransport("smtp");
									tran.connect(TechSystem.getInstance().getTechConfig() .getFromServerP(),
												TechSystem.getInstance() .getTechConfig().getFromAddressP(), 
												TechSystem .getInstance().getTechConfig() .getFromPasswordP() );
									tran.sendMessage(msg, msg.getAllRecipients()); // 发送
									
									isSend = true;
								} 
								catch (AddressException e) {
									//sendMap.put(kjUser.getEmail(), "收件地址出错");
									sendMailStatusItem = "收件地址出错";
									e.printStackTrace();
								}
								catch (MessagingException e) {
									//sendMap.put(kjUser.getEmail(), "邮件发送出错");
									sendMailStatusItem = "邮件发送出错";
									e.printStackTrace();
								} // 发件人
							}else{
								//sendMap.put(thisUserId, "收件人或地址信息不完整");
								sendMailStatusItem = "收件人或地址信息不完整";
							}
							if(isSend){
								sendMailStatusItem = "邮件发送成功";
							}
							else{
								sendMailStatusItem = "邮件发送失败";
							}
						}
						if(i == 0){
							sendMailStatus = sendMailStatusItem;
						}else{
							sendMailStatus = sendMailStatus + "," +sendMailStatusItem;
						}
						
						break;//zz 2017-12-18
					}//end for i-loop 
				}
				//end - 发送邮件
				
				//start -给抄送人发送邮件
				String CCList = ParamUtil.getParameter(request,"CCList");
//				System.out.println(CCList);
				if(!CCList.equals("")){
					String[] CClist = CCList.split(",");
					List<String> toCClist = new ArrayList<String>();
					for(int i = 0; i< CClist.length; i++){
						toCClist.add(CClist[i]);
					}
//					System.out.println(toCClist.size());
					if(toCClist.size()>0){
						for (int i = 0; i <toCClist.size(); i++){
//							System.out.println(toCClist.get(i));
							toCCAdd.add(toCClist.get(i));
							Boolean isSend = false;
							if(sendMail.equalsIgnoreCase("true")){
								try{
									Properties p = new Properties(); // Properties p =
																	// System.getProperties();
									p.put("mail.smtp.auth", "true");
									p.put("mail.transport.protocol", "smtp");
									p.put("mail.smtp.host", TechSystem.getInstance() .getTechConfig().getFromServerP());
									p.put("mail.smtp.port", "25");
									// 建立会话
									Session session = Session.getInstance(p);
									Message msg = new MimeMessage(session); // 建立信息
									
									// zz 2012-03-26
									String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
		
									msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddressP(), fromMail));
			
									msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toCClist.get(i)));
								
									// msg.setSentDate(); // 发送日期
									if (title != null && title.length() > 0)
										msg.setSubject(title); // 主题
		//							if (content != null && content.length() > 0){
		//								msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"【本邮件由科技系统 http://kj.tju.edu.cn 发出，无需回复】"); // 内容
		//							}
		//							msg.setText(content); // 内容
									msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
									
									if(toAddressList.equals("")){
										toAddressList = toCClist.get(i);
									}
									else{
										toAddressList = toAddressList + "," + toCClist.get(i);
									}
									
									// 邮件服务器进行验证,本地调试的时候要屏蔽
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromServerP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromAddressP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromPasswordP());
							
									
									Transport tran = session.getTransport("smtp");
									tran.connect(TechSystem.getInstance().getTechConfig() .getFromServerP(),
												TechSystem.getInstance() .getTechConfig().getFromAddressP(), 
												TechSystem .getInstance().getTechConfig() .getFromPasswordP() );
									tran.sendMessage(msg, msg.getAllRecipients()); // 发送
									
									isSend = true;
								} 
								catch (AddressException e) {
									//sendMap.put(kjUser.getEmail(), "收件地址出错");
									sendMailStatusItem = "收件地址出错";
									e.printStackTrace();
								}
								catch (MessagingException e) {
									//sendMap.put(kjUser.getEmail(), "邮件发送出错");
									sendMailStatusItem = "邮件发送出错";
									e.printStackTrace();
								} // 发件人
							}else{
								//sendMap.put(thisUserId, "收件人或地址信息不完整");
								sendMailStatusItem = "收件人或地址信息不完整";
							}
							if(isSend){
								sendMailStatusItem = "邮件发送成功";
							}
							else{
								sendMailStatusItem = "邮件发送失败";
							}
						}
					}
				}
				//end-给抄送人发送邮件
				
				//start - 发送短信
				String sendSmsStatusItem = "未提交发送请求";
				//System.out.println("1-发送短信!");
				for (int i = 0; i < toUserId.size(); i++){
					Boolean isSmsSend = false;
					String thisTableId = toTableId.get(i);
					String thisUserId = toUserId.get(i);
					
					KjUser kjUser = kjUserDao.findByPk(thisUserId);
					
					if(sendSms.equalsIgnoreCase("true")){
						if (kjUser != null && kjUser.getCellPhone() != null && SmsUtil.isMobileNO(kjUser.getCellPhone())){
							content = KjPatentFeeDao.getInstance().getRemindStringByPatentId(Long.parseLong(tableIds[k]));//zz 2015-04-30  防止字符太长
							String filterdContent = StringUtil.splitAndFilterString(content,200);
							
							Boolean sendOK = SmsUtil.sendOneSms(filterdContent, kjUser.getCellPhone());
							if( sendOK ){
								//sendSmsMap.put(kjUser.getCellPhone(), "短信发送成功");
								sendSmsStatusItem = "短信发送成功";
								isSmsSend = true;
							}
							else{
								//sendSmsMap.put(kjUser.getCellPhone(), "短信发送失败");
								sendSmsStatusItem = "短信发送失败";
							}
						}
						else{
							//sendSmsMap.put(thisUserId, "用户人或手机号信息有误");
							sendSmsStatusItem = "用户手机号码信息有误";
//							//System.out.println(toId.get(i)+ " - 用户人或手机号信息有误");
						}
						/**
						 * zz 2014-02-28 把tableid的保存成功信息 放到专利的2个map中
						 */
						if(isSmsSend){
							sendSmsStatusItem = "短信发送成功";
						}
						else{
							sendSmsStatusItem = "短信发送失败";
						}
						
					}
					if(i == 0){
						sendSmsStatus = sendSmsStatusItem;
					}else{
						sendSmsStatus = sendSmsStatus + "," +sendSmsStatusItem;
					}
				}//end for i-loop
				if( sendSms.equalsIgnoreCase("true") ){title = title +"（已发短信）";}
				//end - 发送短信		
				
				//发送记录存入数据库
				kjMailForm.setTableIds(toList);
				kjMailForm.setMailType(mailType);
				kjMailForm.setFromAddress(TechSystem.getInstance() .getTechConfig().getFromAddressP());
				kjMailForm.setCreateDate(null);
				
				kjMailForm.setToAddress(toAddressList);
				//System.out.println(toAddressList+"++++++++++");
				kjMailForm.setContent(content);
				kjMailForm.setTitle(title);
				
				kjMailForm.setSendMailStatus(sendMailStatus);
				if(sendMailStatusString.length()==0 || sendMailStatus.endsWith(",")){
					sendMailStatusString+=sendMailStatus;
				}else{
					sendMailStatusString+=","+sendMailStatus;
				}
				
				kjMailForm.setSendSmsStatus(sendSmsStatus);
				if(sendSmsStatusString.length()==0 || sendSmsStatus.endsWith(",")){
					sendSmsStatusString+=sendSmsStatus;
				}else{
					sendSmsStatusString+=","+sendSmsStatus;
				}

				mailId = KjMailDAO.getInstance().saveKjMail(request,kjMailForm);
				
				request.setAttribute("kjMail", KjMailDAO.getInstance().findByPk(mailId) );
				request.setAttribute("AMail", KjMailDAO.getInstance().findByPk(mailId) );
			}
		}
		
		
		HttpSession session=request.getSession();
	    String sign = ((KjDictIdentity)session.getAttribute("identity")).getSign();
	    request.setAttribute("sign", sign);
	    
	    request.setAttribute("items", tableNameList);
	    request.setAttribute("toAdd", toAdd);
	    //System.out.println(toAdd+"++++++++++");
	    request.setAttribute("toCCAdd", toCCAdd); //用于抄送人地址的回显
	    request.setAttribute("toSms", toSms);
	    request.setAttribute("userList", userList);
	    request.setAttribute("sendMailStatusString", sendMailStatusString);
	    request.setAttribute("sendSmsStatusString", sendSmsStatusString);
	    
	    if(doGiveup.equalsIgnoreCase("true"))
	    {
	    	String patentId = toList.replaceAll(",", "").replaceAll(";", "");
	    	if(patentId!=null && patentId.length()>0)
	    	{
	    		KjPatentGiveUpDao.getInstance().addPatentGiveUpForced(request, Long.parseLong(patentId));
	    	}
	    }
	    
		return new ActionForward("/mail/detailMail.jsp");
	}
		/*	
		 * SQH 判断合并发送的邮件是否是同一个发明人
		 * 
		 * */
	public ActionForward isSameInventorName(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException{
		String selectedItems = ParamUtil.getParameter(request, "selectedItems");
		String tableIds = ParamUtil.getParameter(request, "tableIds");
		String unSelectedItems = ParamUtil.getParameter(request, "unSelectedItems");
		
		List<String> selectedList = new ArrayList<String>();
		if(selectedItems!=null && unSelectedItems!=null && tableIds != null)
			selectedList = dealIds(tableIds, selectedItems, unSelectedItems);
		boolean isSame = true;
		if(selectedList!=null && selectedList.size()>0){
			for(int i =1; i<selectedList.size();i++){
			//	System.out.println(selectedList.get(i));
				String kjPatentName = KjPatentDao.getInstance().findByPk(Long.parseLong(selectedList.get(i))).getKjFirstInventorId().getName();
			//	System.out.println(kjPatentName);
				if(!kjPatentName.equals(KjPatentDao.getInstance().findByPk(Long.parseLong(selectedList.get(i-1))).getKjFirstInventorId().getName())){
					isSame = false;
					break;
				}
			}
		}
		if(isSame){
			response.setContentType("text/html;charset=utf-8");     
			PrintWriter out= response.getWriter();
			out.println("是同一发明人");
			out.flush();
			out.close();	
		}
		
		return null;
	}
	
	/**
	 * zz 2013-09-10 增加短信支持<br>
	 * zz 2014-01-16 扩展到拨款文件<br>
	 * zz 2017-10-19 扩展到强制放弃 doGiveup = true 表示强制放弃<br>
	 * ZZ SQH 2018-12-12 支持“合并邮件”按钮。现在可以多个专利的正文拼接。其他发明人可以列出来不要默认选中（改jsp）。
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward gotoSendList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
		KjMailForm kjMailForm = (KjMailForm) form;
		HttpSession session=request.getSession();
		UserBean ub=(UserBean)session.getAttribute("UserBean");
		
		//zz 2012-03-30 让邮件标题更有区别性
		String firstObjName = "";
		int mark = 0;  //用来合并邮件正文
		
		String userName = ub.getName();
		String userId = ub.getUid();
		List <String> Privlige = ub.getIdentity(ub);
		if(Privlige.contains("Admin"))
			request.setAttribute("privlige", "Admin");
			else request.setAttribute("privlige", "no");
		kjMailForm.setFromAddress(userName);
		kjMailForm.setFromAddressId(userId);
		String module = ParamUtil.getParameter(request, "module");
//		System.out.println("module is :"+module);
		kjMailForm.setMailType(module);
		request.setAttribute("mailType", module);
		
		List<String> selectedList = new ArrayList<String>();
		String selectedItems = ParamUtil.getParameter(request, "selectedItems");
		String unSelectedItems = ParamUtil.getParameter(request, "unSelectedItems");
		String tableIds = ParamUtil.getParameter(request, "tableIds");
		String otherModuleType = ParamUtil.getParameter(request, "otherModuleType");
		String otherModuleId = ParamUtil.getParameter(request, "otherModuleId");
		String doGiveup = ParamUtil.getParameter(request, "doGiveup");//zz 2017-10-19
		
//		String mark = ParamUtil.getParameter(request, "mark");   //1为发消息，2为合并发消息
//		System.out.println("mark is :"+mark);
		
		request.setAttribute("otherModuleType", otherModuleType);
		request.setAttribute("doGiveup", doGiveup);
		
		if(otherModuleType.equals("fundFile" )){
			String projectIdsAllot = "";
			List<KjFundAllot> allotList = FundAllotDAO.getInstance().getListBySql(" as kj where kj.deleteMark ='0' and kj.kjFundFile.fileId="+otherModuleId+" and kj.statusId>=3");
//			System.out.println(allotList.size());
			for(int i=0;i<allotList.size();i++)
			{
				if(i==0)
				{
					projectIdsAllot = allotList.get(i).getKjProject().getProjectId().toString();
				}
				else
				{
					projectIdsAllot = projectIdsAllot + "," + allotList.get(i).getKjProject().getProjectId().toString();
				}
			}
			if(projectIdsAllot.length()>0)
			{
				selectedItems = projectIdsAllot;
			}
		}
		
		if(selectedItems!=null && unSelectedItems!=null && tableIds != null)
			selectedList = dealIds(tableIds, selectedItems, unSelectedItems);
		
		String newTableIds = "";
		
		for(int i=0;i<selectedList.size();i++){
			if(i>0){
				newTableIds +=",";
			}
		newTableIds += selectedList.get(i);	
		}
		
//		System.out.println(newTableIds);
		
		kjMailForm.setTableIds(newTableIds);
		//TODO 取各模块列表显示在页面上，并设置默认标题和内容
		StringBuffer contentStrBuf = new StringBuffer("");
		if("project".equals(module)){
			
			String kjDictStatusId = ParamUtil.getParameter(request, "kjDictStatusId");
			kjMailForm.setTitle("关于项目：");
			if("5".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目等待审核：");
			}else if("11".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目已立项：");
			}else if("16".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目立项中：");	
			}else if("10".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目待立项：");
			}else if("6".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目院级审核通过：");
			}else if("9".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目审核驳回：");
			}else if("13".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列项目已结题：");
			}
		}
		else if("invoice".equals(module))
		{
			kjMailForm.setTitle("关于发票：");
			String kjDictStatusId = ParamUtil.getParameter(request, "invoiceType");
			if("技术开发".equals(kjDictStatusId)){
				contentStrBuf.append("技术开发：");
			}else if("技术服务".equals(kjDictStatusId)){
				contentStrBuf.append("技术服务：");
			}else if("技术转让".equals(kjDictStatusId)){
				contentStrBuf.append("技术转让：");	
			}else if("技术咨询".equals(kjDictStatusId)){
				contentStrBuf.append("技术咨询：");
			}else if("科研费".equals(kjDictStatusId)){
				contentStrBuf.append("科研费：");
			}
		}
		else if("thesis".equals(module))
		{
			kjMailForm.setTitle("关于论文：");
			String kjDictStatusId = ParamUtil.getParameter(request, "status");
			if("5".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列论文等待审核：");
			}else if("6".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列论文院级审核通过：");
			}else if("8".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列论文校级审核通过：");	
			}else if("9".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列论文审核驳回：");
			}
		}
		else if("patent".equals(module) || "patentFee".equals(module))
		{
			kjMailForm.setTitle("关于专利：");
			String kjDictStatusId = ParamUtil.getParameter(request, "status");
			if("5".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列专利等待审核：");
			}else if("6".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列专利院级审核通过：");
			}else if("8".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列专利校级审核通过：");	
			}else if("9".equals(kjDictStatusId)){
				contentStrBuf.append("您的下列专利审核驳回：");
			}
		}
		else if("apply".equals(module))
		{
			kjMailForm.setTitle("关于申请：");
		}
		else if("allot".equals(module))
		{
			kjMailForm.setTitle("关于拨款：");
		}
		else if("user".equals(module))
		{
			kjMailForm.setTitle("科技处");
		}else if("meeting".equals(module))
		{
			kjMailForm.setTitle("关于会议：");
			KjSnsMeetingBase kjSnsMeetingBase = KjSnsMeetingBaseDAO.getInstance().findByPk(Long.parseLong(tableIds));
			
			String kjDictStatusId = kjSnsMeetingBase.getStatus();
			String strStatus = null;
			if("0".equals(kjDictStatusId)){
				strStatus = "凑时间中";
			}else if("1".equals(kjDictStatusId)){
				strStatus = "已确认";
			}
			
			firstObjName = kjSnsMeetingBase.getName()+"  当前状态："+strStatus;
			contentStrBuf.append("<p>会议名称："+kjSnsMeetingBase.getName()+"</p>"
					+"<p>会议英文名："+kjSnsMeetingBase.getOtherName()+"</p>"
					+"<p>会议状态："+strStatus+"</p>"
					+"<p>议题："+kjSnsMeetingBase.getIssue()+"</p>"
					+"<p>会议介绍："+kjSnsMeetingBase.getDescription()+"</p>"
					+"<p>开始时间："+kjSnsMeetingBase.getBeginTime()+"</p>"
					+"<p>结束时间："+kjSnsMeetingBase.getEndTime()+"</p>"
					+"<p>所在国家："+kjSnsMeetingBase.getCountry()+"</p>"
					+"<p>所在城市："+kjSnsMeetingBase.getCity()+"</p>"
					+"<p>详细地址："+kjSnsMeetingBase.getPlace()+"</p>"
					+"<p>主办单位："+kjSnsMeetingBase.getHostDept()+"</p>"
					+"<p>协办单位："+kjSnsMeetingBase.getAsistDept()+"</p>"
					+"<p>承办单位："+kjSnsMeetingBase.getUndertakeDept()+"</p>"
					+"<p>报名截止日期："+kjSnsMeetingBase.getRegestDeadline()+"</p>"
					+"<p>参会费："+kjSnsMeetingBase.getRegFee()+"</p>"
					+"<p>联系邮箱："+kjSnsMeetingBase.getEmail()+"</p>"
					+"<p>通讯方式："+kjSnsMeetingBase.getMailAddress()+"</p>"
					+"<p>邮编："+kjSnsMeetingBase.getZipCode()+"</p>"
					+"<p>备注："+kjSnsMeetingBase.getMemo()+"</p>"
					+"<p>请您登陆kj.tju.edu.cn 社交——社交会议——我的参会 确认参会信息。</P>");
			
		}
		List<String> toList = new ArrayList<String>();
		List<String> toListId = new ArrayList<String>();   //for patent email send  //yzw20151122
		List<String> toListName  =new ArrayList<String>();
		List<String> toAdd  =new ArrayList<String>();
		List<String> toSms  =new ArrayList<String>();
		KjUserDAO kjUserDao = KjUserDAO.getInstance();
		KjPatent kjPatent =new KjPatent();//DUAN 2014-02-27
//		for(int i=0;i<selectedList.size();i++){
//			System.out.println(selectedList.get(i));
//		}
		if(selectedList!=null && selectedList.size()>0){
			for (int i=0; i<selectedList.size(); i++){
				if(selectedList.get(i).length()==0){
					continue;
				}
				KjUser kjUser = null;
				if("project".equals(module)){
					ProjectDao projectDao = null;
					KjProject kjProject = projectDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjProject.getProjectName());
					contentStrBuf.append(kjProject.getProjectName() + " （校内编号"+kjProject.getProjectNo()+"），");
					
					if(kjProject.getEmployeeByLeaderId()!=null && kjProject.getEmployeeByLeaderId().getName()!=null)
					{
						toListName.add(kjProject.getEmployeeByLeaderId().getName());
					}
					else if(kjProject.getKjUser()!=null)
					{
						toListName.add(kjProject.getKjUser().getName());
					}
					else 
						toListName.add(" ");
					
					if(kjProject.getEmployeeByLeaderId()!=null && kjProject.getEmployeeByLeaderId().getName()!=null)
					{
						kjUser = kjUserDao.findByPk(kjProject.getEmployeeByLeaderId().getStaffId());
					}
					else if(kjProject.getKjUser()!=null)
					{
						kjUser = kjProject.getKjUser();
					}
			    	
			    	if(i==0)
			    	{
			    		firstObjName = kjProject.getProjectName();
			    	}
				}
				else if("allot".equals(module)){
					FundAllotDAO allotDAO = null;
					KjFundAllot allot = allotDAO.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(allot.getKjProject().getProjectName());
					contentStrBuf.append(allot.getKjProject().getProjectName() + " （校内编号"+allot.getKjProject().getProjectNo()+"），");
					
					if(allot.getKjProject().getEmployeeByLeaderId()!=null && allot.getKjProject().getEmployeeByLeaderId().getName()!=null)
					{
						toListName.add(allot.getKjProject().getEmployeeByLeaderId().getName());
					}
					else if(allot.getKjProject().getKjUser()!=null)
					{
						toListName.add(allot.getKjProject().getKjUser().getName());
					}
					else 
						toListName.add(" ");
					
					if(allot.getKjProject().getEmployeeByLeaderId()!=null && allot.getKjProject().getEmployeeByLeaderId().getName()!=null)
					{
						kjUser = kjUserDao.findByPk(allot.getKjProject().getEmployeeByLeaderId().getStaffId());
					}
					else if(allot.getKjProject().getKjUser()!=null)
					{
						kjUser = allot.getKjProject().getKjUser();
					}
			    	
			    	if(i==0)
			    	{
			    		firstObjName = allot.getKjProject().getProjectName();
			    	}
				}
				else if("invoice".equals(module)){
					KjInvoiceDao kjInvoiceDao = null;
					KjInvoice kjInvoice = kjInvoiceDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjInvoice.getKjProject().getProjectName());
					contentStrBuf.append(kjInvoice.getKjProject().getProjectName() + " ，");
					if(kjInvoice.getInvoiceUser()!=null){
						toListName.add(kjInvoice.getInvoiceUser().getName());
					}
//					toAdd.add(kjInvoice.getInvoiceUser().getEmail());
					if(kjInvoice.getInvoiceUser()!=null)
					{
						kjUser = kjUserDao.findByPk(kjInvoice.getInvoiceUser().getStaffId());
					}
					else if(kjInvoice.getKjInvoiceUser()!=null)
					{
						kjUser = kjInvoice.getKjInvoiceUser();
					}
					
					if(i==0)
			    	{
			    		firstObjName = kjInvoice.getKjProject().getProjectName();
			    	}
				}
				else if("thesis".equals(module)){
					KjThesisDao kjThesisDao = null;
					KjThesis kjThesis = kjThesisDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjThesis.getName());
					contentStrBuf.append(kjThesis.getName() + " ，");
					if(kjThesis.getAuthorName() !=null)
					{
						toListName.add(kjThesis.getAuthorName());
					}
					else
					{
						toListName.add(" ");
					}
//					toAdd.add(kjThesis.getAuthorId().getEmail());
					
					if(kjThesis.getAuthorId()!=null)
					{
						kjUser = kjUserDao.findByPk(kjThesis.getAuthorId().getStaffId());
					}
					else if(kjThesis.getKjAuthorId()!=null)
					{
						kjUser = kjThesis.getKjAuthorId();
					}
					
					if(i==0)
			    	{
			    		firstObjName = kjThesis.getName();
			    	}
				}
				else if("meetingFund".equals(module)){
					KjMeetingFundDao kjMeetingFundDao = null;
					KjMeetingFund kjMeetingFund = kjMeetingFundDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjMeetingFund.getKjMeeting().getMeetingName());
					toListName.add(kjMeetingFund.getKjMeeting().getCreateUser().getName());
//					toAdd.add(kjMeetingFund.getKjMeeting().getCreateUser().getEmail());
					
					if(kjMeetingFund.getKjMeeting().getCreateUser()!=null)
					{
						kjUser = kjUserDao.findByPk(kjMeetingFund.getKjMeeting().getCreateUser().getStaffId());
					}
					else if(kjMeetingFund.getKjMeeting().getKjCreateUser()!=null)
					{
						kjUser = kjMeetingFund.getKjMeeting().getKjCreateUser();
					}
					
					if(i==0)
			    	{
			    		firstObjName = kjMeetingFund.getKjMeeting().getMeetingName();
			    	}
				}
				else if("patent".equals(module) || "patentFee".equals(module)) //生成正文
				{
//					/KjPatentDao kjPatentDao = null;
					//inc.tech.patent.dao.KjPatentDao kjPatentDao =null;
					kjPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjPatent.getName());
					//contentStrBuf.append(kjPatent.getName() + " ，");
					if(kjPatent!=null && kjPatent.getKjFirstInventorId()!=null && kjPatent.getKjFirstInventorId().getName()!=null){
						toListName.add(kjPatent.getKjFirstInventorId().getName());
						toListId.add(kjPatent.getKjFirstInventorId().getStaffId());
					}else{
						toListName.add("无第一发明人");
					}
//					toAdd.add(kjPatent.getFirstInventorId().getEmail());
					kjUser = kjPatent.getKjFirstInventorId();
//					if(i==0){                                     这里限制只有选一个专利时才显示邮件正文
			    		firstObjName = "需尽快缴纳年费";
//			    		if(selectedList.size()==1){
			    			
			    			//zz 2017-10-19 再更新一遍年费
			    			KjPatentFeeAction feeAction = new KjPatentFeeAction();
//			    			try {
//			    				request.setAttribute("patentId", kjPatent.getPatentId().toString());
//								feeAction.getFeeSynchronization(mapping, form, request, response);
//							} 
//			    			catch (IOException e) { e.printStackTrace(); } 
//			    			catch (SAXException e) { e.printStackTrace(); } 
//			    			catch (InterruptedException e) { e.printStackTrace(); } 
//			    			catch (ParseException e) { e.printStackTrace(); } 
//			    			catch (DAOException e) { e.printStackTrace(); }
			    			
			    			KjTableTemplate kjTableTemplate = KjTableTemplateDao.getInstance().searchTable("专利费用", 15l);
			    			if(doGiveup.equals("true"))
			    			{
			    				kjTableTemplate = KjTableTemplateDao.getInstance().searchTable("强制放弃", 15l);
			    			}
			    			
			    			String thisContent = kjTableTemplate.getContent();
			    		//	System.out.println(thisContent);
			    			thisContent = thisContent.replaceAll("param_patentName", kjPatent.getName()==null?"":(i+1)+": "+kjPatent.getName());
			    			thisContent = thisContent.replaceAll("param_patentNo", kjPatent.getPatentNo()==null?"":kjPatent.getPatentNo());
			    			if(kjPatent.getKjFirstInventorId()!=null) 
			    			{ thisContent = thisContent.replaceAll("param_patentName", kjPatent.getKjFirstInventorId().getName()); }
			    			else  thisContent = thisContent.replaceAll("param_patentName", kjPatent.getAllInventorName()==null?"专利发明人":kjPatent.getAllInventorName());
			    				
			    			KjPatentFee remindFee = KjPatentFeeDao.getInstance().getRemindFeeByPatentId(kjPatent.getPatentId());
			    			String remindStr = KjPatentFeeDao.getInstance().getRemindStringByPatentFee(remindFee);
			    			
			    			thisContent = thisContent.replaceAll("param_feeNameAndAmount", remindStr.substring(remindStr.indexOf("其中")));
			    			List<KjProjectstaff> newProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 2L, "0");
			    			List<KjProjectstaff> oldProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 0L, "0");
			    			if(newProjectStaffList==null || newProjectStaffList.size()==0) newProjectStaffList = oldProjectStaffList;
			    			String allUsername = "";
			    			if(newProjectStaffList!=null)
			    			{
			    				for (int j = 0; j < newProjectStaffList.size(); j++) 
			    				{
			    					if(j==0) allUsername = newProjectStaffList.get(j).getKjUser().getName();
			    					else
			    					{
			    						if(newProjectStaffList.get(j).getKjUser()!=null)
			    						{
			    							allUsername = allUsername + "," + newProjectStaffList.get(j).getKjUser().getName();
			    						}
			    						else if(newProjectStaffList.get(j).getEmployee()!=null)
			    						{
			    							allUsername = allUsername + "," + newProjectStaffList.get(j).getEmployee().getName();
			    						}
			    						else
			    						{
			    							allUsername = allUsername + ",未知";
			    						}
			    					}
								}
			    			}
			    			thisContent = thisContent.replaceAll("param_allInventors", allUsername==null?"":allUsername);
			    			String acceptDate = DateUtil.convertDateToString(kjPatent.getAcceptDate());
			    			thisContent = thisContent.replaceAll("param_applyDate", acceptDate==null?"":acceptDate);
			    			
			    			if(doGiveup.equals("true"))
			    			{
								Long remainDays = 10000L; // 当前专利的剩余天数,以最小剩余天数为准
								Date expireDay = new Date(); // 专利离当前最小剩余天数对应的截止日期(用于找到该截止日期需要提醒的所有费用记录)
								if (remindFee != null) {
									remainDays = remindFee.getRemainDays();
									expireDay = remindFee.getExpiryDay();
								}
								// 强制放弃还有:
								// param_mailTime 已发送邮件时间
								// param_expireDays 逾期天数
								// param_cancledate 邮件发送时间加一周
								// param_feeDate 年费截止日期
								thisContent = thisContent.replaceAll("param_expireDays",(30l-remainDays)+"");
								thisContent = thisContent.replaceAll("param_feeDate",DateUtil.convertDateToString(expireDay));
								
								Calendar  calendar = Calendar.getInstance();   
							    calendar.setTime(new Date()); 
							    calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+7);//让日期加7  
								thisContent = thisContent.replaceAll("param_cancledate",DateUtil.convertDateToString(calendar.getTime()));
								
								List<KjMail> mailList = KjPatentFeeDao.getInstance().getThisYearFeeMails(kjPatent);
								if(mailList!=null && mailList.size()>0)
								{
									String param_mailTime = "";
									for (int j = 0; j < mailList.size(); j++) 
									{
										KjMail thisMail = mailList.get(j);
										Date mailDate = thisMail.getCreateDate();
										if(j>0) param_mailTime = param_mailTime + "和";
										param_mailTime = param_mailTime + DateUtil.convertDateToString("yyyy年MM月dd日",mailDate);
									}
									param_mailTime = param_mailTime + "，共计"+mailList.size()+"次";
									thisContent = thisContent.replaceAll("param_mailTime",param_mailTime);
								}
								else{thisContent = thisContent.replaceAll("param_mailTime","");}
							}
			    			//正文合并
			    			if(mark == 0){ //这是合并的第一个专利，不用切割
			    				contentStrBuf.append(thisContent);
			    				mark = 1;
			    			}else{        //需要对正文进行切割
			    				contentStrBuf.delete(contentStrBuf.indexOf("（缴费金额由系统自动计算生成"), contentStrBuf.length());
			    		//		System.out.println(thisContent.indexOf("以下是您的专利信息："));
			    				String newthisContent = thisContent.substring(thisContent.indexOf("以下是您的专利信息：")+10);
			    		//		System.out.println(newthisContent);
			    				contentStrBuf.append(newthisContent);
			    			}
// contentStrBuf.append("您的 "+kjPatent.getName()+" 专利即将到期，应缴纳金额
// "+(kjPatent.getCostId()==null?"":kjPatent.getCostId().getMoney())+"
// 元，金额由系统自动计算生成，仅供参考，所需缴费金额以实际为准。");
//			    		}
//			    		{
//			    			contentStrBuf.append("您有专利即将到达缴费日期，请尽快登录科技信息系统查询");
//			    		}
			    	}
//				}
				else if("apply".equals(module)){
					ApplyDAO applyDAO = null;
					KjApply kjApply = applyDAO.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjApply.getProjectName());
					//contentStrBuf.append(kjApply.getProjectName() + " ，");
					toListName.add(kjApply.getEmployeeByLeaderId().getName());
//					toAdd.add(kjApply.getEmployeeByLeaderId().getEmail());
					
					if(kjApply.getEmployeeByLeaderId()!=null)
					{
						kjUser = kjUserDao.findByPk(kjApply.getEmployeeByLeaderId().getStaffId());
					}
					else if(kjApply.getKjUserByLeaderId()!=null)
					{
						kjUser = kjApply.getKjUserByLeaderId();
					}
					
					if(i==0)
			    	{
			    		firstObjName = kjApply.getProjectName();			    		
			    	}
				}
				else if("user".equals(module)){
					KjUserDAO kjUserDAO = null;
					kjUser = kjUserDAO.getInstance().findByPk(selectedList.get(i));
//					System.out.println("kjUser"+kjUser.getName());
//					System.out.println("kjUser.getEmail()"+kjUser.getEmail());
					toList.add(kjUser.getName());
					toListName.add(kjUser.getName());
				}
				else if("meeting".equals(module)){
					// 1 取Meetingbase
					// 2 根据meetingbase 取meetingUser 是一个列表
					List<KjSnsMeetingUser> kjSnsMeetingUserList = KjSnsMeetingUserDAO.getInstance().findAllByMeetingId(tableIds);
					tableIds = "";
					if (kjSnsMeetingUserList != null){ 
						for(int k=0; k<kjSnsMeetingUserList.size(); k++){
							toList.add(kjSnsMeetingUserList.get(k).getKjUser().getName());
							toListName.add(kjSnsMeetingUserList.get(k).getKjUser().getName());
							
							kjUser = kjSnsMeetingUserList.get(k).getKjUser();
							
							if(k != 0){
								tableIds += ",";
 							}
							tableIds += kjSnsMeetingUserList.get(k).getId().toString();
						}
					}
				}				
				
				if(kjUser!=null && kjUser.getEmail()!=null && kjUser.getEmail().length()>0){
		    		toAdd.add(kjUser.getEmail());
		    	}
		    	else  { toAdd.add(" "); }
		    	
		    	if(kjUser!=null && kjUser.getCellPhone()!=null && kjUser.getCellPhone().length()>0){
		    		toSms.add(kjUser.getCellPhone());
		    	}
		    	else  { toSms.add(" "); }
		    	
		    	//duan 2014-02-27 由于需要给每个发明人都提醒，所以此处再循环一次 ----->  之前的第一发明人的提醒是没有意义的,只要给所有发明人都提醒就好了
		    	if("patent".equals(module) || "patentFee".equals(module)) // 查找所有专利发明人作为邮件接收人
		    	{				
					List<KjProjectstaff> kjProjectstaff=null;
					List<KjProjectstaff> projectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 0L, "0");
					List<KjProjectstaff> newProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 2L, "0");
					if(newProjectStaffList.size()!=0){//即存在新数据，要求所用全为新数据
						kjProjectstaff=newProjectStaffList;
					}
					else{//不存在新数据，用旧数据即可
						kjProjectstaff=projectStaffList;
					}
						if(kjProjectstaff.size()!=0){
							for(int x =0;x<kjProjectstaff.size();x++){
								if(kjProjectstaff.get(x).getPsOrder()!=1){
									toList.add(kjPatent.getName());
									kjUser = kjProjectstaff.get(x).getKjUser();
									
									if(kjUser == null && kjProjectstaff.get(x).getEmployee()!=null){
										kjUser = KjUserDAO.getInstance().findByPk(kjProjectstaff.get(x).getEmployee().getStaffId());
									}
									
									if(kjProjectstaff.get(x).getEmployee()!=null && kjProjectstaff.get(x).getEmployee().getName()!=null){
										toListName.add(kjProjectstaff.get(x).getEmployee().getName());
									}
									else if(kjUser!=null){
										toListName.add(kjUser.getName());
									}
									else{
										toListName.add("--");
									}
									
									if(kjUser!=null &&kjUser.getStaffId()!=null && kjUser.getStaffId().length()>0)
									{
										toListId.add(kjUser.getStaffId());
									}
									
									if(kjUser!=null && kjUser.getEmail()!=null && kjUser.getEmail().length()>0){
							    		toAdd.add(kjUser.getEmail());
							    	}
							    	else  { toAdd.add(" "); }
							    	
							    	if(kjUser!=null && kjUser.getCellPhone()!=null && kjUser.getCellPhone().length()>0){
							    		toSms.add(kjUser.getCellPhone());
							    	}
							    	else  { toSms.add(" "); }
								}
							}
						}
				}

//			int listNum = toList.size();
			}
		}
		
		//zz 2012-03-30 让邮件标题更有区别性
		kjMailForm.setTitle(kjMailForm.getTitle() + firstObjName + " 的提醒");
		
		//zz 2014-01-16
		if(otherModuleType.equals("fundFile" )){
			KjFundFile fundFile = FundFileDAO.getInstance().findByPk(Long.parseLong(otherModuleId));
			if(fundFile!=null)
			{
				kjMailForm.setTitle("天津大学科技系统:来款提醒");
				contentStrBuf = new  StringBuffer("");
				contentStrBuf.append("您的项目（"+"--"+"）来款已进入天津大学待拨户，信息如下： 来源："+fundFile.getKjDictOutlay().getOutlayName()+" ")
				.append(" 到款额："+fundFile.getFundSum()+"（万） ")
				.append(" 到款日期："+ DateUtil.convertDateToString(fundFile.getReceiveDate())+" ")
				.append(" 内容："+ fundFile.getFileContent()+" ")
				.append(" 入账账号："+ fundFile.getKjDictAccount().getAccount()+" ")
				.append(" 入账时间："+ DateUtil.convertDateToString(fundFile.getEnterDate())+" ")
				.append(" 请您及时登录科技系统申请拨款。")
				;
			}
		}
		
		//获取科研秘书列表
		List<KjGroupMember1> allSecList = MemberDAO.getInstance().getMemberList(MemberDAO.SEC_GROUP);
		List<KjGroupMember1> adminList = MemberDAO.getInstance().getMemberList(
				new Long[]{
						MemberDAO.ADMIN_GROUP,132l,133l,134l
						});
		
//		System.out.println("allSecList "+allSecList.size());
//		System.out.println("adminList "+adminList.size());
		
		List<KjGroupMember1> researchSecList = new ArrayList();
		for(int i=0;i<allSecList.size();i++){
			//maofm 2012/12/13  将kjuser改为employee
//			Employee thisUser = allSecList.get(i).getEmployee();
			KjUser thisUser = allSecList.get(i).getKjUser();
			if(thisUser==null || !thisUser.getStMark().equals("1")){
//				System.out.println("1");
				continue;
			}
			Boolean haveSame = false;
			for(int j=0;j<adminList.size();j++){
				//maofm 2012/12/13  将kjuser改为employee
//				Employee jUser = adminList.get(j).getEmployee();
				KjUser jUser = adminList.get(j).getKjUser();
				if(jUser!=null&&jUser.getStaffId().equals(thisUser.getStaffId()))
				{
					haveSame = true;
					break;
				}
			}
			if(!haveSame){
				researchSecList.add(allSecList.get(i));
			}
		}
//		System.out.println("researchSecList "+researchSecList.size());
		request.setAttribute("researchSecList", researchSecList);
		
//		contentStrBuf.append("【本邮件由科技系统 http://kj.tju.edu.cn 发出，请勿直接回复】");
//		contentStrBuf.append("【天津大学科学发展研究发展院 " + DateUtil.getDate(new Date())+"】");
		
//		System.out.println("toListName"+toListName);
//		System.out.println("toList"+toList);
//		System.out.println("toAdd"+toAdd);
		kjMailForm.setContent(contentStrBuf.toString());
		request.setAttribute("listNum", toList.size());			
		request.setAttribute("toList", toList);
		request.setAttribute("toListId", toListId);
		request.setAttribute("toListName", toListName);
		request.setAttribute("toAdd", toAdd);
		request.setAttribute("toSms", toSms);
		request.setAttribute("kjMailForm", kjMailForm);
		
		return mapping.findForward("sendMail");
	}
	
	private List<String> dealIds(String ids, String selectedItems, String unSelectedItems)
	{
//		System.out.println("ids = " + ids);
		if(ids.length()==0)
		{
			ids = selectedItems;
		}
		List<String> selectedList = new ArrayList<String>();
		String[] selectedArray = selectedItems.split(";");
		String[] unSelectedArray = unSelectedItems.split(";");
		String[] idsArray = ids.split(";");
		if(ids.length()>0)
		{
			for(int i=0; i<idsArray.length; i++)
				selectedList.add(idsArray[i]);
		}
		for(int i=0; i<selectedArray.length; i++)
		{
			if(!selectedList.contains(selectedArray[i]))
			{
				selectedList.add(selectedArray[i]);
			}
		}
		for(int i=0; i<unSelectedArray.length; i++)
		{
			if(selectedList.contains(unSelectedArray[i]))
				selectedList.remove(unSelectedArray[i]);
		}
		return selectedList;
	}
}