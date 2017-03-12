package rabbitmaster;

import org.junit.Assert;
import org.junit.Test;

import com.wizes.rabbitmaster.RabbitMaster;

public class RabbitMasterTest {

	@Test
	public void RabbitMasterLoginTest() {
		String rabbitUrl = "http://localhost:1975";
		String masterId = "hyunwoo";
		String masterPassword = "qwer124";
		RabbitMaster rabbitMaster = new RabbitMaster(rabbitUrl, masterId, masterPassword);
		Assert.assertEquals(rabbitMaster.getRabbitURL(), rabbitUrl + "/");
		Assert.assertEquals(true, rabbitMaster.login());
		
	}

}
