package com.txmq.aviator.config.model;

import java.util.List;

import com.txmq.aviator.config.AviatorConfiguration;

@AviatorConfiguration(property="hcs")
public class HCSConfig {
	public boolean useMainnet;
	public boolean createTopic;
	public String mirrorNodeAddress;
	public String operatorID;
	public String operatorKey;
	public String hcsTopicID;
}
