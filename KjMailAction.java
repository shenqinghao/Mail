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
	private static String MENU_SIGN="Project";	//��Ŀ����
	private static String CAUDIT="CAudit";	//ѧԺ���Ȩ��
	private static String UAUDIT="UAudit";	//ѧУ���Ȩ��
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
	 * ���٣�2013-08-19 �����ʼ���ģ�飬��ʾ���ģ�������<br>
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
	    
	    if("psw".equals(module) && kjMail.getContent().indexOf("����")>0)
	    {
	    	kjMail.setContent(kjMail.getContent().substring(0,kjMail.getContent().indexOf("����")) + "����Ϊ *********");
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
	// ����ģ���б��������ȡ��Ӧ����Ա����
	public List<String> getUserId(String id/*��ǰ�������*/, String mailType)
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
		
		if("patent".equals(mailType) || "patentFee".equals(mailType)){  //����ר��������ȡ�����ʼ����û��б�,����Ĭ�ϵ������з����� 
			//��������Ϣ�б�,��Ϊ�ɵ����ݺ��µ�����
			List<KjProjectstaff> projectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(Long.parseLong(id), 0L, "0");
			List<KjProjectstaff> newProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(Long.parseLong(id), 2L, "0");
			
			//���û�и���ר����Ϣ,��������ʾ�ɵ�ר��������
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
	 * zz 2013-08-19 �����ʼ����ͻ�ִ���ܣ���һ����ַ����ʧ�ܲ���Ӱ��������ַ�ķ���
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
		//2014-4-30 by zhangm �����ʼ����߶��ţ����߶���ͬʱ
		String sendSms = ParamUtil.getParameter(request,"sendSms");
		String sendMail = ParamUtil.getParameter(request,"sendMail");
		
		//��÷����˵���Ϣ
		KjUserDAO kjUserDao = KjUserDAO.getInstance();
    	Long mailId  = 0l;
    	
    	//System.out.println("start##################");
    	//����ռ��˵���Ϣ
		if(toList!=null && toList.length()>0){
			String[] tableIds = toList.split(",");
			String[] secIds = toSec.split(",");
			
			List<String> toUserId = new ArrayList<String>();
			List<String> toTableId = new ArrayList<String>();//zz 2014-02-28 �� toUserId ��ȫƽ�е�1������
			toList ="";                                      //lyb 2015-01-13 �˴������ÿ� ��Ϊһ��ר����Ҫ�����еķ����˷��ʼ�
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
					//zz ���� toTableId ���뼴��
					toTableId.add("");
				}
			}
			
		
			//start - �����ʼ�
			String toAddressList = "";
			String sendMailStatusItem = "δ�ύ��������";
			
			//System.out.println("0-�����ʼ�!");
			
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
								// �����Ự
								Session session = Session.getInstance(p);
								Message msg = new MimeMessage(session); // ������Ϣ
								
								// zz 2012-03-26
								String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
		
								msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddress(), fromMail));
		
								msg.addRecipient(Message.RecipientType.TO, new InternetAddress(kjUser.getEmail()));
							
								// msg.setSentDate(); // ��������
								if (title != null && title.length() > 0)
								{
									msg.setSubject(kjUser.getName()+":"+title); // ����
								}
								if (content != null && content.length() > 0){
//									msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ظ���"); // ����
									msg.setContent(content, "text/html;charset = gbk");  //zz 2015-04-30
								}else{
									if(mailType.equals("patentFee")){
										content = "����ר����������ɷ����ڣ��뾡���¼�Ƽ���Ϣϵͳ��ѯ";
//										msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ظ���"); // ����
										msg.setContent(content, "text/html;charset = gbk");  //zz 2015-04-30
									}
								}
								
								if(toAddressList.equals("")){
									toAddressList = kjUser.getEmail();
								}
								else{
									toAddressList = toAddressList + "," + kjUser.getEmail();
								}
								
								// �ʼ�������������֤,���ص��Ե�ʱ��Ҫ����
