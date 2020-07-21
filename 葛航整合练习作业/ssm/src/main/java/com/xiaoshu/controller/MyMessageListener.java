package com.xiaoshu.controller;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.alibaba.fastjson.JSON;
import com.xiaoshu.entity.Goods;

public class MyMessageListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
			TextMessage textMessage = (TextMessage) message;
			String text;
			try {
				text = textMessage.getText();
				Goods goods = JSON.parseObject(text, Goods.class);
				System.out.println("接收到的MQ为:"+goods);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		
	
	}

}
