package com.andmail.kernel;

import android.os.Message;

import com.andframe.application.AfApplication;
import com.andframe.thread.AfTask;
import com.andmail.model.MailModel;

public class MailSender extends AfTask{
	
	protected static MailModel defaultModel;

	protected MailBySmtp mMail;

	protected String mSubject;
	protected String mContent;
	protected String mSendTo;

	protected MailModel mModel;

	public MailSender(String subject, String content) {
		// TODO Auto-generated constructor stub
		this(defaultModel,defaultModel.username,subject,content);
	}

	public MailSender(MailModel model,String subject, String content) {
		// TODO Auto-generated constructor stub
		this(model,model.username,subject,content);
	}

	public MailSender(MailModel model,String sendto,String subject, String content) {
		// TODO Auto-generated constructor stub
		mModel = model;
		mSubject = subject;
		mContent = content;

		mSendTo = sendto;
		mMail = new MailBySmtp(model.host, model.username, model.password);
	}

	public void send() throws Exception {
		mMail.create(mModel.username,mSendTo , mSubject);
		mMail.addContent(mContent);
		mMail.send();
	}

	public void sendTask() {
		// TODO Auto-generated method stub
		AfApplication.postTask(this);
	}

	@Override
	protected void onWorking(Message msg) throws Exception {
		// TODO Auto-generated method stub
		this.send();
	}

	public static void setDefaultMailModel(MailModel model) {
		defaultModel = model;
	}
}