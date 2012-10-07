
# quit-yo-jibber jabber client

quit-yo-jibber jabber client is a clojure wrapper around jive software's [smack](http://www.igniterealtime.org/projects/smack/) talk xmpp library. Branched from [xmpp-clj](http://github.com/zkim/xmpp-clj), this version aims to be more general, allowing for chat clients as well as chatbots.

## Lein


## Usage
Add quit-yo-jibber to your deps (project.clj):
    [quit-yo-jibber "0.3.0"]

require the core
    (ns mybot.core
      (:require [quit-yo-jibber :as xmpp]))
<br />

Define your connection params (host, domain and port optional):

    ;; Connection Info
    (def connect-info {:username "testclojurebot@gmail.com"
                       :password "clojurebot12345"
                       :host "talk.google.com"
                       :domain "gmail.com"})
<br />

Create a function to respond to a message:
    (defn handle-message [msg]
      (str "You said " (:body msg)))

Now make a connection with some callbacks defined:
    (def conn (make-connection connect-info 
                 :messages (var handle-message)
                 :presence (fn [pre] (println "New presence info: " (prn-str pre))))

<br />


    
Next, fire up your chat client, add your new buddy, and send him a message.  The response should look someting like this:

> me: hello chatbot  

> chatbot: You said hello chatbot
<br />  


When you're done with a connection, you can log out and close it like so:
    (xmpp/close-connection conn)

<br />

## Problems?

Open up an [issue](/issues)

## License

[Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html)
