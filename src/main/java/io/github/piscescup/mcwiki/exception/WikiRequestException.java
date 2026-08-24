package io.github.piscescup.mcwiki.exception;

/**
 * Thrown when a request to the Minecraft Wiki fails.
 *
 * @author Ren YuanTong
 * @since 1.0.0
 */
public class WikiRequestException extends RuntimeException {

    public WikiRequestException(String message) {
        super(message);
    }

    public WikiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
