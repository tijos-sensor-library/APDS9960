package tijos.sample.sensor.apds9960;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.sensor.apds9960.TiAPDS9960;
import tijos.util.Delay;

public class TiAPDS9960ColorSensorSample {
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
			
			// Start running the APDS-9960 light sensor (no interrupts)
			apds9960.enableLightSensor(false);

			// Wait for initialization and calibration to finish
			Delay.msDelay(500);

			int num = 10;
			while (num -- > 0) {
				try {
					 // Read the light levels (ambient, red, green, blue)
					int ambient_light = apds9960.readAmbientLight();
					int red_light = apds9960.readRedLight();
					int green_light = apds9960.readGreenLight();
					int blue_light = apds9960.readBlueLight();
					
					System.out.println("ambient_light: " +ambient_light);
					System.out.println("red_light: " +red_light);
					System.out.println("green_light: " +green_light);
					System.out.println("blue_light: " +blue_light);
					
					// Wait 1000 ms before next reading
					Delay.msDelay(1000);
				} catch (Exception ex) {

					ex.printStackTrace();
				}

			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}

	}
}
