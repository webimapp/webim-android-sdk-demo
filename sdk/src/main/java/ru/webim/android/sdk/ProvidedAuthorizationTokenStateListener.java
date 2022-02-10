package ru.webim.android.sdk;

/**
 * Created by Nikita Lazarev-Zubov on 06.12.17.
 */

import androidx.annotation.NonNull;

/**
 * When client provides custom visitor authorization mechanism, it can be realised by providing
 * custom authorization token which is used instead of visitor fields.
 * When provided authorization token is generated (or passed to session by client app),
 * `update(providedAuthorizationToken:)` method is called. This method call indicates that client
 * app must send provided authorisation token to its server which is responsible to send it to
 * Webim service.
 */
public interface ProvidedAuthorizationTokenStateListener {
    /**
     * Method is called in two cases:
     * 1. Provided authorization token is generated (or set by client app) and must be sent to
     * client server which is responsible to send it to Webim service.
     * 2. Passed provided authorization token is not valid. Provided authorization token can be
     * invalid if Webim service did not receive it from client server yet.
     * When this method is called, client server must send provided authorization token to
     * Webim service.
     * @see Webim.SessionBuilder
     * @param providedAuthorizationToken Provided authorization token which corresponds to session
     */
    void updateProvidedAuthorizationToken(@NonNull String providedAuthorizationToken);
}
