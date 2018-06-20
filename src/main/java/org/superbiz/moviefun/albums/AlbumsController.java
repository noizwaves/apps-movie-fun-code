package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        blobStore.put(new Blob(
                getCoverFilePath(albumId),
                uploadedFile.getInputStream(),
                uploadedFile.getContentType()
        ));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> existingCover = blobStore.get(getCoverFilePath(albumId));
        Blob cover = existingCover.orElseGet(this::buildDefaultCoverBlob);

        byte[] imageBytes = IOUtils.toByteArray(cover.inputStream);
        HttpHeaders headers = createImageHttpHeaders(imageBytes, cover.contentType);

        return new HttpEntity<>(imageBytes, headers);
    }


    private HttpHeaders createImageHttpHeaders(byte[] data, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(data.length);
        return headers;
    }

    private String getCoverFilePath(long albumId) {
        return format("covers/%d", albumId);
    }

    private Blob buildDefaultCoverBlob() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");

        return new Blob("default-cover", input, IMAGE_JPEG_VALUE);
    }

}
