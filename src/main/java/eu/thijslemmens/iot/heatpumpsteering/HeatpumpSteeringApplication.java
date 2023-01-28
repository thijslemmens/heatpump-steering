package eu.thijslemmens.iot.heatpumpsteering;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.mqtt.core.Mqttv3ClientManager;
import org.springframework.integration.mqtt.core.Mqttv5ClientManager;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;

@SpringBootApplication
@EnableConfigurationProperties(HeatpumpConfiguration.class)
public class HeatpumpSteeringApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeatpumpSteeringApplication.class, args);
	}

	@Bean
	public JdbcPollingChannelAdapter jdbcPollingChannelAdapter(DataSource dataSource) {
		var query = "SELECT\n"
				+ "SUM(CASE when direction='out' then value else -value end)\n"
				+ "FROM (\n"
				+ "SELECT\n"
				+ "  labels -> 'tariff' as tariff, labels -> 'direction' as direction,\n"
				+ "  (max(value) - min(value)) * 3600000 /  extract(epoch from (max(time) - min(time))) as value\n"
				+ "FROM\n"
				+ "\tmetrics\n"
				+ "where time > now() - INTERVAL '30 seconds' and\n"
				+ "name = 'electricity.meter'\n"
				+ "group by labels) AS FOO;";
		return new JdbcPollingChannelAdapter(dataSource, query);
	}

	@Bean
	public IntegrationFlow heatPumpSteering(JdbcPollingChannelAdapter jdbcPollingChannelAdapter, Mqttv5ClientManager clientManager, HeatPump heatPump, HeatpumpConfiguration heatpumpConfiguration) {
		var messageHandler = new Mqttv5PahoMessageHandler(clientManager);
		messageHandler.setDefaultTopic(heatpumpConfiguration.getMqttOutboundTopic());
		return IntegrationFlow.from(jdbcPollingChannelAdapter, c -> c.poller(Pollers.fixedDelay(5000)))
				.transform(o -> {
					double sum = (double) ((Map) ((List) o).get(0)).get("sum");
					if (heatPump.isOn() && sum < -200) {
						return new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8));
					} else if (!heatPump.isOn() && sum > 500) {
						return new MqttMessage("ON".getBytes(StandardCharsets.UTF_8));
					} else if(heatPump.isOn()) {
						return new MqttMessage("ON".getBytes(StandardCharsets.UTF_8));
					}
					return new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8));
				})
				.handle(messageHandler)
				.get();
	}

	@Bean
	public Mqttv5ClientManager clientManager(HeatpumpConfiguration configuration) {
		var options = new MqttConnectionOptions();
		options.setServerURIs(new String[]{configuration.getMqttUrl()});
		options.setConnectionTimeout(3000);
		options.setMaxReconnectDelay(1000);
		options.setAutomaticReconnect(true);
		var clientManager = new Mqttv5ClientManager(options, configuration.getMqttClientId());
		return clientManager;
	}
	@Bean
	public Mqttv5PahoMessageDrivenChannelAdapter heatPumpState(Mqttv5ClientManager clientManager, HeatpumpConfiguration configuration) {
		return new Mqttv5PahoMessageDrivenChannelAdapter(clientManager, configuration.getMqttInboundTopic());
	}

	@Bean
	public HeatPump heatPump() {
		return new HeatPump();
	}

	@Bean
	public IntegrationFlow updateHeatPumpState(Mqttv5PahoMessageDrivenChannelAdapter mqttPahoMessageDrivenChannelAdapter, HeatPump heatPump) {
		return IntegrationFlow.from(mqttPahoMessageDrivenChannelAdapter)
				.handle(message -> {
					String payload = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
					if ("ON".equals(payload)) {
						heatPump.setOn(true);
					} else {
						heatPump.setOn(false);
					}
				}).get();
	}


}
