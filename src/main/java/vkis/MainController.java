package vkis;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Controller
@Validated
@RequestMapping(path="/")
public class MainController {
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	MainService mainService;

	// when file is sent by url
	@PostMapping(path="/findByUrl")
	public @ResponseBody String findByUrl(
			@RequestParam @NotBlank String photoUrl,
			@RequestParam @NotBlank String albumUrl,
			@RequestParam @NotBlank String authCode)
			throws ApiException, ClientException, IOException
	{
		logger.info("Received request for /findByUrl, URL: "+photoUrl);
		logger.info("Album URL: "+albumUrl);
		File tempFile = File.createTempFile("photo",".jpg");
		FileUtils.copyURLToFile(new URL(photoUrl),tempFile);
		String response = mainService.findPhoto(tempFile, albumUrl, authCode);
		if (!tempFile.delete()) logger.warn("Can't delete temp file");
		logger.info("Responding: "+response);
		return response;
	}

	// when file is sent as an actual file
	@PostMapping(path="/findByFile")
	public @ResponseBody String findByFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam @NotBlank String albumUrl,
			@RequestParam @NotBlank String authCode)
			throws ApiException, ClientException, IOException
	{
		logger.info("Received request for /findByFile");
		logger.info("Album URL: "+albumUrl);
		if (file.getSize()==0) throw new IllegalArgumentException();
		String ext = "." + FilenameUtils.getExtension(file.getOriginalFilename());
		File tempFile = File.createTempFile("photo", ext);
		file.transferTo(tempFile);
		String response = mainService.findPhoto(tempFile, albumUrl, authCode);
		if (!tempFile.delete()) logger.warn("Can't delete temp file");
		logger.info("Responding: "+response);
		return response;
	}

}