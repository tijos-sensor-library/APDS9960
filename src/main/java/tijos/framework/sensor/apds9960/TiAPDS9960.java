
package tijos.framework.sensor.apds9960;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.eventcenter.ITiEvent;
import tijos.framework.eventcenter.ITiEventListener;
import tijos.framework.eventcenter.TiEventService;
import tijos.framework.eventcenter.TiEventType;
import tijos.framework.eventcenter.TiGPIOEvent;
import tijos.util.BigBitConverter;
import tijos.util.Delay;

/*
 * https://github.com/sparkfun/SparkFun_APDS-9960_Sensor_Arduino_Library
 */

class TiAPDS9960Register {

	/* APDS-9960 register addresses */
	public static final int APDS9960_ENABLE = 0x80;
	public static final int APDS9960_ATIME = 0x81;
	public static final int APDS9960_WTIME = 0x83;
	public static final int APDS9960_AILTL = 0x84;
	public static final int APDS9960_AILTH = 0x85;
	public static final int APDS9960_AIHTL = 0x86;
	public static final int APDS9960_AIHTH = 0x87;
	public static final int APDS9960_PILT = 0x89;
	public static final int APDS9960_PIHT = 0x8B;
	public static final int APDS9960_PERS = 0x8C;
	public static final int APDS9960_CONFIG1 = 0x8D;
	public static final int APDS9960_PPULSE = 0x8E;
	public static final int APDS9960_CONTROL = 0x8F;
	public static final int APDS9960_CONFIG2 = 0x90;
	public static final int APDS9960_ID = 0x92;
	public static final int APDS9960_STATUS = 0x93;
	public static final int APDS9960_CDATAL = 0x94;
	public static final int APDS9960_CDATAH = 0x95;
	public static final int APDS9960_RDATAL = 0x96;
	public static final int APDS9960_RDATAH = 0x97;
	public static final int APDS9960_GDATAL = 0x98;
	public static final int APDS9960_GDATAH = 0x99;
	public static final int APDS9960_BDATAL = 0x9A;
	public static final int APDS9960_BDATAH = 0x9B;
	public static final int APDS9960_PDATA = 0x9C;
	public static final int APDS9960_POFFSET_UR = 0x9D;
	public static final int APDS9960_POFFSET_DL = 0x9E;
	public static final int APDS9960_CONFIG3 = 0x9F;
	public static final int APDS9960_GPENTH = 0xA0;
	public static final int APDS9960_GEXTH = 0xA1;
	public static final int APDS9960_GCONF1 = 0xA2;
	public static final int APDS9960_GCONF2 = 0xA3;
	public static final int APDS9960_GOFFSET_U = 0xA4;
	public static final int APDS9960_GOFFSET_D = 0xA5;
	public static final int APDS9960_GOFFSET_L = 0xA7;
	public static final int APDS9960_GOFFSET_R = 0xA9;
	public static final int APDS9960_GPULSE = 0xA6;
	public static final int APDS9960_GCONF3 = 0xAA;
	public static final int APDS9960_GCONF4 = 0xAB;
	public static final int APDS9960_GFLVL = 0xAE;
	public static final int APDS9960_GSTATUS = 0xAF;
	public static final int APDS9960_IFORCE = 0xE4;
	public static final int APDS9960_PICLEAR = 0xE5;
	public static final int APDS9960_CICLEAR = 0xE6;
	public static final int APDS9960_AICLEAR = 0xE7;
	public static final int APDS9960_GFIFO_U = 0xFC;
	public static final int APDS9960_GFIFO_D = 0xFD;
	public static final int APDS9960_GFIFO_L = 0xFE;
	public static final int APDS9960_GFIFO_R = 0xFF;

}

/* Container for gesture data */
class gesture_data_type {
	public int[] u_data = new int[32];
	public int[] d_data = new int[32];
	public int[] l_data = new int[32];
	public int[] r_data = new int[32];

	public int index;
	public int total_gestures;
	public int in_threshold;
	public int out_threshold;
};

public class TiAPDS9960  implements ITiEventListener {


	/* APDS-9960 I2C address */
	public static final int APDS9960_I2C_ADDR      = 0x39 << 1;

	/* Acceptable device IDs */
	public static final int APDS9960_ID_1 = 0xAB;
	public static final int APDS9960_ID_2 = 0x9C;

	/* Bit fields */
	public static final int APDS9960_PON = 0x01; // 0b00000001
	public static final int APDS9960_AEN = 0x02; // 0b00000010
	public static final int APDS9960_PEN = 0x04; // 0b00000100
	public static final int APDS9960_WEN = 0x08; // 0b00001000
	public static final int APSD9960_AIEN = 0x10; // 0b00010000
	public static final int APDS9960_PIEN = 0x20; // 0b00100000
	public static final int APDS9960_GEN = 0x40; // 0b01000000
	public static final int APDS9960_GVALID = 0x01; // 0b00000001

	/* Acceptable parameters for setMode */
	public static final int POWER = 0;
	public static final int AMBIENT_LIGHT = 1;
	public static final int PROXIMITY = 2;
	public static final int WAIT = 3;
	public static final int AMBIENT_LIGHT_INT = 4;
	public static final int PROXIMITY_INT = 5;
	public static final int GESTURE = 6;
	public static final int ALL_MODES = 7;

	/* Gesture parameters */
	private static final int GESTURE_THRESHOLD_OUT = 10;
	private static final int GESTURE_SENSITIVITY_1 = 50;
	private static final int GESTURE_SENSITIVITY_2 = 20;

	/* LED Drive values */
	public static final int LED_DRIVE_100MA = 0;
	public static final int LED_DRIVE_50MA = 1;
	public static final int LED_DRIVE_25MA = 2;
	public static final int LED_DRIVE_12_5MA = 3;

	/* Proximity Gain (PGAIN) values */
	public static final int PGAIN_1X = 0;
	public static final int PGAIN_2X = 1;
	public static final int PGAIN_4X = 2;
	public static final int PGAIN_8X = 3;

	/* ALS Gain (AGAIN) values */
	public static final int AGAIN_1X = 0;
	public static final int AGAIN_4X = 1;
	public static final int AGAIN_16X = 2;
	public static final int AGAIN_64X = 3;

	/* Gesture Gain (GGAIN) values */
	public static final int GGAIN_1X = 0;
	public static final int GGAIN_2X = 1;
	public static final int GGAIN_4X = 2;
	public static final int GGAIN_8X = 3;

	/* LED Boost values */
	public static final int LED_BOOST_100 = 0;
	public static final int LED_BOOST_150 = 1;
	public static final int LED_BOOST_200 = 2;
	public static final int LED_BOOST_300 = 3;

	/* Gesture wait time values */
	public static final int GWTIME_0MS = 0;
	public static final int GWTIME_2_8MS = 1;
	public static final int GWTIME_5_6MS = 2;
	public static final int GWTIME_8_4MS = 3;
	public static final int GWTIME_14_0MS = 4;
	public static final int GWTIME_22_4MS = 5;
	public static final int GWTIME_30_8MS = 6;
	public static final int GWTIME_39_2MS = 7;

