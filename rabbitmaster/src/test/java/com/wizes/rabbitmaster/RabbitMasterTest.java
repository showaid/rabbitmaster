package com.wizes.rabbitmaster;

import org.junit.Assert;
import org.junit.Test;

import com.wizes.rabbitmaster.RabbitMaster;

public class RabbitMasterTest {

	@Test
	public void RabbitMasterLoginTest() {
		String rabbitUrl = "http://localhost:8080";
		String masterId = "";
		String masterPassword = "";
		RabbitMaster rabbitMaster = new RabbitMaster(rabbitUrl, masterId, masterPassword);
		Assert.assertEquals(rabbitMaster.getRabbitURL(), rabbitUrl + "/");
		Assert.assertEquals(true, rabbitMaster.readPageAfterLogin());
		Assert.assertEquals(true, rabbitMaster.readPageAfterLogin());
		Assert.assertEquals(true, rabbitMaster.readPageAfterLogin());
		Assert.assertEquals(true, rabbitMaster.readPageAfterLogin());
		
	}

}