//								Transport tran = session.getTransport("smtp");
//								tran.connect(TechSystem.getInstance().getTechConfig() .getFromServer(),
//											TechSystem.getInstance() .getTechConfig().getFromAddress(), 
//											TechSystem .getInstance().getTechConfig() .getFromPassword() );
//								tran.sendMessage(msg, msg.getAllRecipients()); // ����
								
								System.out.println("send - " + kjUser.getEmail() );
								sendCount ++;
								if(sendCount%50==0)
								{
									System.out.println(sendCount);
								}
								isSend = true;
							} 
							catch (AddressException e) {
								//sendMap.put(kjUser.getEmail(), "�ռ���ַ����");
								sendMailStatusItem = "�ռ���ַ����";
							}
							catch (MessagingException e) {
								//sendMap.put(kjUser.getEmail(), "�ʼ����ͳ���");
								sendMailStatusItem = "�ʼ����ͳ���";
							} // ������
						}else{
							//sendMap.put(thisUserId, "�ռ��˻��ַ��Ϣ������");
							sendMailStatusItem = "�ռ��˻��ַ��Ϣ������";
						}
						if(isSend){
							sendMailStatusItem = "�ʼ����ͳɹ�";
						}
						else{
							sendMailStatusItem = "�ʼ�����ʧ��";
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
			//end - �����ʼ�
			
			//TODO 2018-12-12 SQH �����Ǹ����û�idѭ�������ʼ��������Ǹ����û�����¼��ĳ��ͣ������ʼ�
			//��ȡ�������б�
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
								// �����Ự
								Session session = Session.getInstance(p);
								Message msg = new MimeMessage(session); // ������Ϣ
								
								// zz 2012-03-26
								String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
	
								msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddressP(), fromMail));
		
								msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toCClist.get(i)));
							
								// msg.setSentDate(); // ��������
								if (title != null && title.length() > 0)
									msg.setSubject(title); // ����
	//							if (content != null && content.length() > 0){
	//								msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ظ���"); // ����
	//							}
	//							msg.setText(content); // ����
								msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
								
								if(toAddressList.equals("")){
									toAddressList = toCClist.get(i);
								}
								else{
									toAddressList = toAddressList + "," + toCClist.get(i);
								}
								
								// �ʼ�������������֤,���ص��Ե�ʱ��Ҫ����
	//							System.out.println(TechSystem.getInstance().getTechConfig() .getFromServerP());
	//							System.out.println(TechSystem.getInstance().getTechConfig() .getFromAddressP());
	//							System.out.println(TechSystem.getInstance().getTechConfig() .getFromPasswordP());
	//							
								
								Transport tran = session.getTransport("smtp");
								tran.connect(TechSystem.getInstance().getTechConfig() .getFromServerP(),
											TechSystem.getInstance() .getTechConfig().getFromAddressP(), 
											TechSystem .getInstance().getTechConfig() .getFromPasswordP() );
								tran.sendMessage(msg, msg.getAllRecipients()); // ����
								
								isSend = true;
							} 
							catch (AddressException e) {
								//sendMap.put(kjUser.getEmail(), "�ռ���ַ����");
								sendMailStatusItem = "�ռ���ַ����";
								e.printStackTrace();
							}
							catch (MessagingException e) {
								//sendMap.put(kjUser.getEmail(), "�ʼ����ͳ���");
								sendMailStatusItem = "�ʼ����ͳ���";
								e.printStackTrace();
							} // ������
						}else{
							//sendMap.put(thisUserId, "�ռ��˻��ַ��Ϣ������");
							sendMailStatusItem = "�ռ��˻��ַ��Ϣ������";
						}
						if(isSend){
							sendMailStatusItem = "�ʼ����ͳɹ�";
						}
						else{
							sendMailStatusItem = "�ʼ�����ʧ��";
						}
					}
				}
			}
			
			//start - ���Ͷ���
			String sendSmsStatusItem = "δ�ύ��������";
			//System.out.println("1-���Ͷ���!");
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
							//sendSmsMap.put(kjUser.getCellPhone(), "���ŷ��ͳɹ�");
							sendSmsStatusItem = "���ŷ��ͳɹ�";
							isSmsSend = true;
						}
						else{
							//sendSmsMap.put(kjUser.getCellPhone(), "���ŷ���ʧ��");
							sendSmsStatusItem = "���ŷ���ʧ��";
						}
					}
					else{
						//sendSmsMap.put(thisUserId, "�û��˻��ֻ�����Ϣ����");
						sendSmsStatusItem = "�û��ֻ�������Ϣ����";
//						//System.out.println(toId.get(i)+ " - �û��˻��ֻ�����Ϣ����");
					}
					/**
					 * zz 2014-02-28 ��tableid�ı���ɹ���Ϣ �ŵ�ר����2��map��
					 */
					if(isSmsSend){
						sendSmsStatusItem = "���ŷ��ͳɹ�";
					}
					else{
						sendSmsStatusItem = "���ŷ���ʧ��";
					}
					
				}
				if(i == 0){
					sendSmsStatus = sendSmsStatusItem;
				}else{
					sendSmsStatus = sendSmsStatus + "," +sendSmsStatusItem;
				}
			}//end for i-loop
			if( sendSms.equalsIgnoreCase("true") ){title = title +"���ѷ����ţ�";}
			//end - ���Ͷ���		
			
			//���ͼ�¼�������ݿ�
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
		
		//Ҫ�ܿ�������ʼ��������˵�����״̬ 2013-08-19
		
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
	 * zz 2015-10-27 Ⱥ��ר������ʼ�
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
		
		//zz 2012-03-30 ���ʼ��������������
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
		//TODO ȡ��ģ���б���ʾ��ҳ���ϣ�������Ĭ�ϱ��������
		
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
				kjMailForm.setTitle("����ר����");
				
				/*if("patent".equals(module) || "patentFee".equals(module))
				{
					
					String kjDictStatusId = ParamUtil.getParameter(request, "status");
					if("5".equals(kjDictStatusId)){
						contentStrBuf.append("��������ר���ȴ���ˣ�");
					}else if("6".equals(kjDictStatusId)){
						contentStrBuf.append("��������ר��Ժ�����ͨ����");
					}else if("8".equals(kjDictStatusId)){
						contentStrBuf.append("��������ר��У�����ͨ����");	
					}else if("9".equals(kjDictStatusId)){
						contentStrBuf.append("��������ר����˲��أ�");
					}
				}*/
				
				if("patent".equals(module) || "patentFee".equals(module)) //��������
				{
//					/KjPatentDao kjPatentDao = null;
					//inc.tech.patent.dao.KjPatentDao kjPatentDao =null;
					kjPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toItemList.add(kjPatent.getName());
					//contentStrBuf.append(kjPatent.getName() + " ��");
					/*if(kjPatent!=null && kjPatent.getKjFirstInventorId()!=null && kjPatent.getKjFirstInventorId().getName()!=null){
						toListName.add(kjPatent.getKjFirstInventorId().getName());
					}else{
						toListName.add("�޵�һ������");
					}*/
					kjUser = kjPatent.getKjFirstInventorId();
			    	firstObjName = "��"+kjPatent.getName()+"��" + " �辡��������";
			    	
			    			KjTableTemplate kjTableTemplate = KjTableTemplateDao.getInstance().searchTable("ר������", 15l);
			    			String thisContent = kjTableTemplate.getContent();
			    			thisContent = thisContent.replaceAll("param_patentName", kjPatent.getName()==null?"":kjPatent.getName());
			    			thisContent = thisContent.replaceAll("param_patentNo", kjPatent.getPatentNo()==null?"":kjPatent.getPatentNo());
			    			thisContent = thisContent.replaceAll("param_patentName", kjPatent.getKjFirstInventorId().getName());
			    			String remindStr = KjPatentFeeDao.getInstance().getRemindStringByPatentId(kjPatent.getPatentId());
			    			thisContent = thisContent.replaceAll("param_feeNameAndAmount", remindStr.substring(remindStr.indexOf("����")));
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
				 kjMailForm.setTitle(kjMailForm.getTitle() + firstObjName + " ������");
				 kjMailForm.setContent(contentStrBuf.toString());
				 String content = contentStrBuf.toString();
				 String title = kjMailForm.getTitle();
				 String sendMailStatus = "";
				 String sendSmsStatus = "";
				 
				  //��ȡ������Ա
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
				 //start - �����ʼ�
				 String toAddressList = "";
				 String sendMailStatusItem = "δ�ύ��������";
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
										// �����Ự
										Session session2 = Session.getInstance(p);
										Message msg = new MimeMessage(session2); // ������Ϣ
										
										// zz 2012-03-26
										String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
				
										msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddress(), fromMail));
				
										msg.addRecipient(Message.RecipientType.TO, new InternetAddress(kjUser.getEmail()));
									
										// msg.setSentDate(); // ��������
										if (title != null && title.length() > 0)
											msg.setSubject(title); // ����
