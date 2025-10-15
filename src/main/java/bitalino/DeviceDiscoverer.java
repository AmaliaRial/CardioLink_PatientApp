package bitalino;

import javax.bluetooth.*;
import java.util.Vector;

public class DeviceDiscoverer implements DiscoveryListener {


    public Vector<RemoteDevice> remoteDevices = new Vector<RemoteDevice>();
    DiscoveryAgent discoveryAgent;
    public String deviceName;
    String inqStatus = null;

    /**
     * Constructor that initializes the Bluetooth device discovery process.
     */
    public DeviceDiscoverer() {
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            System.err.println(LocalDevice.getLocalDevice());
            discoveryAgent = localDevice.getDiscoveryAgent();
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);            

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Callback method when a device is discovered.
     * @param remoteDevice The discovered remote device.
     * @param cod The class of device.
     */
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass cod) {
        
    	try
    	{
           deviceName=remoteDevice.getFriendlyName(false); //Records devices names
           if (deviceName.equalsIgnoreCase("bitalino")) 
           {
	           remoteDevices.addElement(remoteDevice);
           }
          
        } 
    	catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Callback method when the inquiry is completed.
     * @param discType The type of completion (completed, terminated, error).
     */
    public void inquiryCompleted(int discType) 
    {
    
	    if (discType == DiscoveryListener.INQUIRY_COMPLETED) 
	    {
	        inqStatus = "Scan completed.";
	    }
	    else if (discType == DiscoveryListener.INQUIRY_TERMINATED) 
	    {
	        inqStatus = "Scan terminated.";
	    }
	    else if (discType == DiscoveryListener.INQUIRY_ERROR) 
	    {
	        inqStatus = "Scan with errors.";
	    }
    }

    /**
     * Callback method when services are discovered.
     * @param transID
     * @param servRecord
     */
    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord){}

    /**
     * Callback method when service search is completed.
     * @param transID
     * @param respCode
     */
    @Override
    public void serviceSearchCompleted(int transID, int respCode) {}
}
