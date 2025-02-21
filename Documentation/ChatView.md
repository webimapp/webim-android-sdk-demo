## ChatView

`ChatView` is a full-fledged chat view component, containing the 
message list itself, an input field, as well as additional elements 
such as dialogs. Chat is a multifunctional component that implements sending 
messages, photos, videos, files and much more. Despite the huge number of 
functions, the chat is a common Android `View` component and can be easily integrated 
into the application both through XML layout and through code. The chat is built
on the principle that all default values are already set, but everything 
can be redefined according to your needs. </br>
It combines extreme ease of use with powerful functionality.
Webim `ChatView` is a good alternative to using Webim SDK as it already includes
all UI elements and adapters. And requires minimal effort to integrate.

### 1. Adding a library to dependencies

```kotlin-dsl
// kotlin dsl
dependencies {
    implementation("ru.webim.sdk:chatview:<LAST_VERSION>)
}
// groovy
dependencies {
    implementation("ru.webim.sdk:chatview:<LAST_VERSION>")
}
```

### 2. Configuring ChatVeiw

`ChatView` is a normal view component that can be defined both programmatically through code and
through XML layouts.

#### Using XML:

```xml

<ru.webim.chatview.ui.ChatView 
    android:id="@+id/chatView" 
    android:layout_width="match_parent"
    android:layout_height="match_parent" 
    app:chv_account_name="https://demo.webim.ru"
    app:chv_location_name="mobile" />
```

Parameters `chv_account_name` and `chv_location_name` define your webim account and its location.
These parameters can also be changed in code by specifying the webim session.

#### Programmatically:

Chat under the hood itself works with Webim Sdk. Therefore, in order to set up
a chat view, pass it the configured session that you previously used to work
with Webim SDK.
You need to pass the Webim session to `ChatView` through the method `ChatView#setSession`.
Now the `ChatView` will do all the work for you.
```java
ChatView chatView = new ChatView(getContext());
chatView.setSession(session);
containerView.addView(chatView);
```

The session is set up like this:

```java
WebimSession session = Webim.newSessionBuilder()
    .setContext(getContext())
    .setAccountName("https://demo.webim.ru")
    .setLocation("mobile")
    ...
    .build();
```

Next, the view is responsible for working with the session, starting, stopping and
destroying it.
</br>
Setting up a chat view programmatically allows you to customize the chat view
more flexibly. You can also use a combination of these two approaches.
Declare a chat view in the markup, and find the view in the code and set up
a session for it

### 3. Customizing the theme

You can also override the default colors for the chat view. You can do this,
for example, by creating your own style by inheriting from `ChatViewDefaultStyle`. 
```xml
<style name="MyChatTheme" parent="ChatViewDefaultStyle">
    <item name="chv_bar_background">@color/white</item>
    <item name="chv_message_border">@color/message_border</item>
    ...
</style>
```
And in the layout, specify the style you created in the `app:chv_chat_style` field of `ChatView`.
Here is an example.
```xml
<ru.webim.chatview.ui.ChatView
    android:id="@+id/chatView"
    app:chv_chat_style="@style/MyChatTheme" 
    ...
    />
```
There is a whole list of colors and drawables that can 
be overridden. This list is shown below:

Style item | Type             |
--- |------------------|
`chv_primary_color`|integer or color
`chv_secondary_color`|integer or color
`chv_accent_color`|integer or color
`chv_bar_background`|integer or color
`chv_message_border`|integer or color
`chv_text_light`|integer or color
`chv_text_strong`|integer or color
`chv_text_medium`|integer or color
`chv_text_hint`|integer or color
`chv_sent_bubble`|integer or color
`chv_sent_text`|integer or color
`chv_sent_link_text`|integer or color
`chv_sent_time_text`|integer or color
`chv_sent_edited_text`|integer or color
`chv_sent_selected`|integer or color
`chv_sent_divider`|integer or color
`chv_sent_file_data`|integer or color
`chv_received_bubble`|integer or color
`chv_received_text`|integer or color
`chv_received_link_text`|integer or color
`chv_received_time_text`|integer or color
`chv_received_edited_text`|integer or color
`chv_received_selected`|integer or color
`chv_received_divider`|integer or color
`chv_received_sender_name`|integer or color
`chv_system_bubble`|integer or color
`chv_system_text`|integer or color
`chv_system_link_text`|integer or color
`chv_system_time_text`|integer or color
`chv_buttons_background`|integer or color
`chv_buttons_pending`|integer or color
`chv_buttons_complete`|integer or color
`chv_buttons_canceled`|integer or color
`chv_message_replied`|integer or color
`chv_prompt_syncing`|integer or color
`chv_prompt_net_lost`|integer or color
`chv_prompt_net_losing`|integer or color
`chv_message_text_size`|dimension
`chv_message_corner_radius`|dimension
`chv_message_menu_background`|integer or color
`chv_chat_menu_background`|integer or color