//										if (content != null && content.length() > 0){
//											msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ظ���"); // ����
//										}
//										msg.setText(content); // ����
										msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
										
										if(toAddressList.equals("")){
											toAddressList = kjUser.getEmail();
										}
										else{
											toAddressList = toAddressList + "," + kjUser.getEmail();
										}
										
										// �ʼ�������������֤,���ص��Ե�ʱ��Ҫ����
										Transport tran = session2.getTransport("smtp");
										tran.connect(TechSystem.getInstance().getTechConfig() .getFromServer(),
													TechSystem.getInstance() .getTechConfig().getFromAddress(), 
													TechSystem .getInstance().getTechConfig() .getFromPassword() );
										tran.sendMessage(msg, msg.getAllRecipients()); // ����
										
										isSend = true;
									} 
									catch (AddressException e) {
										//sendMap.put(kjUser.getEmail(), "�ռ���ַ����");
										sendMailStatusItem = "�ռ���ַ����";
									}
									catch (MessagingException e) {
										//sendMap.put(kjUser.getEmail(), "�ʼ����ͳ���");
										sendMailStatusItem = "�ʼ����ͳ���";
									} // ������
								}else{
									//sendMap.put(thisUserId, "�ռ��˻��ַ��Ϣ������");
									sendMailStatusItem = "�ռ��˻��ַ��Ϣ������";
								}
								if(isSend){
									sendMailStatusItem = "�ʼ����ͳɹ�";
								}
								else{
									sendMailStatusItem = "�ʼ�����ʧ��";
								}
							}
							if(j == 0){
								sendMailStatus = sendMailStatusItem;
							}else{
								sendMailStatus = sendMailStatus + "," +sendMailStatusItem;
							}
						}//end for i-loop 
				 }
				
				//���ͼ�¼�������ݿ�
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
	 * lyb-2015-01-13 ר����ÿ��id�洢һ���ʼ����ͼ�¼
	 * ���������鷢�ʼ���ʱ����
	 * zz 2017-09-05 ����Ϊר��ר������
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
		//2014-4-30 by zhangm �����ʼ����߶��ţ����߶���ͬʱ
		String sendSms = ParamUtil.getParameter(request,"sendSms");
		String sendMail = ParamUtil.getParameter(request,"sendMail");
		String doGiveup = ParamUtil.getParameter(request,"doGiveup");
		
