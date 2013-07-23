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

   Optionally you may provide a default incoming message handler which
   expects a function which takes a connection and a message map.
   Return a string from this function to pass a message back to the sender,
   or nil for no response

   received message map example (nils are possible where n/a):
   {:body    ; message text
    :subject ; a subject, usually set in chat rooms
    :thread  ; id used to correlate several messages, such as a conversation
    :jid     ; entire from id, e.g. me@example.com/GTalk E0124793
    :from    ; email address of the sender
    :to      ; to whom the message was sent, i.e. this bot
   }         ; - see javadoc for org.jivesoftware.smack.packet.Message

   You may also provide a presence listener which takes a connection returns
   a map representing the presence change event (undocumented fields subject
   to change).
   {:from    ; email address of the person who this concerns
    :jid     ; entire from id, e.g. me@example.com/GTalk_E242435
    :status  ; the user's display status
    :online?
    :away?
   }
   "
  ([{:keys [username password host domain port]
     :or   {host   "talk.google.com"
            domain "gmail.com"
            port   5222}}
    & [message-fn presence-fn]]
     (let [conn (doto (XMPPConnection. (ConnectionConfiguration. host port domain))
                  (.connect)
                  (.login username password)
                  (presence/set-availability! :available))]
       (when message-fn  (message/add-message-listener   conn message-fn))
       (when presence-fn (presence/add-presence-listener conn presence-fn))
       conn)))

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

(defn on-their-phone?
  "Whether this user's jid implies they may be mobile (on an android device)"
  [conn user]
  (boolean (re-find #"from=\".*?@.*?/android.*?\"" (.toXML (.getPresence (.getRoster conn) user)))))

(defn on-their-phone
  "All the people who have android phone jids, so are probably mobile"
  [conn]
  (filter (partial on-their-phone? conn) (online conn)))

(defn send-question
  "Send a message to a user and set a one-off callback for when the user
   responds to that message, skipping the default handler. Callback
   should take a conn and message body"
  [conn to message-body callback]
  (message/send-question conn to message-body callback))

(defn awaiting-response?
  "Returns a boolean stating whether a response is currently pending for the
   given user on the given connection"
  [conn from]
  (message/awaiting-response? conn from))
