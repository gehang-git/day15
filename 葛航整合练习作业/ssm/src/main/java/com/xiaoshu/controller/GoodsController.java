package com.xiaoshu.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.xiaoshu.config.util.ConfigUtil;
import com.xiaoshu.dao.GoodstypeMapper;
import com.xiaoshu.entity.Attachment;
import com.xiaoshu.entity.Goods;
import com.xiaoshu.entity.Goodstype;
import com.xiaoshu.entity.Log;
import com.xiaoshu.entity.Operation;
import com.xiaoshu.entity.Role;
import com.xiaoshu.entity.User;
import com.xiaoshu.service.GoodsService;
import com.xiaoshu.service.OperationService;
import com.xiaoshu.service.RoleService;
import com.xiaoshu.service.UserService;
import com.xiaoshu.util.StringUtil;
import com.xiaoshu.util.TimeUtil;
import com.xiaoshu.util.WriterUtil;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;

@Controller
@RequestMapping("goods")
public class GoodsController extends LogController{
	static Logger logger = Logger.getLogger(GoodsController.class);

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService ;
	
	@Autowired
	private OperationService operationService;
	@Autowired
	private GoodsService service;
	
	@RequestMapping("goodsIndex")
	public String index(HttpServletRequest request,Integer menuid) throws Exception{
		List<Role> roleList = roleService.findRole(new Role());
		List<Operation> operationList = operationService.findOperationIdsByMenuid(menuid);
		List<Goodstype> tlist = service.findAllType();
		request.setAttribute("operationList", operationList);
		request.setAttribute("roleList", roleList);
		request.setAttribute("tlist", tlist);
		return "goods";
	}
	
	
	@RequestMapping(value="goodsList",method=RequestMethod.POST)
	public void userList(Goods goods,HttpServletRequest request,HttpServletResponse response,String offset,String limit) throws Exception{
		try {
			Integer pageSize = StringUtil.isEmpty(limit)?ConfigUtil.getPageSize():Integer.parseInt(limit);
			Integer pageNum =  (Integer.parseInt(offset)/pageSize)+1;
			PageInfo<Goods> userList=service.findUserPage(goods,pageNum,pageSize);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("total",userList.getTotal() );
			jsonObj.put("rows", userList.getList());
	        WriterUtil.write(response,jsonObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户展示错误",e);
			throw e;
		}
	}
	
	
	// 新增或修改
	@RequestMapping("reserveGoods")
	public void reserveUser(HttpServletRequest request,User user,HttpServletResponse response){
		Integer userId = user.getUserid();
		JSONObject result=new JSONObject();
		try {
			if (userId != null) {   // userId不为空 说明是修改
				User userName = userService.existUserWithUserName(user.getUsername());
				if(userName != null && userName.getUserid().compareTo(userId)==0){
					user.setUserid(userId);
					userService.updateUser(user);
					result.put("success", true);
				}else{
					result.put("success", true);
					result.put("errorMsg", "该用户名被使用");
				}
				
			}else {   // 添加
				if(userService.existUserWithUserName(user.getUsername())==null){  // 没有重复可以添加
					userService.addUser(user);
					result.put("success", true);
				} else {
					result.put("success", true);
					result.put("errorMsg", "该用户名被使用");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存用户信息错误",e);
			result.put("success", true);
			result.put("errorMsg", "对不起，操作失败");
		}
		WriterUtil.write(response, result.toString());
	}
	
	
	@RequestMapping("deleteGoods")
	public void deleteGoods(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			String[] ids=request.getParameter("ids").split(",");
			for (String id : ids) {
				service.deleteUser(Integer.parseInt(id));
			}
			result.put("success", true);
			result.put("delNums", ids.length);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除用户信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}
	@RequestMapping("echartsGoods")
	public void echartsGoods(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			List<Goods> golist = service.findEcharts();
			result.put("success", true);
			result.put("golist", golist);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除用户信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}
	/**
	 * 备份
	 */
	@RequestMapping("exportGoods")
	public void exportGoods(HttpServletRequest request,HttpServletResponse response){
		JSONObject result = new JSONObject();
		try {
			String time = TimeUtil.formatTime(new Date(), "yyyyMMddHHmmss");
		    String excelName = "手动备份"+time;
			//Log log = new Log();
			Goods goods = new Goods();
			List<Goods> list = service.findUserPage(goods);
			//List<Log> list = logService.findLog(log);
			String[] handers = {"序号","商品名称","商品颜色","商品特点","商品图片","上架时间","商品分类"};
			// 1导入硬盘
			ExportExcelToDisk(request,handers,list, excelName,response);
			/*// 2导出的位置放入attachment表
			Attachment attachment = new Attachment();
			attachment.setAttachmentname(excelName+".xls");
			attachment.setAttachmentpath("logs/backup");
			attachment.setAttachmenttime(new Date());
			attachmentService.insertAttachment(attachment);
			// 3删除log表
			logService.truncateLog();*/
			result.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("", "对不起，备份失败");
		}
		//WriterUtil.write(response, result.toString());
	}
	
	
	
	// 导出到硬盘
	@SuppressWarnings("resource")
	private void ExportExcelToDisk(HttpServletRequest request,
			String[] handers, List<Goods> list, String excleName,HttpServletResponse response) throws Exception {
		
		try {
			HSSFWorkbook wb = new HSSFWorkbook();//创建工作簿
			HSSFSheet sheet = wb.createSheet("操作记录备份");//第一个sheet
			HSSFRow rowFirst = sheet.createRow(0);//第一个sheet第一行为标题
			rowFirst.setHeight((short) 500);
			for (int i = 0; i < handers.length; i++) {
				sheet.setColumnWidth((short) i, (short) 4000);// 设置列宽
			}
			//写标题了
			for (int i = 0; i < handers.length; i++) {
			    //获取第一行的每一个单元格
			    HSSFCell cell = rowFirst.createCell(i);
			    //往单元格里面写入值
			    cell.setCellValue(handers[i]);
			}
			for (int i = 0;i < list.size(); i++) {
			    //获取list里面存在是数据集对象
			    //Log log = list.get(i);
			    Goods goods = list.get(i);
			    //创建数据行
			    HSSFRow row = sheet.createRow(i+1);
			    //设置对应单元格的值
			    row.setHeight((short)400);   // 设置每行的高度
			    //"序号","商品名称","商品颜色","商品特点","商品图片","上架时间","商品分类"
			    row.createCell(0).setCellValue(i+1);
			    row.createCell(1).setCellValue(goods.getName());
			    row.createCell(2).setCellValue(goods.getColor());
			    row.createCell(3).setCellValue(goods.getTrait());
			    row.createCell(4).setCellValue(goods.getImg());
			    row.createCell(5).setCellValue(TimeUtil.formatTime(goods.getCreatetime(), "yyyy-MM-dd"));
			    row.createCell(6).setCellValue(goods.getTname());
			}
			//写出文件（path为文件路径含文件名）
			/*	OutputStream os;
				File file = new File("F:/商品信息.xls");
				
				if (!file.exists()){//若此目录不存在，则创建之  
					file.createNewFile();  
					logger.debug("创建文件夹路径为："+ file.getPath());  
	            } 
				os = new FileOutputStream(file);
				wb.write(os);
				os.close();*/
			response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode("商品列表.xls", "UTF-8"));
			response.setHeader("Connection", "close");
			response.setHeader("Content-Type", "application/octet-stream");
	        wb.write(response.getOutputStream());
			wb.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
	}
	
	//导入
	@RequestMapping("importGoods")
	public void importGoods(MultipartFile importFile,HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			/*//获取文件信息
			HSSFWorkbook wb = new HSSFWorkbook(importFile.getInputStream());
			//解析文件,获取sheet页
			HSSFSheet sheetAt = wb.getSheetAt(0);
			//获取最后一行的行数
			int lastRowNum = sheetAt.getLastRowNum();
			//循环行数 获取每一个行对象
			for (int i = 1; i <=lastRowNum; i++) {
				//获取每一行对象
				HSSFRow row = sheetAt.getRow(i);
				//获取单元格内的数据
				//姓名 颜色 特点 图片 时间 分类
				String name = row.getCell(0).toString();
				String color = row.getCell(1).toString();
				String trait = row.getCell(2).toString();
				String img = row.getCell(3).toString();
				Date createtime = row.getCell(4).getDateCellValue();
				String tname = row.getCell(5).toString();
				//根据分类名称查询分类id
				Integer id = findTnameByTid(tname);
				//封装goods对象
				Goods goods = new Goods();
				goods.setColor(color);
				goods.setCreatetime(createtime);
				goods.setImg(img);
				goods.setName(name);
				goods.setTrait(trait);
				goods.setTid(id);
				service.addUser(goods);
			}*/
			//获取文件
			HSSFWorkbook wb = new HSSFWorkbook(importFile.getInputStream());
			//获取sheet页
			HSSFSheet sheetAt = wb.getSheetAt(0);
			//获取最后一页
			int lastRowNum = sheetAt.getLastRowNum();
			//循环的行,获取行对象
			for (int i = 1; i <= lastRowNum; i++) {
				HSSFRow row = sheetAt.getRow(i);
				//姓名 颜色 特点 图片 时间 分类
				String name = row.getCell(0).toString();
				String color = row.getCell(1).toString();
				String trait = row.getCell(2).toString();
				String img = row.getCell(3).toString();
				Date createtime = row.getCell(4).getDateCellValue();
				String tname = row.getCell(5).toString();
				//根据分类名称查找id
				Integer id = findTnameByTid(tname);
				//封装对象
				Goods goods = new Goods();
				goods.setColor(color);
				goods.setCreatetime(createtime);
				goods.setTrait(trait);
				goods.setImg(img);
				goods.setTid(id);
				service.addUser(goods);
			}
			
			result.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除用户信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}
	//根据名称查询id
	@Autowired
	private GoodstypeMapper tMapper;
	public Integer findTnameByTid(String tname) {
		Goodstype goodstype = new Goodstype();
		goodstype.setName(tname);
		Goodstype one = tMapper.selectOne(goodstype);
		if (one==null) {
			tMapper.insertType(goodstype);
			one = goodstype;
		} 
		return one.getId();
	}


	@RequestMapping("editPassword")
	public void editPassword(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		String oldpassword = request.getParameter("oldpassword");
		String newpassword = request.getParameter("newpassword");
		HttpSession session = request.getSession();
		User currentUser = (User) session.getAttribute("currentUser");
		if(currentUser.getPassword().equals(oldpassword)){
			User user = new User();
			user.setUserid(currentUser.getUserid());
			user.setPassword(newpassword);
			try {
				userService.updateUser(user);
				currentUser.setPassword(newpassword);
				session.removeAttribute("currentUser"); 
				session.setAttribute("currentUser", currentUser);
				result.put("success", true);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("修改密码错误",e);
				result.put("errorMsg", "对不起，修改密码失败");
			}
		}else{
			logger.error(currentUser.getUsername()+"修改密码时原密码输入错误！");
			result.put("errorMsg", "对不起，原密码输入错误！");
		}
		WriterUtil.write(response, result.toString());
	}
}
