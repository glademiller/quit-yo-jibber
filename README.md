# Quit-yo-jibber Jabber library

The quit-yo-jibber jabber client is a clojure wrapper around jive software's [smack](http://www.igniterealtime.org/projects/smack/) talk xmpp library. Forked from [xmpp-clj](http://github.com/zkim/xmpp-clj), this version aims to be more general, allowing for chat clients as well as chatbots.

## Usage
Add quit-yo-jibber to your deps (project.clj):

    [quit-yo-jibber "0.4.2"]

and use the main file

    (ns my.namespace
        (:require [quit-yo-jibber :refer :all]))

Define your connection params (host, domain and port are optional arguments, but it defaults to gtalk settings):

    ;; Connection Info
    (def connect-info {:username "some.bot@gmail.com"
                       :password "*****"})

I recommend that you don't define these in code, but (read-string (slurp)) in a credentials file which you don't
put into version control.

Create a function to respond to a message:

    (defn handle-message [conn msg]
      (str "You said " (:body msg)))

Now make a connection with some callbacks defined (The var around handle-message means that you can re-define it and the underlying java listener will call your newly defined function, rather than staying on the old implementation):

    (def conn (make-connection connect-info (var handle-message))

Next, fire up your chat client, add your new buddy, and send him a message.  The response should look someting like this:

> me: hello chatbot
> chatbot: You said hello chatbot

If you want to send a message unprompted, without first receiving one, you can use the send function like so:

    (send conn "person@gmail.com" "I wouldn't like not to speak unless spoken to")

You can get roster information like so (see also the roster and available functions):

    (online conn)
    => ("me@example.com", "person@gmail.com", "friend@yahoo.com")

And you can test for online status and such with the online? and away? predicates like so:

    (online? conn "me@example.com")
    => false ;; because it's a fake email address. Of course they're not online.

When you're done with a connection, you can log out and close it like so:
    (close-connection conn)

## Changelog

### 1.4.3

* Set sent message type to chat - this means that asterisk *bold* and underbar _underline_ formatting are properly understood by the gtalk client
* Filter out only messages which contain a body, rather than chat type messages which were actually typing notifications causing extra events to fire in previous versions

### 1.4.2

* Fix presence listeners
* Added experimental ability to set-availability (experimental - not in core namespace but accessible in quit-yo-jibber.presence)

### 1.4.1

* Add in API for detecting whether users are active on their android phone or a PC

### 1.4.0

* API revision - major release

## Problems?

Open up an [issue](/issues)

## License

[Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html)
