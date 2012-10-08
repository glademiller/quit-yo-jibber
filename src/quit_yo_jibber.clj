(ns quit-yo-jibber
  (:require [quit-yo-jibber.message  :as message ]
            [quit-yo-jibber.presence :as presence])
  (:import [org.jivesoftware.smack ConnectionConfiguration
                                   XMPPConnection]))

(def connection-info {:username "mrs.doyle.teabot@gmail.com"
                      :password "mXA7oaC7"
                      :host     "talk.google.com"
                      :domain   "gmail.com"})

(defn echo [message]
  (:body message))

(defn make-connection
  "Defines and logs in to an xmpp connection, optionally registering event
   listeners for incoming messages and presence notifications. The first
   parameter is a map representing the data needed to make a connection to
   the jabber server (only username and password are required, gtalk is
   assumed by default:

   connnect-info example:
   {:host \"talk.google.com\"
    :domain \"gmail.com\"
    :username \"testclojurebot@gmail.com\"
    :password \"clojurebot12345\"}

   Following this one of two keyword arguments may be given, each of which
   expects a single argument function. The first is for a default message
   listener, which is passed a map representing a message on receive.
   Return a string from this function to pass a message back to the sender,
   or nil for no response

   received message map example (nils are possible where n/a):
   {:body
    :subject
    :thread <Id used to correlate several messages, such as a converation>
    :from <entire from id, ex. zachary.kim@gmail.com/Zachary KiE0124793>
    :from-name <Just the 'name' from the 'from', ex. zachary.kim@gmail.com>
    :to <To whom the message was sent, i.e. this bot>
    :packet-id <donno>
    :error <a map representing the error, if present>
    :type <Type of message: normal, chat, group_chat, headline, error.
           see javadoc for org.jivesoftware.smack.packet.Message>}
   "
  [{:keys [username password host domain port]
    :or   {host   "talk.google.com"
           domain "gmail.com"
           port   5222}}
   & {:keys [message presence]}]
  (doto (XMPPConnection. (ConnectionConfiguration. host port domain))
    (.connect)
    (.login username password)
    (presence/set-availability! :available)
    (message/add-message-listener message)))

(defn close-connection
  "Log out of and close an active connection"
  [#^XMPPConnection conn]
  (.disconnect conn))

(defn send-message
  "Send a message directly from a connection, outside of the normal
   response handling system"
  [conn to message-body]
  (message/send-message conn to message-body))