	/* Default values */
	public static final int DEFAULT_ATIME = 219; // 103ms
	public static final int DEFAULT_WTIME = 246; // 27ms
	public static final int DEFAULT_PROX_PPULSE = 0x87; // 16us, 8 pulses
	public static final int DEFAULT_GESTURE_PPULSE = 0x89; // 16us, 10 pulses
	public static final int DEFAULT_POFFSET_UR = 0; // 0 offset
	public static final int DEFAULT_POFFSET_DL = 0; // 0 offset
	public static final int DEFAULT_CONFIG1 = 0x60; // No 12x wait (WTIME) factor
	public static final int DEFAULT_LDRIVE = LED_DRIVE_100MA;
	public static final int DEFAULT_PGAIN = PGAIN_4X;
	public static final int DEFAULT_AGAIN = AGAIN_4X;
	public static final int DEFAULT_PILT = 0; // Low proximity threshold
	public static final int DEFAULT_PIHT = 50; // High proximity threshold
	public static final int DEFAULT_AILT = 0xFFFF; // Force interrupt for calibration
	public static final int DEFAULT_AIHT = 0;
	public static final int DEFAULT_PERS = 0x11; // 2 consecutive prox or ALS for int.
	public static final int DEFAULT_CONFIG2 = 0x01; // No saturation interrupts or LED boost
	public static final int DEFAULT_CONFIG3 = 0; // Enable all photodiodes, no SAI
	public static final int DEFAULT_GPENTH = 40; // Threshold for entering gesture mode
	public static final int DEFAULT_GEXTH = 30; // Threshold for exiting gesture mode
	public static final int DEFAULT_GCONF1 = 0x40; // 4 gesture events for int., 1 for exit
	public static final int DEFAULT_GGAIN = GGAIN_4X;
	public static final int DEFAULT_GLDRIVE = LED_DRIVE_100MA;
	public static final int DEFAULT_GWTIME = GWTIME_2_8MS;
	public static final int DEFAULT_GOFFSET = 0; // No offset scaling for gesture mode
	public static final int DEFAULT_GPULSE = 0xC9; // 32us, 10 pulses
	public static final int DEFAULT_GCONF3 = 0; // All photodiodes active during gesture
	public static final int DEFAULT_GIEN = 0; // Disable gesture interrupts

	private TiI2CMaster i2cmObj;

	private int i2cSlaveAddr = APDS9960_I2C_ADDR;

	private byte[] data = new byte[128];

	/* Direction definitions */
	public enum Direction {
		DIR_NONE, DIR_LEFT, DIR_RIGHT, DIR_UP, DIR_DOWN, DIR_NEAR, DIR_FAR, DIR_ALL
	};

	/* State definitions */
	public enum State {
		NA_STATE, NEAR_STATE, FAR_STATE, ALL_STATE
	};

	private gesture_data_type gesture_data = new gesture_data_type();

	private int gesture_ud_delta = 0;
	private int gesture_lr_delta = 0;
	private int gesture_ud_count = 0;
	private int gesture_lr_count = 0;
	private int gesture_near_count = 0;
	private int gesture_far_count = 0;

	private State gesture_state = State.NA_STATE;
	private Direction gesture_motion = Direction.DIR_NONE;

	/**
	 * TiGeneralSensor signal pin id
	 */
	private int signalPin;
	/**
	 * TiGeneralSensor event time
	 */
	private long eventTime;
	/**
	 * TiGPIO object
	 */
	private TiGPIO gpioObj = null;

	/**
	 * TiGeneralSensor event listener
	 */
	private ITiAPDS9960EventListener apds9960EventLc = null;

	/**
	 * Initialize with I2C and default address without gpio.
	 * @param i2c  TiI2CMaster object
	 */
	public TiAPDS9960(TiI2CMaster i2c) {
		this(i2c, APDS9960_I2C_ADDR, null, 0);
	}

	/**
	 *  Initialize with I2C and default address with gpio.
	 * @param i2c TiI2CMaster object
	 * @param gpio  TiGPIO object
	 * @param signalPinID gpio pin id
	 */
	public TiAPDS9960(TiI2CMaster i2c, TiGPIO gpio, int signalPinID) {
		this(i2c, APDS9960_I2C_ADDR, gpio, signalPinID);
	}

	/**
	 * 
	 * @param i2c
	 * @param addr
	 * @param gpio
	 * @param signalPinID
	 */
	public TiAPDS9960(TiI2CMaster i2c, int addr, TiGPIO gpio, int signalPinID) {
		this.i2cmObj = i2c;
		this.i2cSlaveAddr = addr;
		this.gpioObj = gpio;
		this.signalPin = signalPinID;
	}

	/**
	 * Set the TiGeneralSensor event listener
	 * 
	 * @param lc
	 *            listener object or null[IN]
	 * @throws IOException
	 */
	public void setEventListener(ITiAPDS9960EventListener lc) throws IOException {
		synchronized (this) {
			if (apds9960EventLc == null && lc != null) {
				gpioObj.setPinEvent(signalPin, TiGPIO.EVENT_FALLINGEDGE, 0);
				TiEventService.getInstance().addListener(this);
				apds9960EventLc = lc;
			} else if (apds9960EventLc != null && lc == null) {
				gpioObj.setPinEvent(signalPin, TiGPIO.EVENT_NONE, 0);
				TiEventService.getInstance().unregisterEvent(this);
				apds9960EventLc = null;
			} else {
			}
		}
		return;
	}
	/**
	 * initializes registers to defaults
	 * 
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		
		this.i2cmObj.setBaudRate(400);
		
		/* Read ID register and check against known values for APDS-9960 */
		int id = wireReadDataByte(TiAPDS9960Register.APDS9960_ID);

//		if (!(id == APDS9960_ID_1 || id == APDS9960_ID_2)) {
//			
//			throw new IOException("Invalid ID:"+id);
//		}

		/* Set ENABLE register to 0 (disable all features) */
		setMode(ALL_MODES, 0);

