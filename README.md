# Webim SDK demo
[ ![Download](https://api.bintray.com/packages/webim/maven/WebimSdkAndroid3/images/download.svg) ](https://bintray.com/webim/maven/WebimSdkAndroid3/_latestVersion)
Application demonstrates capabilities of *Webim Android SDK*. This application is published on [Google Play](https://play.google.com/store/apps/details?id=ru.webim.demo.client).

[Webim online consultant](https://webim.ru) is a multifunctional service for consulting vistors of your site using pop-up chat — it can be freely downloaded and installed as a universal script or CMS module on required web pages and can also be integrated into a mobile trading application. *Webim Android SDK* library provides developers of Google Android mobile applications with tools for integration a chat between users and operators of a developer company into those applications based on technologies used in Webim online consultant.

On the one hand this application was created to demonstrate capabilities of our SDK, on the other hand to show how to use our SDK correctly. While integrating chat into your own application be guided by this demo application - it can greatly simplify the integration.

## SDK Installation
Supported Android 5.+ (Android API 21+) versions
To start using *Webim Android SDK* add a dependency to build.gradle of your application.
```
implementation "ru.webim.sdk:webimclientsdkandroid:X.XX.X"
```
Also add to AndroidManifest.xml the following permissions:
```
<uses-permission android:name="android.permission.INTERNET"/>
```

## Usage
The public API is only in com.webimapp.android.sdk package. All the nested packages are used exclusively for SDK internal needs.
### WebimSession
To get started with the chat, you need to get WebimSession object.
```java
WebimSession sessoin = Webim.newSessionBuilder()
    .setContext(context) // Activity ot Application
    .setErrorHandler(errorHandler) // implementation of FatalErrorHandler
    .setAccountName("demo") // Webim account name
    .setLocation("mobile") // Use "mobile" if in doubt
    .build()
```
For this you need to decide on a [location](https://webim.ru/kb/start/8282-terms-and-concepts/#location) and an account of Webim service. `Webim.newSessionBuilder()` method returns builder object containing large number of session configuration methods. Read more about each of them in javadoc.

`build()` method must be called from the thread having `android.os.Looper` associated object (usually it is the main thread). Further, any work with the session must be performed from the same thread in which context the session was created (at a call of any session method it is checked which thread it was called from). All the callbacks are also performed in context of the same thread the session was created in.

Webim session contains methods for managing the network: `resume()`, `pause()`, `destroy()`.Initially, the session is created paused, i.e. to start using it, the first thing you need to do is to call `resume()`. `pause()` again suspends the session, i.e. stops use of network resources. `destroy()` method, for its part, completely releases all the resources occupied by the session and after its call session recovery is impossible (when calling any session method it is dropped `IllegalStateException`). `destroy()` and `pause()` methods are allowed to call on the destroyed session (no action is performed).This is done for integration with the Activity lifecycle. It is recommended to call `pause()` from `Activity.onStop()`, `resume()` from `Activity.onStart()`, `destroy()` from `Activity.onDestroy()`.

`getStream()` method returns `MessageStream` object, all the rest of work with a chat is done by using it.

### View message history
SDK allows to view a message history and track history changes (editing/deleting messages). For this, it is used object `MessageTracker` returned by `MessageStream.newMessageTracker(MessageListener)` method.
At each call of `MessageTracker.getNextMessages(int, GetMessagesCallback)` callback gets the next batch of the messages above in the history. At the same time `MessageTracker` saves the requested interval of the messages and tracks changes in this interval. These changes are transmitted to an object implementing `MessageListener` interface which is necessary to transmit to `MessageStream.newMessageTracker(MessageListener)` method when creating `MessageTracker` object. `MessageListener` will also receive new messages.

### Working with MessageTracker
###### MessageTracker.getNextMessages(int, GetMessagesCallback)
SDK always keeps last message called headMessage. To receive the next chat messages, use the method 
`MessageTracker.getNextMessages(int, GetMessagesCallback)`. If synchronization with the server has already occurred, the method will load the latest chat messages received from server, if synchronization has not yet occurred, then the following messages will be loaded from the local database, but if there is no messages in local database, callback will be saved and fired later after synchronization with the server. You can also use `MessageTracker.resetTo(Message)` to reset chat headMessage to specified message. 
This method respond for SDK messages state.

###### MessageTracker.getLastMessages(int, GetMessagesCallback)
Works similar to `getNextMessages(int, GetMessagesCallback)`, but if chat messages or headMessage are not empty it will reset it and load last message from local database or from  server if synchronization was occured.
This method respond for SDK messages state.

###### MessageTracker.getAllMessages(getMessagesCallback)
Method loads all messages from local database. This method doesn't respond for SDK messages state. This method will not cache callback if database messages list is empty and will not save headMessage.

```java
 // Local database keeps following messages: 2, 3, 4, 5, 6. And now headMessage = null
 tracker.getNextMessages(2, callback);  // will load messages 6, 5. headMessage = 5
 /*
 Here synchronization with server occured
 Server sent folowing messages: 2, 3, 4, 5, 6, 7, 8
 for messages 6, 5 may fire MessageListener.messageChanged
 for messages 7, 8 will fire MessageListener.messageAdded
 */
 tracker.getNextMessages(2, callback);  // will load messages 4, 3. headMessage = 3
 tracker.resetTo(4) // for message 3 will be called MessageListener.messageRemove(msg). headMessage = 4
```

### Sending messages
To send messages `MessageStream.sendMessage(String)` method is used. At the same time the message to be sent is immediately received at `MessageListener.messageAdded` (if there is an active `MessageTracker`). After confirmation of receiving the message the server calls a `MessageListener.messageChanged` method with an already current message.

### Working with push-notifications
At the beginning you must configure a session to work with push-notifications. For this, you must call `SessionBuilder.setPushSystem(PushSystem.GCM)` method. As a result Webim service will send push notifications to your device. Received pushes pass our SDK by unnoticed and in your application you must accept them by yourself. An example you can watch in the demo-appplication. After a push is received you must compare the field `from` with `Webim.getGcmSenderId()`and if they match, it means this push was sent by Webim service. Then to deserialize push data `Webim.parseGcmPushNotification(Bundle)`method is used.
```java
if (intent.getStringExtra("from").equals(Webim.getGcmSenderId())) {
    WebimPushNotification push = Webim.parseGcmPushNotification(intent.getExtras());
    // process webim push-notification
} else {
    // process your application push-notification
}
```
