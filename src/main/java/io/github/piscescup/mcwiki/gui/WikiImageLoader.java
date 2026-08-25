package io.github.piscescup.mcwiki.gui;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.piscescup.mcwiki.References;
import io.github.piscescup.mcwiki.exception.WikiRequestException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Downloads and registers wiki images as client textures.
 */
public final class WikiImageLoader {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private WikiImageLoader() {
    }

    public static CompletableFuture<LoadedImage> load(
        Minecraft minecraft,
        String imageUrl
    ) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl))
            .header("User-Agent", "MC-Wiki/1.0.0")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        return HTTP_CLIENT.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
            )
            .thenApply(WikiImageLoader::requireSuccessfulResponse)
            .thenApply(WikiImageLoader::decode)
            .thenApplyAsync(
                image -> register(minecraft, imageUrl, image),
                minecraft::execute
            );
    }

    private static byte[] requireSuccessfulResponse(
        HttpResponse<byte[]> response
    ) {
        if (response.statusCode() < 200
            || response.statusCode() >= 300) {
            throw new WikiRequestException(
                "Minecraft Wiki image returned HTTP status: "
                    + response.statusCode()
            );
        }
        return response.body();
    }

    private static NativeImage decode(byte[] bytes) {
        try {
            return NativeImage.read(bytes);
        } catch (IOException exception) {
            throw new WikiRequestException(
                "Failed to decode the Wiki image.",
                exception
            );
        }
    }

    private static LoadedImage register(
        Minecraft minecraft,
        String imageUrl,
        NativeImage image
    ) {
        Identifier textureId = References.ofPath(
            "wiki/" + Integer.toUnsignedString(imageUrl.hashCode(), 36)
                + "-" + Long.toUnsignedString(System.nanoTime(), 36)
        );

        try {
            DynamicTexture texture = new DynamicTexture(
                () -> "wiki_image",
                image
            );
            texture.upload();
            minecraft.getTextureManager().register(textureId, texture);
            return new LoadedImage(
                textureId,
                image.getWidth(),
                image.getHeight()
            );
        } catch (RuntimeException exception) {
            image.close();
            throw exception;
        }
    }

    public record LoadedImage(
        Identifier textureId,
        int width,
        int height
    ) {
        public void release(Minecraft minecraft) {
            minecraft.getTextureManager().release(this.textureId);
        }
    }
}
