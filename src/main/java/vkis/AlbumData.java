package vkis;

public class AlbumData {
	private final int ownerId;
	private final String albumId;

	// create vkis.AlbumData by extracting info from url
	public AlbumData(String albumUrl) {
		String baseUrl = "https://vk.com/album";
		String album = albumUrl.replaceFirst(baseUrl, "");

		// should now be OWNERID_ALBUMID
		String[] data = album.split("_");
		if (data.length != 2) throw new IllegalArgumentException();
		this.ownerId = Integer.parseInt(data[0]);
		switch (data[1]) {
			// special albumId cases
			case "0": this.albumId = "profile"; break;
			case "00": this.albumId = "wall"; break;
			case "000": this.albumId = "saved"; break;
			default: this.albumId = data[1];
		}
	}

	public String getAlbumId() {
		return albumId;
	}

	public int getOwnerId() {
		return ownerId;
	}
}
