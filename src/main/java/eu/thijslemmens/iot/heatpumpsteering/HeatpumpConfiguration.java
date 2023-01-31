package eu.thijslemmens.iot.heatpumpsteering;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "heatpump")
public class HeatpumpConfiguration {

    public String getMqttUrl() {
        return mqttUrl;
    }

    public void setMqttUrl(String mqttUrl) {
        this.mqttUrl = mqttUrl;
    }

    public String getMqttInboundTopic() {
        return mqttInboundTopic;
    }

    public void setMqttInboundTopic(String mqttInboundTopic) {
        this.mqttInboundTopic = mqttInboundTopic;
    }

    public String getMqttOutboundTopic() {
        return mqttOutboundTopic;
    }

    public void setMqttOutboundTopic(String mqttOutboundTopic) {
        this.mqttOutboundTopic = mqttOutboundTopic;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public void setMqttClientId(String mqttClientId) {
        this.mqttClientId = mqttClientId;
    }

    private String mqttUrl;
    private String mqttInboundTopic;
    private String mqttOutboundTopic;
    private String mqttClientId;

    public int getSwitchOffPowerThreshold() {
        return switchOffPowerThreshold;
    }

    public void setSwitchOffPowerThreshold(int switchOffPowerThreshold) {
        this.switchOffPowerThreshold = switchOffPowerThreshold;
    }

    public int getSwitchOnPowerThreshold() {
        return switchOnPowerThreshold;
    }

    public void setSwitchOnPowerThreshold(int switchOnPowerThreshold) {
        this.switchOnPowerThreshold = switchOnPowerThreshold;
    }

    private int switchOffPowerThreshold;
    private int switchOnPowerThreshold;


}
