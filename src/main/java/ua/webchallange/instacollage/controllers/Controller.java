package ua.webchallange.instacollage.controllers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.webchallange.instacollage.NotAuthenticatedException;
import ua.webchallange.instacollage.SessionData;
import ua.webchallange.instacollage.service.CollageGenerationService;
import ua.webchallange.instacollage.service.FeedParser;
import ua.webchallange.instacollage.util.HttpHelper;
import ua.webchallange.instacollage.util.ImageHelper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Controller
public class Controller {

    @Value("${client.id}")
    private String clientId;

    @Value("${host}")
    private String serverHost;

    @Value("${server.port}")
    private String serverPort;

    @Value("${access.token.url}")
    private String accessTokenUrl;

    @Value("${client.secret}")
    private String clientSecret;

    @Autowired
    private SessionData sessionData;

    @Autowired
    private CollageGenerationService collageGenerationService;

    @Autowired
    private FeedParser feedParser;

    @RequestMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("clientId", clientId);
        model.addAttribute("redirectUri", "http://" + serverHost + ":" + serverPort + "/code");
        return "login";
    }

    @RequestMapping("/code")
    public String getCode(@RequestParam String code) throws IOException {
        final Map<String, String> params = ImmutableMap.<String, String>builder()
                .put("client_id", clientId)
                .put("client_secret", clientSecret)
                .put("grant_type", "authorization_code")
                .put("redirect_uri", "http://" + serverHost + ":" + serverPort + "/code")
                .put("code", code)
                .build();
        String response = HttpHelper.sendPostRequest(accessTokenUrl, params);
        JSONObject responseJson = new JSONObject(response);
        String accessToken = responseJson.getString("access_token");
        System.out.println("!!!!!! " + accessToken);
        sessionData.setAccessToken(accessToken);
        return "redirect:/";
    }

    @RequestMapping("/")
    public String main() {
        try {
            checkAuthenticated();
            return "main";
        } catch (NotAuthenticatedException e) {
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/generate/{height}/{width}/{photosNumber}/")
    public @ResponseBody String generate(@PathVariable int height, @PathVariable int width, @PathVariable int photosNumber) {
        try {
            checkAuthenticated();
            String response = HttpHelper.sendGetRequest("https://api.instagram.com/v1/users/self/feed",
                    ImmutableMap.<String, String>builder()
                            .put("access_token", sessionData.getAccessToken())
                            .build());
            List<String> photoLinks = feedParser.getPhotosLinks(response, photosNumber);

            List<BufferedImage> bufferedImages = new ArrayList<>();
            for (String photoLink : photoLinks) {
                bufferedImages.add(HttpHelper.sendGetRequestGetImage(photoLink));
            }
            BufferedImage collage = collageGenerationService.generate(height, width, bufferedImages);
            byte[] bytes = ImageHelper.pngImageToBytes(collage);
            return "{\"status\": \"OK\", \"data\": \"data:image/png;base64," + StringUtils.newStringUtf8(Base64.encodeBase64(bytes, false)) + "\"}";
        } catch (NotAuthenticatedException e) {
            return "{\"status\": \"fail\", \"error\": \"not authenticated\"}";
        } catch (IOException e) {
            return "{\"status\": \"fail\", \"error\": \"server error\"}";
        }
    }

    private void checkAuthenticated() throws NotAuthenticatedException {
        if (sessionData.getAccessToken() == null) {
            throw new NotAuthenticatedException();
        }
    }
}

