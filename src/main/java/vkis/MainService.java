package vkis;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class MainService {

	@Value("${APP_ID}")
	private Integer APP_ID;

	@Value("${REDIRECT_URI}")
	private String REDIRECT_URI;

	@Value("${CLIENT_SECRET}")
	private String CLIENT_SECRET;

	@Value("${IMG_SIZE}")
	private int IMG_SIZE;

	private final int REQUEST_COUNT = 1000;
	private final double ASPECT_RATIO_EPS = 0.01;

	private VkApiClient vk;
	private UserActor actor;

	public String findPhoto(File sample, String albumUrl, String authCode)
			throws ClientException, ApiException, IOException
	{
		// try to construct vkis.AlbumData from url
		// if this fails, 400 status code is returned via vkis.ErrorHandler
		AlbumData albumData = new AlbumData(albumUrl);

		// get UserActor from our authCode
		vkInit(authCode);

		// get image and its aspect ratio
		BufferedImage sImg = ImageIO.read(sample);
		double sRatio = (double)sImg.getWidth() / sImg.getHeight();

		// get resized height and resize the image
		int height = (int) Math.round(IMG_SIZE/sRatio);
		sImg = ImageUtils.getMiniature(sImg, IMG_SIZE, height);
		ImageUtils sIU = new ImageUtils(sImg);

		// initialize default values for the loop
		long bestDiff = Long.MAX_VALUE;
		String response = "not found";

		// do requests until there's no photos left
		for (int offset = 0;; offset+=REQUEST_COUNT) {
			List<Photo> list = getPhotoList(albumData, offset);
			if (list.isEmpty()) break;

			// for each photo in the list, obviously
			for (Photo photo : list) {

				// get the PhotoSizes of the smallest image version and its aspect ratio
				PhotoSizes ps = photo.getSizes().get(0);
				double cRatio = (double)ps.getWidth() / ps.getHeight();

				// if ratios aren't roughly the same, skip the image
				if (Math.abs(sRatio-cRatio) > ASPECT_RATIO_EPS) continue;

				// create temp file and get the miniature
				File tempFile = File.createTempFile("photo",".jpg");
				FileUtils.copyURLToFile(ps.getUrl(), tempFile);
				BufferedImage cImg = ImageUtils.getMiniature(ImageIO.read(tempFile), IMG_SIZE, height);

				// get diff sum for the image and check if it's better than what we have
				long diff = ImageUtils.getDiff(sIU, new ImageUtils(cImg));
				if (diff < bestDiff) {
					bestDiff = diff;
					String photoUrl = "https://vk.com/photo" + photo.getOwnerId() + "_" + photo.getId();
					response = photoUrl + "," + ps.getUrl();
				}
			}
		}
		vk = null;
		actor = null;
		return response;
	}

	private void vkInit(String code) throws ApiException, ClientException {
		TransportClient transportClient = HttpTransportClient.getInstance();
		vk = new VkApiClient(transportClient);
		UserAuthResponse authResponse = vk.oAuth()
				.userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
				.execute();
		actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
	}

	private List<Photo> getPhotoList(
			AlbumData albumData, Integer offset)
			throws ClientException, ApiException
	{
		return vk.photos().get(actor)
				.albumId(albumData.getAlbumId())
				.ownerId(albumData.getOwnerId())
				.count(REQUEST_COUNT)
				.offset(offset)
				.execute()
				.getItems();
	}
}