		/* Set default values for ambient light and proximity registers */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_ATIME, DEFAULT_ATIME);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_WTIME, DEFAULT_WTIME);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_PPULSE, DEFAULT_PROX_PPULSE);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_POFFSET_UR, DEFAULT_POFFSET_UR);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_POFFSET_DL, DEFAULT_POFFSET_DL);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONFIG1, DEFAULT_CONFIG1);
		setLEDDrive(DEFAULT_LDRIVE);
		setProximityGain(DEFAULT_PGAIN);
		setAmbientLightGain(DEFAULT_AGAIN);
		setProxIntLowThresh(DEFAULT_PILT);
		setProxIntHighThresh(DEFAULT_PIHT);
		setLightIntLowThreshold(DEFAULT_AILT);
		setLightIntHighThreshold(DEFAULT_AIHT);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_PERS, DEFAULT_PERS);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONFIG2, DEFAULT_CONFIG2);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONFIG3, DEFAULT_CONFIG3);

		/* Set default values for gesture sense registers */
		setGestureEnterThresh(DEFAULT_GPENTH);
		setGestureExitThresh(DEFAULT_GEXTH);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF1, DEFAULT_GCONF1);
		setGestureGain(DEFAULT_GGAIN);
		setGestureLEDDrive(DEFAULT_GLDRIVE);
		setGestureWaitTime(DEFAULT_GWTIME);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GOFFSET_U, DEFAULT_GOFFSET);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GOFFSET_D, DEFAULT_GOFFSET);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GOFFSET_L, DEFAULT_GOFFSET);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GOFFSET_R, DEFAULT_GOFFSET);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GPULSE, DEFAULT_GPULSE);
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF3, DEFAULT_GCONF3);
		setGestureIntEnable(DEFAULT_GIEN);
	}

	/*******************************************************************************
	 * Public methods for controlling the APDS-9960
	 ******************************************************************************/

	/**
	 * Reads and returns the contents of the ENABLE register
	 * 
	 * @return Contents of the ENABLE register.
	 * @throws IOException
	 */
	public int getMode() throws IOException {
		return wireReadDataByte(TiAPDS9960Register.APDS9960_ENABLE);
	}

	/**
	 * Enables or disables a feature in the APDS-9960
	 * 
	 * @throws IOException
	 *
	 * @param mode
	 *            which feature to enable
	 * @param enable
	 *            ON (1) or OFF (0)
	 */
	public void setMode(int mode, int enable) throws IOException {
		int reg_val;

		/* Read current ENABLE register */
		reg_val = getMode();

		/* Change bit(s) in ENABLE register */
		enable = enable & 0x01;
		if (mode >= 0 && mode <= 6) {
			if (enable > 0) {
				reg_val |= (1 << mode);
			} else {
				reg_val &= ~(1 << mode);
			}
		} else if (mode == ALL_MODES) {
			if (enable > 0) {
				reg_val = 0x7F;
			} else {
				reg_val = 0x00;
			}
		}

		/* Write value back to ENABLE register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_ENABLE, reg_val);

	}

	/**
	 * Starts the light (R/G/B/Ambient) sensor on the APDS-9960
	 * 
	 * @throws IOException
	 *
	 * @param interrupts
	 *            true to enable hardware interrupt on high or low light
	 */
	public void enableLightSensor(boolean interrupts) throws IOException {
		/* Set default gain, interrupts, enable power, and enable sensor */
		setAmbientLightGain(DEFAULT_AGAIN);
		if (interrupts) {
			setAmbientLightIntEnable(1);
		} else {
			setAmbientLightIntEnable(0);
		}

		enablePower();

		setMode(AMBIENT_LIGHT, 1);
	}

	/**
	 * Ends the light sensor on the APDS-9960
	 * 
	 * @throws IOException
	 */
	public void disableLightSensor() throws IOException {
		setAmbientLightIntEnable(0);

		setMode(AMBIENT_LIGHT, 0);
	}

	/**
	 * Starts the proximity sensor on the APDS-9960
	 * 
	 * @throws IOException
	 *
	 * @param interrupts
	 *            true to enable hardware external interrupt on proximity
	 */
	public void enableProximitySensor(boolean interrupts) throws IOException {
		/* Set default gain, LED, interrupts, enable power, and enable sensor */
		setProximityGain(DEFAULT_PGAIN);
		setLEDDrive(DEFAULT_LDRIVE);
		if (interrupts) {
			setProximityIntEnable(1);
		} else {
			setProximityIntEnable(0);
		}

		enablePower();
		setMode(PROXIMITY, 1);
	}

	/**
	 * Ends the proximity sensor on the APDS-9960
	 * 
	 * @throws IOException
	 */
	public void disableProximitySensor() throws IOException {
		setProximityIntEnable(0);
		setMode(PROXIMITY, 0);
	}

	/**
	 * Starts the gesture recognition engine on the APDS-9960
	 * 
	 * @throws IOException
	 *
	 * @param interrupts
	 *            true to enable hardware external interrupt on gesture
	 */
	public void enableGestureSensor(boolean interrupts) throws IOException {

		/*
		 * Enable gesture mode Set ENABLE to 0 (power off) Set WTIME to 0xFF Set
		 * AUX to LED_BOOST_300 Enable PON, WEN, PEN, GEN in ENABLE
		 */
		resetGestureParameters();
		wireWriteDataByte(TiAPDS9960Register.APDS9960_WTIME, 0xFF);

		wireWriteDataByte(TiAPDS9960Register.APDS9960_PPULSE, DEFAULT_GESTURE_PPULSE);

		setLEDBoost(LED_BOOST_300);
		if (interrupts) {
			setGestureIntEnable(1);
		} else {
			setGestureIntEnable(0);
		}

		setGestureMode(1);
		enablePower();

		setMode(WAIT, 1);
		setMode(PROXIMITY, 1);
		setMode(GESTURE, 1);
	}

	/**
	 * Ends the gesture recognition engine on the APDS-9960
	 * 
	 * @throws IOException
	 *
	 */
	public void disableGestureSensor() throws IOException {
		resetGestureParameters();
		setGestureIntEnable(0);
		setGestureMode(0);
		setMode(GESTURE, 0);
	}

	/**
	 * Determines if there is a gesture available for reading
	 *
	 * @return True if gesture available. False otherwise.
	 * @throws IOException
	 */
	public boolean isGestureAvailable() throws IOException {
		/* Read value from GSTATUS register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GSTATUS);

		/* Shift and mask out GVALID bit */
		val &= APDS9960_GVALID;

		/* Return true/false based on GVALID bit */
		if (val == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Processes a gesture event and returns best guessed gesture
	 *
	 * @return Number corresponding to gesture.
	 * @throws IOException
	 */
	public Direction readGesture() throws IOException {
		int fifo_level = 0;
		int gstatus;
		Direction motion;
		int i;

		if((getMode() & 0x41) == 0)
		{
			return Direction.DIR_NONE;
		}

		/* Keep looping as long as gesture data is valid */
		while (true) {

			/* Wait some time to collect next batch of FIFO data */
			Delay.msDelay(20);

			/* Get the contents of the STATUS register. Is data still valid? */
			gstatus = wireReadDataByte(TiAPDS9960Register.APDS9960_GSTATUS);

			/* If we have valid data, read in FIFO */
			if ((gstatus & APDS9960_GVALID) == APDS9960_GVALID) {
				
				/* Read the current FIFO level */
				fifo_level = wireReadDataByte(TiAPDS9960Register.APDS9960_GFLVL);

				System.out.println("fifo_level " + fifo_level);
				/* If there's stuff in the FIFO, read it into our data block */
				if (fifo_level > 0) {
					int len = fifo_level * 4;
					byte[] fifo_data = wireReadDataBlock(TiAPDS9960Register.APDS9960_GFIFO_U, len);
							
					/* If at least 1 set of data, sort the data into U/D/L/R */
					if (len >= 4) {
						for (i = 0; i < len; i += 4) {
							gesture_data.u_data[gesture_data.index] = fifo_data[i + 0] & 0xFF;
							gesture_data.d_data[gesture_data.index] = fifo_data[i + 1] & 0xFF;
							gesture_data.l_data[gesture_data.index] = fifo_data[i + 2] & 0xFF;
							gesture_data.r_data[gesture_data.index] = fifo_data[i + 3] & 0xFF;
							gesture_data.index++;
							gesture_data.total_gestures++;
						}

						/*
						 * Filter and process gesture data. Decode near/far
						 * state
						 */
						if (processGestureData()) {
							if (decodeGesture()) {
								// ***TODO: U-Turn Gestures
							}
						}

						System.out.println("gesture_data.total_gestures  " + gesture_data.total_gestures );
						/* Reset data */
						gesture_data.index = 0;
						gesture_data.total_gestures = 0;
					}
				}
			} else {
				System.out.println("return motion " + gesture_motion);
				/* Determine best guessed gesture and clean up */
				Delay.msDelay(10);
				decodeGesture();
				motion = gesture_motion;
				resetGestureParameters();
				return motion;
			}
		}
	}

	/**
	 * Turn the APDS-9960 on
	 * 
	 * @throws IOException
	 */
	public void enablePower() throws IOException {
		setMode(POWER, 1);
	}

	/**
	 * Turn the APDS-9960 off
	 * 
	 * @throws IOException
	 */
	public void disablePower() throws IOException {
		setMode(POWER, 0);
	}

	/*******************************************************************************
	 * Ambient light and color sensor controls
	 ******************************************************************************/

	/**
	 * Reads the ambient (clear) light level as a 16-bit value
	 * 
	 * @return value of the light sensor.
	 * @throws IOException
	 */
	public int readAmbientLight() throws IOException {
		/* Read value from clear channel, low byte register */
		data[1] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_CDATAL);

		/* Read value from clear channel, high byte register */
		data[0] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_CDATAH);

		return BigBitConverter.ToUInt16(data, 0);
	}

	/**
	 * Reads the red light level as a 16-bit value
	 * 
	 * @return value of the light sensor.
	 * @throws IOException
	 */
	public int readRedLight() throws IOException {
		/* Read value from clear channel, low byte register */
		data[1] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_RDATAL);

		/* Read value from clear channel, high byte register */
		data[0] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_RDATAH);

		return BigBitConverter.ToUInt16(data, 0);
	}

	/**
	 * Reads the green light level as a 16-bit value
	 * 
	 * @return value of the light sensor.
	 * @throws IOException
	 */
	public int readGreenLight() throws IOException {
		/* Read value from clear channel, low byte register */
		data[1] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_GDATAL);

		/* Read value from clear channel, high byte register */
		data[0] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_GDATAH);

		return BigBitConverter.ToUInt16(data, 0);
	}

	/**
	 * Reads the red light level as a 16-bit value
	 * 
	 * @return value of the light sensor.
	 * @throws IOException
	 */
	public int readBlueLight() throws IOException {
		/* Read value from clear channel, low byte register */
		data[1] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_BDATAL);

		/* Read value from clear channel, high byte register */
		data[0] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_BDATAH);

		return BigBitConverter.ToUInt16(data, 0);
	}

	/*******************************************************************************
	 * Proximity sensor controls
	 ******************************************************************************/

	/**
	 * @brief Reads the proximity level as an 8-bit value
	 * @return value of the proximity sensor.
	 * @throws IOException
	 */
	public int readProximity() throws IOException {
		/* Read value from proximity data register */
		return wireReadDataByte(TiAPDS9960Register.APDS9960_PDATA);
	}

	/*******************************************************************************
	 * High-level gesture controls
	 ******************************************************************************/

	/**
	 * Resets all the parameters in the gesture data member
	 */
	private void resetGestureParameters() {
		gesture_data.index = 0;
		gesture_data.total_gestures = 0;

		gesture_ud_delta = 0;
		gesture_lr_delta = 0;

		gesture_ud_count = 0;
		gesture_lr_count = 0;

		gesture_near_count = 0;
		gesture_far_count = 0;

		gesture_state = State.NA_STATE;
		gesture_motion = Direction.DIR_NONE;
	}

	/**
	 * Processes the raw gesture data to determine swipe direction
	 *
	 * @return True if near or far state seen. False otherwise.
	 */
	private boolean processGestureData() {
		int u_first = 0;
		int d_first = 0;
		int l_first = 0;
		int r_first = 0;
		int u_last = 0;
		int d_last = 0;
		int l_last = 0;
		int r_last = 0;
		int ud_ratio_first;
		int lr_ratio_first;
		int ud_ratio_last;
		int lr_ratio_last;
		int ud_delta;
		int lr_delta;
		int i;
		
		/* If we have less than 4 total gestures, that's not enough */
		if (gesture_data.total_gestures <= 4) {
			return false;
		}

		/* Check to make sure our data isn't out of bounds */
		if ((gesture_data.total_gestures <= 32) && (gesture_data.total_gestures > 0)) {

			/* Find the first value in U/D/L/R above the threshold */
			for (i = 0; i < gesture_data.total_gestures; i++) {
				if ((gesture_data.u_data[i] > GESTURE_THRESHOLD_OUT)
						&& (gesture_data.d_data[i] > GESTURE_THRESHOLD_OUT)
						&& (gesture_data.l_data[i] > GESTURE_THRESHOLD_OUT)
						&& (gesture_data.r_data[i] > GESTURE_THRESHOLD_OUT)) {

					u_first = gesture_data.u_data[i];
					d_first = gesture_data.d_data[i];
					l_first = gesture_data.l_data[i];
					r_first = gesture_data.r_data[i];
					break;
				}
			}
			
			/* If one of the _first values is 0, then there is no good data */
			if ((u_first == 0) || (d_first == 0) || (l_first == 0) || (r_first == 0)) {
				return false;
			}

			/* Find the last value in U/D/L/R above the threshold */
			for (i = gesture_data.total_gestures - 1; i >= 0; i--) {
				if ((gesture_data.u_data[i] > GESTURE_THRESHOLD_OUT)
						&& (gesture_data.d_data[i] > GESTURE_THRESHOLD_OUT)
						&& (gesture_data.l_data[i] > GESTURE_THRESHOLD_OUT)
						&& (gesture_data.r_data[i] > GESTURE_THRESHOLD_OUT)) {

					u_last = gesture_data.u_data[i];
					d_last = gesture_data.d_data[i];
					l_last = gesture_data.l_data[i];
					r_last = gesture_data.r_data[i];
					break;
				}
			}
			
		}

		/* Calculate the first vs. last ratio of up/down and left/right */
		ud_ratio_first = ((u_first - d_first) * 100) / (u_first + d_first);
		lr_ratio_first = ((l_first - r_first) * 100) / (l_first + r_first);
		ud_ratio_last = ((u_last - d_last) * 100) / (u_last + d_last);
		lr_ratio_last = ((l_last - r_last) * 100) / (l_last + r_last);
		
//		System.out.println("u_first = " + u_first);
//		System.out.println("d_first = " + d_first);
//		System.out.println("l_first = " + l_first);
//		System.out.println("r_first = " + r_first);
//	
//		
//		System.out.println("u_last = " + u_last);
//		System.out.println("d_last = " + d_last);
//		System.out.println("l_last = " + l_last);
//		System.out.println("r_last = " + r_last);
//
//		System.out.println("ud_ratio_first = " + ud_ratio_first);
//		System.out.println("ud_ratio_last = " + ud_ratio_last);
//		System.out.println("lr_ratio_first = " + lr_ratio_first);
//		System.out.println("lr_ratio_last = " + lr_ratio_last);

		/* Determine the difference between the first and last ratios */
		ud_delta = ud_ratio_last - ud_ratio_first;
		lr_delta = lr_ratio_last - lr_ratio_first;
		
		/* Accumulate the UD and LR delta values */
		gesture_ud_delta += ud_delta;
		gesture_lr_delta += lr_delta;

		
//		System.out.println("ud_delta = " + ud_delta);
//		System.out.println("lr_delta = " + lr_delta);
//	
//		System.out.println("gesture_ud_delta = " + gesture_ud_delta);
//		System.out.println("gesture_lr_delta = " + gesture_lr_delta);

		
		/* Determine U/D gesture */
		if (gesture_ud_delta >= GESTURE_SENSITIVITY_1) {
			gesture_ud_count = 1;
		} else if (gesture_ud_delta <= -GESTURE_SENSITIVITY_1) {
			gesture_ud_count = -1;
		} else {
			gesture_ud_count = 0;
		}

		/* Determine L/R gesture */
		if (gesture_lr_delta >= GESTURE_SENSITIVITY_1) {
			gesture_lr_count = 1;
		} else if (gesture_lr_delta <= -GESTURE_SENSITIVITY_1) {
			gesture_lr_count = -1;
		} else {
			gesture_lr_count = 0;
		}
		
		System.out.println("gesture_ud_count = " + gesture_ud_count);
		System.out.println("gesture_lr_count = " + gesture_lr_count);
	
		/* Determine Near/Far gesture */
		if ((gesture_ud_count == 0) && (gesture_lr_count == 0)) {
			if ((Math.abs(ud_delta) < GESTURE_SENSITIVITY_2) && (Math.abs(lr_delta) < GESTURE_SENSITIVITY_2)) {

				if ((ud_delta == 0) && (lr_delta == 0)) {
					gesture_near_count++;
				} else if ((ud_delta != 0) || (lr_delta != 0)) {
					gesture_far_count++;
				}

				if ((gesture_near_count >= 10) && (gesture_far_count >= 2)) {
					if ((ud_delta == 0) && (lr_delta == 0)) {
						gesture_state = State.NEAR_STATE;
					} else if ((ud_delta != 0) && (lr_delta != 0)) {
						gesture_state = State.FAR_STATE;
					}
					
					System.out.println("true - gesture_state " + gesture_state);
					return true;
				}
			}
		} else {
			if ((Math.abs(ud_delta) < GESTURE_SENSITIVITY_2) && (Math.abs(lr_delta) < GESTURE_SENSITIVITY_2)) {

				if ((ud_delta == 0) && (lr_delta == 0)) {
					gesture_near_count++;
				}

				if (gesture_near_count >= 10) {
					gesture_ud_count = 0;
					gesture_lr_count = 0;
					gesture_ud_delta = 0;
					gesture_lr_delta = 0;
				}
			}
			
		}
		
//		System.out.println("gesture_ud_count = " + gesture_ud_count);
//		System.out.println("gesture_lr_count = " + gesture_lr_count);
//	
//		System.out.println("gesture_ud_delta = " + gesture_ud_delta);
//		System.out.println("gesture_lr_delta = " + gesture_lr_delta);
	
		
		System.out.println("false - gesture_state " + gesture_state);

		return false;
	}

	/**
	 * Determines swipe direction or near/far state
	 *
	 * @return True if near/far event. False otherwise.
	 */
	private boolean decodeGesture() {
		
		System.out.println("decodeGesture gesture_state " + gesture_state);
		
		/* Return if near or far event is detected */
		if (gesture_state == State.NEAR_STATE) {
			gesture_motion = Direction.DIR_NEAR;
			return true;
		} else if (gesture_state == State.FAR_STATE) {
			gesture_motion = Direction.DIR_FAR;
			return true;
		}

		/* Determine swipe direction */
		if ((gesture_ud_count == -1) && (gesture_lr_count == 0)) {
			gesture_motion = Direction.DIR_UP;
		} else if ((gesture_ud_count == 1) && (gesture_lr_count == 0)) {
			gesture_motion = Direction.DIR_DOWN;
		} else if ((gesture_ud_count == 0) && (gesture_lr_count == 1)) {
			gesture_motion = Direction.DIR_RIGHT;
		} else if ((gesture_ud_count == 0) && (gesture_lr_count == -1)) {
			gesture_motion = Direction.DIR_LEFT;
		} else if ((gesture_ud_count == -1) && (gesture_lr_count == 1)) {
			if (Math.abs(gesture_ud_delta) > Math.abs(gesture_lr_delta)) {
				gesture_motion = Direction.DIR_UP;
			} else {
				gesture_motion = Direction.DIR_RIGHT;
			}
		} else if ((gesture_ud_count == 1) && (gesture_lr_count == -1)) {
			if (Math.abs(gesture_ud_delta) > Math.abs(gesture_lr_delta)) {
				gesture_motion = Direction.DIR_DOWN;
			} else {
				gesture_motion = Direction.DIR_LEFT;
			}
		} else if ((gesture_ud_count == -1) && (gesture_lr_count == -1)) {
			if (Math.abs(gesture_ud_delta) > Math.abs(gesture_lr_delta)) {
				gesture_motion = Direction.DIR_UP;
			} else {
				gesture_motion = Direction.DIR_LEFT;
			}
		} else if ((gesture_ud_count == 1) && (gesture_lr_count == 1)) {
			if (Math.abs(gesture_ud_delta) > Math.abs(gesture_lr_delta)) {
				gesture_motion = Direction.DIR_DOWN;
			} else {
				gesture_motion = Direction.DIR_RIGHT;
			}
		} else {
			return false;
		}

		return true;
	}

	/*******************************************************************************
	 * Getters and setters for register values
	 ******************************************************************************/

	/**
	 * Returns the lower threshold for proximity detection
	 *
	 * @return lower threshold
	 * @throws IOException
	 */
	public int getProxIntLowThresh() throws IOException {
		/* Read value from PILT register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_PILT);

		return val;
	}

	/**
	 * Sets the lower threshold for proximity detection
	 * 
	 * @throws IOException
	 *
	 * @param threshold
	 *            the lower proximity threshold
	 */
	public void setProxIntLowThresh(int threshold) throws IOException {
		wireWriteDataByte(TiAPDS9960Register.APDS9960_PILT, threshold);
	}

	/**
	 * Returns the high threshold for proximity detection
	 *
	 * @return high threshold
	 * @throws IOException
	 */
	public int getProxIntHighThresh() throws IOException {
		/* Read value from PIHT register */
		return wireReadDataByte(TiAPDS9960Register.APDS9960_PIHT);
	}

	/**
	 * Sets the high threshold for proximity detection
	 * 
	 * @throws IOException
	 *
	 * @param[in] threshold the high proximity threshold
	 */
	public void setProxIntHighThresh(int threshold) throws IOException {
		wireWriteDataByte(TiAPDS9960Register.APDS9960_PIHT, threshold);
	}

	/**
	 * Returns LED drive strength for proximity and ALS
	 *
	 * Value LED Current 0 100 mA 1 50 mA 2 25 mA 3 12.5 mA
	 *
	 * @return the value of the LED drive strength. 0xFF on failure.
	 * @throws IOException
	 */
	public int getLEDDrive() throws IOException {
		/* Read value from CONTROL register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONTROL);

		/* Shift and mask out LED drive bits */
		val = (val >> 6) & 0x03;

		return val;
	}

	/**
	 * Sets the LED drive strength for proximity and ALS
	 *
	 * Value LED Current 0 100 mA 1 50 mA 2 25 mA 3 12.5 mA
	 * 
	 * @throws IOException
	 *
	 * @param[in] drive the value (0-3) for the LED drive strength
	 */
	public void setLEDDrive(int drive) throws IOException {
		/* Read value from CONTROL register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONTROL);

		/* Set bits in register to given value */
		drive &= 0x03;
		drive = drive << 6;
		val &= 0x3F;
		val |= drive;

		/* Write register value back into CONTROL register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONTROL, val);
	}

	/**
	 * Returns receiver gain for proximity detection
	 *
	 * Value Gain 0 1x 1 2x 2 4x 3 8x
	 *
	 * @return the value of the proximity gain. 0xFF on failure.
	 * @throws IOException
	 */
	public int getProximityGain() throws IOException {
		/* Read value from CONTROL register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONTROL);

		/* Shift and mask out PDRIVE bits */
		val = (val >> 2) & 0x03;

		return val;
	}

	/**
	 * Sets the receiver gain for proximity detection
	 *
	 * Value Gain 0 1x 1 2x 2 4x 3 8x
	 * 
	 * @throws IOException
	 *
	 * @param drive
	 *            the value (0-3) for the gain
	 */
	public void setProximityGain(int drive) throws IOException {
		/* Read value from CONTROL register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONTROL);

		/* Set bits in register to given value */
		drive &= 0x03;
		drive = drive << 2;
		val &= 0xF3;
		val |= drive;

		/* Write register value back into CONTROL register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONTROL, val);
	}

	/**
	 * Returns receiver gain for the ambient light sensor (ALS)
	 *
	 * Value Gain 0 1x 1 4x 2 16x 3 64x
	 *
	 * @return the value of the ALS gain. 0xFF on failure.
	 * @throws IOException
	 */
	public int getAmbientLightGain() throws IOException {
		/* Read value from CONTROL register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONTROL);

		/* Shift and mask out ADRIVE bits */
		val &= 0x03;

		return val;
	}

	/**
	 * Sets the receiver gain for the ambient light sensor (ALS)
	 *
	 * Value Gain 0 1x 1 4x 2 16x 3 64x
	 * 
	 * @throws IOException
	 *
	 * @param[in] drive the value (0-3) for the gain
	 */
	public void setAmbientLightGain(int drive) throws IOException {
		/* Read value from CONTROL register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONTROL);

		/* Set bits in register to given value */
		drive &= 0x03;
		val &= 0xFC;
		val |= drive;

		/* Write register value back into CONTROL register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONTROL, val);
	}

	/**
	 * Get the current LED boost value
	 * 
	 * Value Boost Current 0 100% 1 150% 2 200% 3 300%
	 * 
	 * @return The LED boost value. 0xFF on failure.
	 * @throws IOException
	 */
	public int getLEDBoost() throws IOException {
		/* Read value from CONFIG2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONFIG2);

		/* Shift and mask out LED_BOOST bits */
		val = (val >> 4) & 0x03;

		return val;
	}

	/**
	 * Sets the LED current boost value
	 *
	 * Value Boost Current 0 100% 1 150% 2 200% 3 300%
	 * 
	 * @throws IOException
	 *
	 * @param drive
	 *            the value (0-3) for current boost (100-300%)
	 */
	public void setLEDBoost(int boost) throws IOException {
		/* Read value from CONFIG2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONFIG2);

		/* Set bits in register to given value */
		boost &= 0x03;
		boost = boost << 4;
		val &= 0xCF;
		val |= boost;

		/* Write register value back into CONFIG2 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONFIG2, val);
	}

	/**
	 * Gets proximity gain compensation enable
	 *
	 * @return 1 if compensation is enabled. 0 if not. 0xFF on error.
	 * @throws IOException
	 */
	public int getProxGainCompEnable() throws IOException {
		/* Read value from CONFIG3 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONFIG3);

		/* Shift and mask out PCMP bits */
		val = (val >> 5) & 0x01;

		return val;
	}

	/**
	 * Sets the proximity gain compensation enable
	 * 
	 * @throws IOException
	 *
	 * @param enable
	 *            1 to enable compensation. 0 to disable compensation.
	 */
	public void setProxGainCompEnable(int enable) throws IOException {
		/* Read value from CONFIG3 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONFIG3);

		/* Set bits in register to given value */
		enable &= 0x01;
		enable = enable << 5;
		val &= 0xDF;
		val |= enable;

		/* Write register value back into CONFIG3 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONFIG3, val);
	}

	/**
	 * Gets the current mask for enabled/disabled proximity photodiodes
	 *
	 * 1 = disabled, 0 = enabled Bit Photodiode 3 UP 2 DOWN 1 LEFT 0 RIGHT
	 *
	 * @return Current proximity mask for photodiodes.
	 * @throws IOException
	 */
	public int getProxPhotoMask() throws IOException {
		/* Read value from CONFIG3 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONFIG3);

		/* Mask out photodiode enable mask bits */
		val &= 0x0F;

		return val;
	}

	/**
	 * Sets the mask for enabling/disabling proximity photodiodes
	 *
	 * 1 = disabled, 0 = enabled Bit Photodiode 3 UP 2 DOWN 1 LEFT 0 RIGHT
	 * 
	 * @throws IOException
	 *
	 * @param mask
	 *            4-bit mask value
	 */
	public void setProxPhotoMask(int mask) throws IOException {
		/* Read value from CONFIG3 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_CONFIG3);

		/* Set bits in register to given value */
		mask &= 0x0F;
		val &= 0xF0;
		val |= mask;

		/* Write register value back into CONFIG3 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_CONFIG3, val);
	}

	/**
	 * Gets the entry proximity threshold for gesture sensing
	 *
	 * @return Current entry proximity threshold.
	 * @throws IOException
	 */
	public int getGestureEnterThresh() throws IOException {
		/* Read value from GPENTH register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GPENTH);
		return val;
	}

	/**
	 * Sets the entry proximity threshold for gesture sensing
	 * 
	 * @throws IOException
	 *
	 * @param threshold
	 *            proximity value needed to start gesture mode
	 */
	public void setGestureEnterThresh(int threshold) throws IOException {
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GPENTH, threshold);
	}

	/**
	 * Gets the exit proximity threshold for gesture sensing
	 *
	 * @return Current exit proximity threshold.
	 * @throws IOException
	 */
	public int getGestureExitThresh() throws IOException {
		/* Read value from GEXTH register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GEXTH);

		return val;
	}

	/**
	 * Sets the exit proximity threshold for gesture sensing
	 *
	 * @param threshold
	 *            proximity value needed to end gesture mode
	 * @throws IOException
	 */
	public void setGestureExitThresh(int threshold) throws IOException {
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GEXTH, threshold);
	}

	/**
	 * Gets the gain of the photodiode during gesture mode
	 *
	 * Value Gain 0 1x 1 2x 2 4x 3 8x
	 * 
	 * @return the current photodiode gain.
	 * @throws IOException
	 */
	public int getGestureGain() throws IOException {
		/* Read value from GCONF2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF2);

		/* Shift and mask out GGAIN bits */
		val = (val >> 5) & 0x03;

		return val;
	}

	/**
	 * Sets the gain of the photodiode during gesture mode
	 *
	 * Value Gain 0 1x 1 2x 2 4x 3 8x
	 *
	 * @throws IOException
	 *
	 * @param gain
	 *            the value for the photodiode gain
	 */
	public void setGestureGain(int gain) throws IOException {
		/* Read value from GCONF2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF2);

		/* Set bits in register to given value */
		gain &= 0x03;
		gain = gain << 5;
		val &= 0x9F;
		val |= gain;

		/* Write register value back into GCONF2 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF2, val);
	}

	/**
	 * Gets the drive current of the LED during gesture mode
	 *
	 * Value LED Current 0 100 mA 1 50 mA 2 25 mA 3 12.5 mA
	 *
	 * @return the LED drive current value.
	 * @throws IOException
	 */
	public int getGestureLEDDrive() throws IOException {
		/* Read value from GCONF2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF2);

		/* Shift and mask out GLDRIVE bits */
		val = (val >> 3) & 0x03;

		return val;
	}

	/**
	 * Sets the LED drive current during gesture mode
	 *
	 * Value LED Current 0 100 mA 1 50 mA 2 25 mA 3 12.5 mA
	 * 
	 * @throws IOException
	 *
	 * @param drive
	 *            the value for the LED drive current
	 */
	public void setGestureLEDDrive(int drive) throws IOException {
		/* Read value from GCONF2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF2);

		/* Set bits in register to given value */
		drive &= 0x03;
		drive = drive << 3;
		val &= 0xE7;
		val |= drive;

		/* Write register value back into GCONF2 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF2, val);
	}

	/**
	 * Gets the time in low power mode between gesture detections
	 *
	 * Value Wait time 0:0 ms 1:2.8 ms 2:5.6 ms 3:8.4 ms 4:14.0 ms 5: 22.4 ms 6: 30.8 ms 7:39.2 ms
	 * 
	 * @return the current wait time between gestures.
	 * @throws IOException
	 */
	public int getGestureWaitTime() throws IOException {
		/* Read value from GCONF2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF2);

		/* Mask out GWTIME bits */
		val &= 0x07;

		return val;
	}

	/**
	 * Sets the time in low power mode between gesture detections
	 *
	 * Value Wait time 0 0 ms 1 2.8 ms 2 5.6 ms 3 8.4 ms 4 14.0 ms 5 22.4 ms 6
	 * 30.8 ms 7 39.2 ms
	 * 
	 * @throws IOException
	 *
	 * @param[in] the value for the wait time
	 */
	public void setGestureWaitTime(int time) throws IOException {
		/* Read value from GCONF2 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF2);

		/* Set bits in register to given value */
		time &= 0x07;
		val &= 0xF8;
		val |= time;

		/* Write register value back into GCONF2 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF2, val);
	}

	/**
	 * Gets the low threshold for ambient light interrupts
	 *
	 * @return threshold current low threshold stored on the APDS-9960
	 * @throws IOException
	 */
	public int getLightIntLowThreshold() throws IOException {

		/* Read value from ambient light low threshold, low byte register */
		data[1] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_AILTL);

		/* Read value from ambient light low threshold, high byte register */
		data[0] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_AILTH);

		return BigBitConverter.ToUInt16(data, 0);
	}

	/**
	 * Sets the low threshold for ambient light interrupts
	 * 
	 * @throws IOException
	 *
	 * @param[in] threshold low threshold value for interrupt to trigger
	 */
	public void setLightIntLowThreshold(int threshold) throws IOException {
		int val_low;
		int val_high;

		/* Break 16-bit threshold into 2 8-bit values */
		val_low = threshold & 0x00FF;
		val_high = (threshold & 0xFF00) >>> 8;

		/* Write low byte */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_AILTL, val_low);

		/* Write high byte */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_AILTH, val_high);
	}

	/**
	 * Gets the high threshold for ambient light interrupts
	 *
	 * @return threshold current low threshold stored on the APDS-9960
	 * @throws IOException
	 */
	public int getLightIntHighThreshold() throws IOException {
		/* Read value from ambient light high threshold, low byte register */
		data[1] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_AIHTL);

		/* Read value from ambient light high threshold, high byte register */
		data[0] = (byte) wireReadDataByte(TiAPDS9960Register.APDS9960_AIHTH);

		return BigBitConverter.ToUInt16(data, 0);
	}

	/**
	 * Sets the high threshold for ambient light interrupts
	 * 
	 * @throws IOException
	 *
	 * @param[in] threshold high threshold value for interrupt to trigger
	 */
	public void setLightIntHighThreshold(int threshold) throws IOException {
		int val_low;
		int val_high;

		/* Break 16-bit threshold into 2 8-bit values */
		val_low = threshold & 0x00FF;
		val_high = (threshold & 0xFF00) >>> 8;

		/* Write low byte */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_AIHTL, val_low);

		/* Write high byte */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_AIHTH, val_high);
	}

	/**
	 * Gets the low threshold for proximity interrupts
	 *
	 * @return threshold current low threshold stored on the APDS-9960
	 * @throws IOException
	 */
	public int getProximityIntLowThreshold() throws IOException {
		/* Read value from proximity low threshold register */
		return wireReadDataByte(TiAPDS9960Register.APDS9960_PILT);
	}

	/**
	 * Sets the low threshold for proximity interrupts
	 * 
	 * @throws IOException
	 *
	 * @param[in] threshold low threshold value for interrupt to trigger
	 */
	public void setProximityIntLowThreshold(int threshold) throws IOException {
		/* Write threshold value to register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_PILT, threshold);
	}

	/**
	 * Gets the high threshold for proximity interrupts
	 *
	 * @return threshold current high threshold stored on the APDS-9960
	 * @throws IOException
	 */
	public int getProximityIntHighThreshold() throws IOException {
		/* Read value from proximity low threshold register */
		return wireReadDataByte(TiAPDS9960Register.APDS9960_PIHT);
	}

	/**
	 * Sets the high threshold for proximity interrupts
	 * 
	 * @throws IOException
	 *
	 * @param[in] threshold high threshold value for interrupt to trigger
	 */
	public void setProximityIntHighThreshold(int threshold) throws IOException {
		/* Write threshold value to register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_PIHT, threshold);
	}

	/**
	 * Gets if ambient light interrupts are enabled or not
	 *
	 * @return 1 if interrupts are enabled, 0 if not.
	 * @throws IOException
	 */
	public int getAmbientLightIntEnable() throws IOException {
		/* Read value from ENABLE register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_ENABLE);

		/* Shift and mask out AIEN bit */
		val = (val >> 4) & 0x01;

		return val;
	}

	/**
	 * Turns ambient light interrupts on or off
	 * 
	 * @throws IOException
	 *
	 * @param[in] enable 1 to enable interrupts, 0 to turn them off
	 */
	public void setAmbientLightIntEnable(int enable) throws IOException {
		/* Read value from ENABLE register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_ENABLE);

		/* Set bits in register to given value */
		enable &= 0x01;
		enable = enable << 4;
		val &= 0xEF;
		val |= enable;

		/* Write register value back into ENABLE register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_ENABLE, val);
	}

	/**
	 * Gets if proximity interrupts are enabled or not
	 *
	 * @return 1 if interrupts are enabled, 0 if not.
	 * @throws IOException
	 */
	public int getProximityIntEnable() throws IOException {
		/* Read value from ENABLE register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_ENABLE);

		/* Shift and mask out PIEN bit */
		val = (val >> 5) & 0x01;

		return val;
	}

	/**
	 * Turns proximity interrupts on or off
	 * 
	 * @throws IOException
	 *
	 * @param[in] enable 1 to enable interrupts, 0 to turn them off
	 */
	public void setProximityIntEnable(int enable) throws IOException {
		/* Read value from ENABLE register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_ENABLE);

		/* Set bits in register to given value */
		enable &= 0x01;
		enable = enable << 5;
		val &= 0xDF;
		val |= enable;

		/* Write register value back into ENABLE register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_ENABLE, val);
	}

	/**
	 * Gets if gesture interrupts are enabled or not
	 *
	 * @return 1 if interrupts are enabled, 0 if not. 0xFF on error.
	 * @throws IOException
	 */
	public int getGestureIntEnable() throws IOException {
		/* Read value from GCONF4 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF4);

		/* Shift and mask out GIEN bit */
		val = (val >>> 1) & 0x01;

		return val;
	}

	/**
	 * Turns gesture-related interrupts on or off
	 * 
	 * @throws IOException
	 *
	 * @param[in] enable 1 to enable interrupts, 0 to turn them off
	 */
	public void setGestureIntEnable(int enable) throws IOException {
		/* Read value from GCONF4 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF4);

		/* Set bits in register to given value */
		enable &= 0x01;
		enable = enable << 1;
		val &= 0xFD;
		val |= enable;

		/* Write register value back into GCONF4 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF4, val);
	}

	/**
	 * Clears the ambient light interrupt
	 * 
	 * @throws IOException
	 */
	public void clearAmbientLightInt() throws IOException {
		wireReadDataByte(TiAPDS9960Register.APDS9960_AICLEAR);
	}

	/**
	 * Clears the proximity interrupt
	 * 
	 * @throws IOException
	 */
	public void clearProximityInt() throws IOException {
		wireReadDataByte(TiAPDS9960Register.APDS9960_PICLEAR);
	}

	/**
	 * Tells if the gesture state machine is currently running
	 *
	 * @return 1 if gesture state machine is running, 0 if not.
	 * @throws IOException
	 */
	public int getGestureMode() throws IOException {
		/* Read value from GCONF4 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF4);

		/* Mask out GMODE bit */
		val &= 0x01;

		return val;
	}

	/**
	 * Tells the state machine to either enter or exit gesture state machine
	 * 
	 * @throws IOException
	 *
	 * @param[in] mode 1 to enter gesture state machine, 0 to exit.
	 */
	public void setGestureMode(int mode) throws IOException {
		/* Read value from GCONF4 register */
		int val = wireReadDataByte(TiAPDS9960Register.APDS9960_GCONF4);

		/* Set bits in register to given value */
		mode &= 0x01;
		val &= 0xFE;
		val |= mode;

		/* Write register value back into GCONF4 register */
		wireWriteDataByte(TiAPDS9960Register.APDS9960_GCONF4, val);

	}

	/*******************************************************************************
	 * Raw I2C Reads and Writes
	 ******************************************************************************/

	/**
	 * Writes a single byte to the I2C device and specified register
	 * 
	 * @throws IOException
	 *
	 * @param reg
	 *            the register in the I2C device to write to
	 * @param val
	 *            the 1-byte value to write to the I2C device
	 */
	private void wireWriteDataByte(int reg, int val) throws IOException {
		data[0] = (byte) val;
		this.i2cmObj.write(this.i2cSlaveAddr, reg, data, 0, 1);
	}

	/**
	 * Reads a single byte from the I2C device and specified register
	 * 
	 * @throws IOException
	 *
	 * @param reg
	 *            the register to read from
	 * @param the
	 *            value returned from the register
	 */
	private int wireReadDataByte(int reg) throws IOException {
		/* Indicate which register we want to read from */
		this.i2cmObj.read(this.i2cSlaveAddr, reg, data, 0, 1);
				
		return data[0] & 0xff;
	}

	/**
	 * Reads a block (array) of bytes from the I2C device and register
	 *
	 * @param reg
	 *            the register to read from
	 * @param len
	 *            number of bytes to read
	 * @return the data.
	 * @throws IOException
	 */
	private byte[] wireReadDataBlock(int reg, int len) throws IOException {
		/* Indicate which register we want to read from */
		/* Read block data */
		this.i2cmObj.read(this.i2cSlaveAddr, reg, data, 0, len);

		return data;
	}

	@Override
	public TiEventType getEventType() {
		return TiEventType.GPIO;
	}

	@Override
	public void onEvent(ITiEvent evt) {
		synchronized (this) {
			TiGPIOEvent event = (TiGPIOEvent) evt;
			if (event.getPin() == signalPin) {
				eventTime = event.getTime();
				if (apds9960EventLc != null)
					apds9960EventLc.onThresholdNotify(this);
			}
		}
		
	}
	
	/**
	 * Gets the value of DOUT
	 * 
	 * @return level
	 * @throws IOException
	 */
	public int getDigitalOutput() throws IOException {
		return gpioObj.readPin(signalPin);
	}
	

	/**
	 * Gets the event time
	 * 
	 * @return event time, unit:us
	 */
	public long getEventTime() {
		return eventTime;
	}
}
