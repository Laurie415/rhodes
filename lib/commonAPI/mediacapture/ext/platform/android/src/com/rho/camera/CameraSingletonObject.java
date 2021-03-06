package com.rho.camera;

import java.io.File;
import java.util.Map;
import java.io.FileNotFoundException;
import android.content.ContentResolver;
import com.rhomobile.rhodes.util.ContextFactory;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.rhomobile.rhodes.Logger;
import com.rhomobile.rhodes.RhodesActivity;
import com.rhomobile.rhodes.api.IMethodResult;
import com.rhomobile.rhodes.ui.FileList;

public class CameraSingletonObject implements ICameraSingletonObject {
    private static final String TAG = CameraSingletonObject.class.getSimpleName();
    public static boolean deprecated_choose_pic;
    private int mId;
    
    static int getCameraIndex(String id) {
        return Integer.valueOf(id.substring(7)).intValue();
    }
    static String getCameraId(int idx) {
        return "camera#" + String.valueOf(idx);
    }

    @Override
    public int getCameraCount() {
        Logger.T(TAG, "getCameraCount");
        return 1;
    }

    public CameraSingletonObject() {
        mId = 0;
    }
    
    @Override
    public String getDefaultID() {
        return getCameraId(mId);
    }

    @Override
    public void setDefaultID(String id) {
        mId = getCameraIndex(id);
    }

    public void setDefaultIndex(int id) {
        mId = id;
    }

    @Override
    public void enumerate(IMethodResult result) {
        Logger.T(TAG, "enumerate");
        int cameraCount = getCameraCount();
        Logger.T(TAG, "Number of cameras: " + cameraCount);
        for (int i = 0 ; i < cameraCount; i++) {
            result.collect(getCameraId(i));
            Log.d(TAG, "Camera id's: "+ getCameraId(i));
        }
        result.set();
    }

    @Override
    public void getCameraByType(String cameraType, IMethodResult result) {
        result.set(getDefaultID());
    }

    @Override
    public void choosePicture(Map<String, String> propertyMap, IMethodResult result) {
    	if(propertyMap.get("deprecated") == null || propertyMap.get("deprecated").equalsIgnoreCase("false")){   
    		propertyMap.put("deprecated", "false");
    		deprecated_choose_pic = false;    	
    	}
    	else
    		deprecated_choose_pic = true;
        Intent intent = null;
        String outputFormat = null;
	if(propertyMap.get("outputFormat") == null){
	    propertyMap.put("outputFormat", "image");
	    outputFormat = propertyMap.get("outputFormat");	        	
	}
	else{
	     outputFormat = propertyMap.get("outputFormat");	         
	}
        CameraFactory factory = (CameraFactory)CameraFactorySingleton.getInstance();
        factory.getRhoListener().setMethodResult(result);
        factory.getRhoListener().setActualPropertyMap(propertyMap);
        RhodesActivity ra = RhodesActivity.safeGetInstance();
        intent = new Intent(ra, FileList.class);
        String fileName = null;
	if(!propertyMap.containsKey("fileName")){
	   fileName = "/sdcard/DCIM/Camera/";
	}
	else{
	   fileName = propertyMap.get("fileName");
	}
	if (fileName != null && fileName.length() > 0) {
	        if (outputFormat.equalsIgnoreCase("image")) {        	
	           String tmpPath = getTemporaryLoc(fileName);
	            if (tmpPath == null) {
	                throw new RuntimeException("Failed to access shared temporary folder");
	            }
	            Uri captureUri = Uri.fromFile(new File(tmpPath));
	            propertyMap.put("captureUri", captureUri.toString());
	            intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
	        }
	        else 
	        if(outputFormat.equalsIgnoreCase("dataUri"))
	        {        	
	            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName + ".jpg")));
	        }        
	        ra.startActivityForResult(intent, 0);
        }
         else {        	
            result.setArgError("'fileName' parameter is missed");
        }
    }

    private String getTemporaryLoc(String targetPath) {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalRoot = Environment.getExternalStorageDirectory();
            if (! externalRoot.exists()){
                if (! externalRoot.mkdirs()){
                    Logger.E(TAG, "Failed to create directory: " + externalRoot);
                    return null;
                }
            }
            String filename = new File(targetPath).getName();
            return new File(externalRoot, filename).getAbsolutePath();
        } else {
            return null;
        }
    }
   /* @Override
    public void saveImageToDeviceGallery(String pathToImage, IMethodResult result) {
        // TODO Auto-generated method stub

    }
    */

    @Override
    public ICameraObject createCameraObject(String id) {
        Logger.T(TAG, "createCameraObject: " + id);
        return new CameraObject(id);
    }
	@Override
	public void copyImageToDeviceGallery(String pathToImage,
			IMethodResult result) {
		// TODO Auto-generated method stub
		ContentResolver contentResolver = ContextFactory.getContext().getContentResolver();
		String imageName = pathToImage.substring(pathToImage.lastIndexOf("/")+1, pathToImage.length());
		String strUri = null;
		try {
			strUri = MediaStore.Images.Media.insertImage(contentResolver, pathToImage, imageName, "Saving Image to Device Gallery through Camera");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
