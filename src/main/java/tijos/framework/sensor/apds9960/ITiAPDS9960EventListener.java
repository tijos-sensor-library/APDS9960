package tijos.framework.sensor.apds9960;

/*
 * Event listener for TiGeneralSensor
 * 
 */
public interface ITiAPDS9960EventListener {
	/**
	 * notification when the value is beyond of the threshold value 
	 * 
	 * @param sensor the sensor object
	 */
	void onThresholdNotify(TiAPDS9960 sensor);	
}
