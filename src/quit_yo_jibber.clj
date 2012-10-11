(ns quit-yo-jibber
  (:require [quit-yo-jibber.message  :as message ]
            [quit-yo-jibber.presence :as presence])
  (:import [org.jivesoftware.smack ConnectionConfiguration
                                   XMPPConnection]))

(defn make-connection
  "Defines and logs in to an xmpp connection, optionally registering event
   listeners for incoming messages and presence notifications. The first
   parameter is a map representing the data needed to make a connection to
   the jabber server (only username and password are required, gtalk is
   assumed by default:

   connnect-info example:
   {:username \"testclojurebot@gmail.com\"
    :password \"clojurebot12345\"}

   Following this one of two keyword arguments may be given, each of which
   expects a single argument function. The first is for a default message
   listener, which is passed a map representing a message on receive.
   Return a string from this function to pass a message back to the sender,
   or nil for no response

   received message map example (nils are possible where n/a):
   {:body    ; message text
    :subject ; a subject, usually set in chat rooms
    :thread  ; id used to correlate several messages, such as a converation
    :jid     ; entire from id, e.g. me@example.com/GTalk E0124793
    :from    ; email address of the sender
    :to      ; to whom the message was sent, i.e. this bot
    :error   ; a map representing the error, if present
    :type    ; type of message: normal, chat, group_chat, headline, error.
   }         ; - see javadoc for org.jivesoftware.smack.packet.Message
   "
  [{:keys [username password host domain port]
    :or   {host   "talk.google.com"
           domain "gmail.com"
           port   5222}}
   & {:keys [message roster-at]}]
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

(defn roster
  "List all of the users known about by this account, regardless of
   availability"
  [conn]
  (let [roster  (.getRoster conn)
        entries (.getEntries roster)]
    (map (memfn toString) entries)))

(defn online?
  "Whether a given user is online and visible to the logged in account"
  [conn user]
  (.isAvailable (.getPresence (.getRoster conn) user)))

(defn online
  "A list of everyone this account knows to currently be online"
  [conn]
  (map (memfn toString) (seq (.getEntries (.getRoster conn)))))

(defn away?
  "Whether a given user is either away or offline"
  [conn user]
  (or (not (online? conn user))
      (.isAway (.getPresence (.getRoster conn) user))))

(defn available
  "A list of everyone this account knows to be online and not marked
   as away"
  [conn]
  (filter #(not (away? conn %)) (online conn)))
