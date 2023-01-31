package eu.thijslemmens.iot.heatpumpsteering;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class HeatpumpSteeringApplicationTests {

	@Container
	public GenericContainer mqttServer = new GenericContainer(DockerImageName.parse("eclipse-mosquitto:latest"))
			.withExposedPorts(1883, 1883);

	@Test
	void contextLoads() {
	}

}
