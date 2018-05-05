package tijos.framework.sensor.apds9960;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.sensor.apds9960.TiAPDS9960;
import tijos.framework.util.Delay;

public class TiAPDS9960ProximitySensorSample 
{
	public static void main(String[] args) {
	try {
		/*
		 * 定义使用的TiGPIO port
		 */
		int gpioPort0 = 0;
		
		/*
		 * 定义所使用的gpioPin
		 */
		int gpioPin0 = 3;
		
		/*
		 * 定义使用的TiI2CMaster port
		 */
		int i2cPort0 = 0;

		/*
		 * 资源分配， 将i2cPort0分配给TiI2CMaster对象i2c0
		 */
		TiI2CMaster i2c0 = TiI2CMaster.open(i2cPort0);
		TiGPIO gpio0 = TiGPIO.open(gpioPort0, gpioPin0);
		

		TiAPDS9960 apds9960 = new TiAPDS9960(i2c0, gpio0, gpioPin0);

		APDS9960EventListener lc = new APDS9960EventListener();
		apds9960.setEventListener(lc);
		
		apds9960.initialize();
		
		// Adjust the Proximity sensor gain
		apds9960.setProximityGain(TiAPDS9960.PGAIN_2X);

		// Start running the APDS-9960 proximity sensor (no interrupts)
		apds9960.enableProximitySensor(false);
		
		int num = 1000;
		while (num -- > 0) {
			try {
				int proximity = apds9960.readProximity();
				
				System.out.println("Proximity: " +proximity);
				
				// Wait 250 ms before next reading
				Delay.msDelay(250);
			} catch (Exception ex) {

				ex.printStackTrace();
			}

		}
	} catch (IOException ie) {
		ie.printStackTrace();
	}
	}
 }