//		System.out.println(" sendMailEach doGiveup " + doGiveup);//ok
		
		//��÷����˵���Ϣ
		KjUserDAO kjUserDao = KjUserDAO.getInstance();
    	Long mailId  = 0l;
    	
    	//���ڴ洢֮��Ļ���
    	List<String> tableNameList=new ArrayList<String>();
	    List<String> toSms = new ArrayList<String>();
	    List<String> toAdd = new ArrayList<String>();
	    List<String> toCCAdd = new ArrayList<String>();   //�����˵�ַ����ʹ��
	    String       sendMailStatusString = "";
	    String       sendSmsStatusString  = "";
	    List<String> userList = new ArrayList<String>();
    	HashMap patentIdMap = new HashMap<String, String>();
    	//ÿ��ר�������洢һ�����ʼ���ʷ��¼
		if(toList!=null && toList.length()>0){
			String[] tableIds = toList.split(",");
		//	System.out.println(tableIds.length);
			String toListIdMail = (String)request.getParameter("toListIdMail");
			//System.out.println(toListIdMail);
			for (int k=0; k<tableIds.length; k++){       //���ÿ��ר��,��װ������ר���ķ������͵������ֶ�
				List<String> toUserId = new ArrayList<String>();
				List<String> toTableId = new ArrayList<String>();
				toList = tableIds[k]+",";       
//				if(content==null || content.length()==0){  //���û��Ĭ�ϵĻ����Լ��޸Ĺ���������ϵͳ�Զ�����
//					content = KjPatentFeeDao.getInstance().getRemindStringByPatentId(Long.parseLong(tableIds[k]));
//				}
				List<String> thisUserIds =  getUserId(tableIds[k], mailType);
				KjPatent thisPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(tableIds[k]));
				//�ϲ��������ж��Ƿ���ͬһ�������˵��ʼ������ǣ�ֻ����һ��
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
				
				//start - �����ʼ�
				String toAddressList = "";
				String sendMailStatusItem = "δ�ύ��������";
				
				//System.out.println("0-�����ʼ�!");
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
									// �����Ự
									Session session = Session.getInstance(p);
									Message msg = new MimeMessage(session); // ������Ϣ
									
									// zz 2012-03-26
									String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
			
									msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddressP(), fromMail));
			
									msg.addRecipient(Message.RecipientType.TO, new InternetAddress(kjUser.getEmail()));
								
									// msg.setSentDate(); // ��������
									if (title != null && title.length() > 0)
										msg.setSubject(title); // ����
