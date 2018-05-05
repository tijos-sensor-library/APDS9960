package tijos.framework.sensor.apds9960;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.sensor.apds9960.ITiAPDS9960EventListener;
import tijos.framework.sensor.apds9960.TiAPDS9960;
import tijos.framework.util.Delay;
 
 
class APDS9960EventListener implements ITiAPDS9960EventListener {

	@Override
	public void onThresholdNotify(TiAPDS9960 apds9960) {
		System.out.println("GPIO Event");

	}

}

public class TiAPDS9960GestureSample {

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
			apds9960.enableGestureSensor(true);
			
			
			System.out.println("mode = " + apds9960.getMode() + "intenable  " + apds9960.getGestureIntEnable());
			
			int num = 2;
			while (num -- > 0) {
				try {
					if(apds9960.isGestureAvailable()){
						
						switch(apds9960.readGesture()){
						case DIR_UP:
							System.out.println("UP");
							break;
						case DIR_DOWN:
							System.out.println("DOWN");
							break;
						case DIR_LEFT:
							System.out.println("LEFT");
							break;
						case DIR_RIGHT:
							System.out.println("RIGHT");
							break;
						case DIR_FAR:
							System.out.println("FAR");
							break;
						case DIR_NEAR:
							System.out.println("NEAR");
							break;
						default:
							System.out.println("NONE");
							break;
						}

					}
					
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
