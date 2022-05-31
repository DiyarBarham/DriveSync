package com.example.drivesync.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.drivesync.dto.FileItemDTO;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import lombok.Data;

@Data
public class DriveService {

	
	public boolean download(String FileId, String filePath, Drive drive) throws IOException
	{
		
	    if(drive != null)
	    {
	        
	            Drive.Files.Get FileDownloadRequest = drive.files().get(FileId);
	            FileDownloadRequest.getMediaHttpDownloader();
	            try {
					FileDownloadRequest.executeMediaAndDownloadTo(new FileOutputStream(filePath));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
	       

	        return true;
	    }

	    return false;
	}
	
	public MessegeService parsing(String FileId, String filePath, Drive drive) throws IOException
	{
		
		List<File> resultList = new LinkedList<File>();
		java.io.File f1 = new java.io.File(filePath);
		f1.mkdir();
		Drive.Files.List request = drive.files().list().setFields("nextPageToken, files");
		request.setQ("trashed = false and '" + FileId + "' in parents");
		do {
			FileList fileList = request.execute();
			List<File> items = fileList.getFiles();
			resultList.addAll(items);
			request.setPageToken(fileList.getNextPageToken());
		} while (request.getPageToken() != null && request.getPageToken().length() > 0);
		for (File file : resultList) {
			if(file.getMimeType().equals("application/vnd.google-apps.folder")) {
				parsing(file.getId(),filePath+"/"+file.getName(), drive);
			} else {
				System.out.println(file.getName());
				System.out.println(filePath);
				download(file.getId(),filePath+"/"+file.getName(), drive);
			}
			
		}
		MessegeService message = new MessegeService();
		message.setMessage("Download has been done succesfully.");
		return message;
	}
	
	public List<FileItemDTO> listFiles(FileList fileList) throws Exception {
	
		List<FileItemDTO> responseList = new ArrayList<>();
		for (File file : fileList.getFiles()) {
			FileItemDTO item = new FileItemDTO();
			item.setId(file.getId());
			item.setName(file.getName());
			item.setThumbnailLink(file.getThumbnailLink());
			responseList.add(item);
		}

		return responseList;
	}
	
}