//									if (content != null && content.length() > 0){
//										msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ظ���"); // ����
//									}
//									msg.setText(content); // ����
									msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
									
									if(toAddressList.equals("")){
										toAddressList = kjUser.getEmail();
									}
									else{
										toAddressList = toAddressList + "," + kjUser.getEmail();
									}
									
									// �ʼ�������������֤,���ص��Ե�ʱ��Ҫ����
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromServerP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromAddressP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromPasswordP());
								
									
									Transport tran = session.getTransport("smtp");
									tran.connect(TechSystem.getInstance().getTechConfig() .getFromServerP(),
												TechSystem.getInstance() .getTechConfig().getFromAddressP(), 
												TechSystem .getInstance().getTechConfig() .getFromPasswordP() );
									tran.sendMessage(msg, msg.getAllRecipients()); // ����
									
									isSend = true;
								} 
								catch (AddressException e) {
									//sendMap.put(kjUser.getEmail(), "�ռ���ַ����");
									sendMailStatusItem = "�ռ���ַ����";
									e.printStackTrace();
								}
								catch (MessagingException e) {
									//sendMap.put(kjUser.getEmail(), "�ʼ����ͳ���");
									sendMailStatusItem = "�ʼ����ͳ���";
									e.printStackTrace();
								} // ������
							}else{
								//sendMap.put(thisUserId, "�ռ��˻��ַ��Ϣ������");
								sendMailStatusItem = "�ռ��˻��ַ��Ϣ������";
							}
							if(isSend){
								sendMailStatusItem = "�ʼ����ͳɹ�";
							}
							else{
								sendMailStatusItem = "�ʼ�����ʧ��";
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
				//end - �����ʼ�
				
				//start -�������˷����ʼ�
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
									// �����Ự
									Session session = Session.getInstance(p);
									Message msg = new MimeMessage(session); // ������Ϣ
									
									// zz 2012-03-26
									String fromMail = MimeUtility.encodeText( "kj.tju.edu.cn", "gb2312", "B");
		
									msg.setFrom(new InternetAddress( TechSystem.getInstance().getTechConfig().getFromAddressP(), fromMail));
			
									msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toCClist.get(i)));
								
									// msg.setSentDate(); // ��������
									if (title != null && title.length() > 0)
										msg.setSubject(title); // ����
		//							if (content != null && content.length() > 0){
		//								msg.setText(content.replaceAll("<p>", "") .replaceAll("</p>", "").replaceAll( "<br />", "")+"�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ظ���"); // ����
		//							}
		//							msg.setText(content); // ����
									msg.setContent(content, "text/html;charset = gbk");  //zz 215-04-30
									
									if(toAddressList.equals("")){
										toAddressList = toCClist.get(i);
									}
									else{
										toAddressList = toAddressList + "," + toCClist.get(i);
									}
									
									// �ʼ�������������֤,���ص��Ե�ʱ��Ҫ����
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromServerP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromAddressP());
//									System.out.println(TechSystem.getInstance().getTechConfig() .getFromPasswordP());
							
									
									Transport tran = session.getTransport("smtp");
									tran.connect(TechSystem.getInstance().getTechConfig() .getFromServerP(),
												TechSystem.getInstance() .getTechConfig().getFromAddressP(), 
												TechSystem .getInstance().getTechConfig() .getFromPasswordP() );
									tran.sendMessage(msg, msg.getAllRecipients()); // ����
									
									isSend = true;
								} 
								catch (AddressException e) {
									//sendMap.put(kjUser.getEmail(), "�ռ���ַ����");
									sendMailStatusItem = "�ռ���ַ����";
									e.printStackTrace();
								}
								catch (MessagingException e) {
									//sendMap.put(kjUser.getEmail(), "�ʼ����ͳ���");
									sendMailStatusItem = "�ʼ����ͳ���";
									e.printStackTrace();
								} // ������
							}else{
								//sendMap.put(thisUserId, "�ռ��˻��ַ��Ϣ������");
								sendMailStatusItem = "�ռ��˻��ַ��Ϣ������";
							}
							if(isSend){
								sendMailStatusItem = "�ʼ����ͳɹ�";
							}
							else{
								sendMailStatusItem = "�ʼ�����ʧ��";
							}
						}
					}
				}
				//end-�������˷����ʼ�
				
				//start - ���Ͷ���
				String sendSmsStatusItem = "δ�ύ��������";
				//System.out.println("1-���Ͷ���!");
				for (int i = 0; i < toUserId.size(); i++){
					Boolean isSmsSend = false;
					String thisTableId = toTableId.get(i);
					String thisUserId = toUserId.get(i);
					
					KjUser kjUser = kjUserDao.findByPk(thisUserId);
					
					if(sendSms.equalsIgnoreCase("true")){
						if (kjUser != null && kjUser.getCellPhone() != null && SmsUtil.isMobileNO(kjUser.getCellPhone())){
							content = KjPatentFeeDao.getInstance().getRemindStringByPatentId(Long.parseLong(tableIds[k]));//zz 2015-04-30  ��ֹ�ַ�̫��
							String filterdContent = StringUtil.splitAndFilterString(content,200);
							
							Boolean sendOK = SmsUtil.sendOneSms(filterdContent, kjUser.getCellPhone());
							if( sendOK ){
								//sendSmsMap.put(kjUser.getCellPhone(), "���ŷ��ͳɹ�");
								sendSmsStatusItem = "���ŷ��ͳɹ�";
								isSmsSend = true;
							}
							else{
								//sendSmsMap.put(kjUser.getCellPhone(), "���ŷ���ʧ��");
								sendSmsStatusItem = "���ŷ���ʧ��";
							}
						}
						else{
							//sendSmsMap.put(thisUserId, "�û��˻��ֻ�����Ϣ����");
							sendSmsStatusItem = "�û��ֻ�������Ϣ����";
//							//System.out.println(toId.get(i)+ " - �û��˻��ֻ�����Ϣ����");
						}
						/**
						 * zz 2014-02-28 ��tableid�ı���ɹ���Ϣ �ŵ�ר����2��map��
						 */
						if(isSmsSend){
							sendSmsStatusItem = "���ŷ��ͳɹ�";
						}
						else{
							sendSmsStatusItem = "���ŷ���ʧ��";
						}
						
					}
					if(i == 0){
						sendSmsStatus = sendSmsStatusItem;
					}else{
						sendSmsStatus = sendSmsStatus + "," +sendSmsStatusItem;
					}
				}//end for i-loop
				if( sendSms.equalsIgnoreCase("true") ){title = title +"���ѷ����ţ�";}
				//end - ���Ͷ���		
				
				//���ͼ�¼�������ݿ�
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
	    request.setAttribute("toCCAdd", toCCAdd); //���ڳ����˵�ַ�Ļ���
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
		 * SQH �жϺϲ����͵��ʼ��Ƿ���ͬһ��������
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
			out.println("��ͬһ������");
			out.flush();
			out.close();	
		}
		
		return null;
	}
	
	/**
	 * zz 2013-09-10 ���Ӷ���֧��<br>
	 * zz 2014-01-16 ��չ�������ļ�<br>
	 * zz 2017-10-19 ��չ��ǿ�Ʒ��� doGiveup = true ��ʾǿ�Ʒ���<br>
	 * ZZ SQH 2018-12-12 ֧�֡��ϲ��ʼ�����ť�����ڿ��Զ��ר��������ƴ�ӡ����������˿����г�����ҪĬ��ѡ�У���jsp����
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
		
		//zz 2012-03-30 ���ʼ��������������
		String firstObjName = "";
		int mark = 0;  //�����ϲ��ʼ�����
		
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
		
//		String mark = ParamUtil.getParameter(request, "mark");   //1Ϊ����Ϣ��2Ϊ�ϲ�����Ϣ
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
		//TODO ȡ��ģ���б���ʾ��ҳ���ϣ�������Ĭ�ϱ��������
		StringBuffer contentStrBuf = new StringBuffer("");
		if("project".equals(module)){
			
			String kjDictStatusId = ParamUtil.getParameter(request, "kjDictStatusId");
			kjMailForm.setTitle("������Ŀ��");
			if("5".equals(kjDictStatusId)){
				contentStrBuf.append("����������Ŀ�ȴ���ˣ�");
			}else if("11".equals(kjDictStatusId)){
				contentStrBuf.append("����������Ŀ�����");
			}else if("16".equals(kjDictStatusId)){
				contentStrBuf.append("����������Ŀ�����У�");	
			}else if("10".equals(kjDictStatusId)){
				contentStrBuf.append("����������Ŀ�����");
			}else if("6".equals(kjDictStatusId)){
				contentStrBuf.append("����������ĿԺ�����ͨ����");
			}else if("9".equals(kjDictStatusId)){
				contentStrBuf.append("����������Ŀ��˲��أ�");
			}else if("13".equals(kjDictStatusId)){
				contentStrBuf.append("����������Ŀ�ѽ��⣺");
			}
		}
		else if("invoice".equals(module))
		{
			kjMailForm.setTitle("���ڷ�Ʊ��");
			String kjDictStatusId = ParamUtil.getParameter(request, "invoiceType");
			if("��������".equals(kjDictStatusId)){
				contentStrBuf.append("����������");
			}else if("��������".equals(kjDictStatusId)){
				contentStrBuf.append("��������");
			}else if("����ת��".equals(kjDictStatusId)){
				contentStrBuf.append("����ת�ã�");	
			}else if("������ѯ".equals(kjDictStatusId)){
				contentStrBuf.append("������ѯ��");
			}else if("���з�".equals(kjDictStatusId)){
				contentStrBuf.append("���зѣ�");
			}
		}
		else if("thesis".equals(module))
		{
			kjMailForm.setTitle("�������ģ�");
			String kjDictStatusId = ParamUtil.getParameter(request, "status");
			if("5".equals(kjDictStatusId)){
				contentStrBuf.append("�����������ĵȴ���ˣ�");
			}else if("6".equals(kjDictStatusId)){
				contentStrBuf.append("������������Ժ�����ͨ����");
			}else if("8".equals(kjDictStatusId)){
				contentStrBuf.append("������������У�����ͨ����");	
			}else if("9".equals(kjDictStatusId)){
				contentStrBuf.append("��������������˲��أ�");
			}
		}
		else if("patent".equals(module) || "patentFee".equals(module))
		{
			kjMailForm.setTitle("����ר����");
			String kjDictStatusId = ParamUtil.getParameter(request, "status");
			if("5".equals(kjDictStatusId)){
				contentStrBuf.append("��������ר���ȴ���ˣ�");
			}else if("6".equals(kjDictStatusId)){
				contentStrBuf.append("��������ר��Ժ�����ͨ����");
			}else if("8".equals(kjDictStatusId)){
				contentStrBuf.append("��������ר��У�����ͨ����");	
			}else if("9".equals(kjDictStatusId)){
				contentStrBuf.append("��������ר����˲��أ�");
			}
		}
		else if("apply".equals(module))
		{
			kjMailForm.setTitle("�������룺");
		}
		else if("allot".equals(module))
		{
			kjMailForm.setTitle("���ڲ��");
		}
		else if("user".equals(module))
		{
			kjMailForm.setTitle("�Ƽ���");
		}else if("meeting".equals(module))
		{
			kjMailForm.setTitle("���ڻ��飺");
			KjSnsMeetingBase kjSnsMeetingBase = KjSnsMeetingBaseDAO.getInstance().findByPk(Long.parseLong(tableIds));
			
			String kjDictStatusId = kjSnsMeetingBase.getStatus();
			String strStatus = null;
			if("0".equals(kjDictStatusId)){
				strStatus = "��ʱ����";
			}else if("1".equals(kjDictStatusId)){
				strStatus = "��ȷ��";
			}
			
			firstObjName = kjSnsMeetingBase.getName()+"  ��ǰ״̬��"+strStatus;
			contentStrBuf.append("<p>�������ƣ�"+kjSnsMeetingBase.getName()+"</p>"
					+"<p>����Ӣ������"+kjSnsMeetingBase.getOtherName()+"</p>"
					+"<p>����״̬��"+strStatus+"</p>"
					+"<p>���⣺"+kjSnsMeetingBase.getIssue()+"</p>"
					+"<p>������ܣ�"+kjSnsMeetingBase.getDescription()+"</p>"
					+"<p>��ʼʱ�䣺"+kjSnsMeetingBase.getBeginTime()+"</p>"
					+"<p>����ʱ�䣺"+kjSnsMeetingBase.getEndTime()+"</p>"
					+"<p>���ڹ��ң�"+kjSnsMeetingBase.getCountry()+"</p>"
					+"<p>���ڳ��У�"+kjSnsMeetingBase.getCity()+"</p>"
					+"<p>��ϸ��ַ��"+kjSnsMeetingBase.getPlace()+"</p>"
					+"<p>���쵥λ��"+kjSnsMeetingBase.getHostDept()+"</p>"
					+"<p>Э�쵥λ��"+kjSnsMeetingBase.getAsistDept()+"</p>"
					+"<p>�а쵥λ��"+kjSnsMeetingBase.getUndertakeDept()+"</p>"
					+"<p>������ֹ���ڣ�"+kjSnsMeetingBase.getRegestDeadline()+"</p>"
					+"<p>�λ�ѣ�"+kjSnsMeetingBase.getRegFee()+"</p>"
					+"<p>��ϵ���䣺"+kjSnsMeetingBase.getEmail()+"</p>"
					+"<p>ͨѶ��ʽ��"+kjSnsMeetingBase.getMailAddress()+"</p>"
					+"<p>�ʱࣺ"+kjSnsMeetingBase.getZipCode()+"</p>"
					+"<p>��ע��"+kjSnsMeetingBase.getMemo()+"</p>"
					+"<p>������½kj.tju.edu.cn �罻�����罻���顪���ҵĲλ� ȷ�ϲλ���Ϣ��</P>");
			
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
					contentStrBuf.append(kjProject.getProjectName() + " ��У�ڱ��"+kjProject.getProjectNo()+"����");
					
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
					contentStrBuf.append(allot.getKjProject().getProjectName() + " ��У�ڱ��"+allot.getKjProject().getProjectNo()+"����");
					
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
					contentStrBuf.append(kjInvoice.getKjProject().getProjectName() + " ��");
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
					contentStrBuf.append(kjThesis.getName() + " ��");
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
				else if("patent".equals(module) || "patentFee".equals(module)) //��������
				{
//					/KjPatentDao kjPatentDao = null;
					//inc.tech.patent.dao.KjPatentDao kjPatentDao =null;
					kjPatent = KjPatentDao.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjPatent.getName());
					//contentStrBuf.append(kjPatent.getName() + " ��");
					if(kjPatent!=null && kjPatent.getKjFirstInventorId()!=null && kjPatent.getKjFirstInventorId().getName()!=null){
						toListName.add(kjPatent.getKjFirstInventorId().getName());
						toListId.add(kjPatent.getKjFirstInventorId().getStaffId());
					}else{
						toListName.add("�޵�һ������");
					}
//					toAdd.add(kjPatent.getFirstInventorId().getEmail());
					kjUser = kjPatent.getKjFirstInventorId();
//					if(i==0){                                     ��������ֻ��ѡһ��ר��ʱ����ʾ�ʼ�����
			    		firstObjName = "�辡��������";
//			    		if(selectedList.size()==1){
			    			
			    			//zz 2017-10-19 �ٸ���һ�����
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
			    			
			    			KjTableTemplate kjTableTemplate = KjTableTemplateDao.getInstance().searchTable("ר������", 15l);
			    			if(doGiveup.equals("true"))
			    			{
			    				kjTableTemplate = KjTableTemplateDao.getInstance().searchTable("ǿ�Ʒ���", 15l);
			    			}
			    			
			    			String thisContent = kjTableTemplate.getContent();
			    		//	System.out.println(thisContent);
			    			thisContent = thisContent.replaceAll("param_patentName", kjPatent.getName()==null?"":(i+1)+": "+kjPatent.getName());
			    			thisContent = thisContent.replaceAll("param_patentNo", kjPatent.getPatentNo()==null?"":kjPatent.getPatentNo());
			    			if(kjPatent.getKjFirstInventorId()!=null) 
			    			{ thisContent = thisContent.replaceAll("param_patentName", kjPatent.getKjFirstInventorId().getName()); }
			    			else  thisContent = thisContent.replaceAll("param_patentName", kjPatent.getAllInventorName()==null?"ר��������":kjPatent.getAllInventorName());
			    				
			    			KjPatentFee remindFee = KjPatentFeeDao.getInstance().getRemindFeeByPatentId(kjPatent.getPatentId());
			    			String remindStr = KjPatentFeeDao.getInstance().getRemindStringByPatentFee(remindFee);
			    			
			    			thisContent = thisContent.replaceAll("param_feeNameAndAmount", remindStr.substring(remindStr.indexOf("����")));
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
			    							allUsername = allUsername + ",δ֪";
			    						}
			    					}
								}
			    			}
			    			thisContent = thisContent.replaceAll("param_allInventors", allUsername==null?"":allUsername);
			    			String acceptDate = DateUtil.convertDateToString(kjPatent.getAcceptDate());
			    			thisContent = thisContent.replaceAll("param_applyDate", acceptDate==null?"":acceptDate);
			    			
			    			if(doGiveup.equals("true"))
			    			{
								Long remainDays = 10000L; // ��ǰר����ʣ������,����Сʣ������Ϊ׼
								Date expireDay = new Date(); // ר���뵱ǰ��Сʣ��������Ӧ�Ľ�ֹ����(�����ҵ��ý�ֹ������Ҫ���ѵ����з��ü�¼)
								if (remindFee != null) {
									remainDays = remindFee.getRemainDays();
									expireDay = remindFee.getExpiryDay();
								}
								// ǿ�Ʒ�������:
								// param_mailTime �ѷ����ʼ�ʱ��
								// param_expireDays ��������
								// param_cancledate �ʼ�����ʱ���һ��
								// param_feeDate ��ѽ�ֹ����
								thisContent = thisContent.replaceAll("param_expireDays",(30l-remainDays)+"");
								thisContent = thisContent.replaceAll("param_feeDate",DateUtil.convertDateToString(expireDay));
								
								Calendar  calendar = Calendar.getInstance();   
							    calendar.setTime(new Date()); 
							    calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+7);//�����ڼ�7  
								thisContent = thisContent.replaceAll("param_cancledate",DateUtil.convertDateToString(calendar.getTime()));
								
								List<KjMail> mailList = KjPatentFeeDao.getInstance().getThisYearFeeMails(kjPatent);
								if(mailList!=null && mailList.size()>0)
								{
									String param_mailTime = "";
									for (int j = 0; j < mailList.size(); j++) 
									{
										KjMail thisMail = mailList.get(j);
										Date mailDate = thisMail.getCreateDate();
										if(j>0) param_mailTime = param_mailTime + "��";
										param_mailTime = param_mailTime + DateUtil.convertDateToString("yyyy��MM��dd��",mailDate);
									}
									param_mailTime = param_mailTime + "������"+mailList.size()+"��";
									thisContent = thisContent.replaceAll("param_mailTime",param_mailTime);
								}
								else{thisContent = thisContent.replaceAll("param_mailTime","");}
							}
			    			//���ĺϲ�
			    			if(mark == 0){ //���Ǻϲ��ĵ�һ��ר���������и�
			    				contentStrBuf.append(thisContent);
			    				mark = 1;
			    			}else{        //��Ҫ�����Ľ����и�
			    				contentStrBuf.delete(contentStrBuf.indexOf("���ɷѽ����ϵͳ�Զ���������"), contentStrBuf.length());
			    		//		System.out.println(thisContent.indexOf("����������ר����Ϣ��"));
			    				String newthisContent = thisContent.substring(thisContent.indexOf("����������ר����Ϣ��")+10);
			    		//		System.out.println(newthisContent);
			    				contentStrBuf.append(newthisContent);
			    			}
// contentStrBuf.append("���� "+kjPatent.getName()+" ר���������ڣ�Ӧ���ɽ��
// "+(kjPatent.getCostId()==null?"":kjPatent.getCostId().getMoney())+"
// Ԫ�������ϵͳ�Զ��������ɣ������ο�������ɷѽ����ʵ��Ϊ׼��");
//			    		}
//			    		{
//			    			contentStrBuf.append("����ר����������ɷ����ڣ��뾡���¼�Ƽ���Ϣϵͳ��ѯ");
//			    		}
			    	}
//				}
				else if("apply".equals(module)){
					ApplyDAO applyDAO = null;
					KjApply kjApply = applyDAO.getInstance().findByPk(Long.parseLong(selectedList.get(i)));
					toList.add(kjApply.getProjectName());
					//contentStrBuf.append(kjApply.getProjectName() + " ��");
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
					// 1 ȡMeetingbase
					// 2 ����meetingbase ȡmeetingUser ��һ���б�
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
		    	
		    	//duan 2014-02-27 ������Ҫ��ÿ�������˶����ѣ����Դ˴���ѭ��һ�� ----->  ֮ǰ�ĵ�һ�����˵�������û�������,ֻҪ�����з����˶����Ѿͺ���
		    	if("patent".equals(module) || "patentFee".equals(module)) // ��������ר����������Ϊ�ʼ�������
		    	{				
					List<KjProjectstaff> kjProjectstaff=null;
					List<KjProjectstaff> projectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 0L, "0");
					List<KjProjectstaff> newProjectStaffList = KjProjectStaffDao.getInstance().searchProjectStaff(kjPatent.getPatentId(), 2L, "0");
					if(newProjectStaffList.size()!=0){//�����������ݣ�Ҫ������ȫΪ������
						kjProjectstaff=newProjectStaffList;
					}
					else{//�����������ݣ��þ����ݼ���
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
		
		//zz 2012-03-30 ���ʼ��������������
		kjMailForm.setTitle(kjMailForm.getTitle() + firstObjName + " ������");
		
		//zz 2014-01-16
		if(otherModuleType.equals("fundFile" )){
			KjFundFile fundFile = FundFileDAO.getInstance().findByPk(Long.parseLong(otherModuleId));
			if(fundFile!=null)
			{
				kjMailForm.setTitle("����ѧ�Ƽ�ϵͳ:��������");
				contentStrBuf = new  StringBuffer("");
				contentStrBuf.append("������Ŀ��"+"--"+"�������ѽ�������ѧ����������Ϣ���£� ��Դ��"+fundFile.getKjDictOutlay().getOutlayName()+" ")
				.append(" ����"+fundFile.getFundSum()+"���� ")
				.append(" �������ڣ�"+ DateUtil.convertDateToString(fundFile.getReceiveDate())+" ")
				.append(" ���ݣ�"+ fundFile.getFileContent()+" ")
				.append(" �����˺ţ�"+ fundFile.getKjDictAccount().getAccount()+" ")
				.append(" ����ʱ�䣺"+ DateUtil.convertDateToString(fundFile.getEnterDate())+" ")
				.append(" ������ʱ��¼�Ƽ�ϵͳ���벦�")
				;
			}
		}
		
		//��ȡ���������б�
		List<KjGroupMember1> allSecList = MemberDAO.getInstance().getMemberList(MemberDAO.SEC_GROUP);
		List<KjGroupMember1> adminList = MemberDAO.getInstance().getMemberList(
				new Long[]{
						MemberDAO.ADMIN_GROUP,132l,133l,134l
						});
		
//		System.out.println("allSecList "+allSecList.size());
//		System.out.println("adminList "+adminList.size());
		
		List<KjGroupMember1> researchSecList = new ArrayList();
		for(int i=0;i<allSecList.size();i++){
			//maofm 2012/12/13  ��kjuser��Ϊemployee
//			Employee thisUser = allSecList.get(i).getEmployee();
			KjUser thisUser = allSecList.get(i).getKjUser();
			if(thisUser==null || !thisUser.getStMark().equals("1")){
//				System.out.println("1");
				continue;
			}
			Boolean haveSame = false;
			for(int j=0;j<adminList.size();j++){
				//maofm 2012/12/13  ��kjuser��Ϊemployee
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
		
//		contentStrBuf.append("�����ʼ��ɿƼ�ϵͳ http://kj.tju.edu.cn ����������ֱ�ӻظ���");
//		contentStrBuf.append("������ѧ��ѧ��չ�о���չԺ " + DateUtil.getDate(new Date())+"��");
		
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