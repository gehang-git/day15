package com.xiaoshu.service;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoshu.dao.GoodsMapper;
import com.xiaoshu.dao.GoodstypeMapper;
import com.xiaoshu.dao.UserMapper;
import com.xiaoshu.entity.Goods;
import com.xiaoshu.entity.Goodstype;
import com.xiaoshu.entity.User;

@Service
public class GoodsService {

	@Autowired
	UserMapper userMapper;
	@Autowired
	private GoodsMapper mapper;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queueTextDestination;
	// 查询所有
	public List<User> findUser(User t) throws Exception {
		return userMapper.select(t);
	};

	// 数量
	public int countUser(User t) throws Exception {
		return userMapper.selectCount(t);
	};

	// 通过ID查询
	public User findOneUser(Integer id) throws Exception {
		return userMapper.selectByPrimaryKey(id);
	};

	// 新增
	public void addUser(final Goods t) throws Exception {
		mapper.insert(t);
		jmsTemplate.send(queueTextDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				String jsonString = JSON.toJSONString(t);
				TextMessage textMessage = session.createTextMessage(jsonString);
				return textMessage;
			}
		});
	};

	// 修改
	public void updateUser(User t) throws Exception {
		userMapper.updateByPrimaryKeySelective(t);
	};

	// 删除
	public void deleteUser(Integer id) throws Exception {
		mapper.deleteByPrimaryKey(id);
	};


	// 通过用户名判断是否存在，（新增时不能重名）
/*	public User existUserWithUserName(String userName) throws Exception {
		UserExample example = new UserExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(userName);
		List<User> userList = userMapper.selectByExample(example);
		return userList.isEmpty()?null:userList.get(0);
	};
*/

	public PageInfo<Goods> findUserPage(Goods goods,int pageNum,int pageSize) {
		PageHelper.startPage(pageNum,pageSize);
		List<Goods> glist = mapper.findAll(goods);
		PageInfo<Goods> pageInfo = new PageInfo<Goods>(glist);
		return pageInfo;
	}
	@Autowired
	private GoodstypeMapper typeMapper;

	public List<Goodstype> findAllType() {
		return typeMapper.selectAll();
	}

	public List<Goods> findUserPage(Goods goods) {
		return mapper.findAll(goods);
	}

	public List<Goods> findEcharts() {
		return mapper.findEcharts();
	}


}
