(ns xmpp-clj
  (:import [org.jivesoftware.smack 
	    Chat 
	    ChatManager 
	    ConnectionConfiguration 
	    MessageListener
	    SASLAuthentication
	    XMPPConnection
	    XMPPException
	    PacketListener]
	   [org.jivesoftware.smack.packet Message Presence Presence$Type Message$Type]
	   [org.jivesoftware.smack.filter MessageTypeFilter]
	   [org.jivesoftware.smack.util StringUtils]))

(def available-presence (Presence. Presence$Type/available))

(defn mapify-error [e]
  (when e {:code (.getCode e) :message (.getMessage e)}))

(defn mapify-message [#^Message m]
  (try
   {:body (.getBody m)
    :subject (.getSubject m)
    :thread (.getThread m)
    :from (.getFrom m)
    :from-name (StringUtils/parseBareAddress (.getFrom m))
    :to (.getTo m)
    :packet-id (.getPacketID m)
    :error (mapify-error (.getError m))
    :type (keyword (str (.getType m)))}
   (catch Exception e (println e) {})))

(defn create-message [to message-body]
  (try
   (let [rep (Message.)]
     (.setTo rep to)
     (.setBody rep (str message-body))
     rep)
   (catch Exception e (println e))))

(defn send [conn to message-body]
  (.sendPacket conn (create-message to message-body)))

(defn with-message-map [handler]
  (fn [conn packet]
    (let [message (mapify-message #^Message packet)]
      (try
       (handler conn message)
       (catch Exception e (println e))))))

(defn wrap-responder [handler]
  (fn [conn message]
    (let [resp (handler message)]
      (send conn (:from message) resp))))

(defn add-message-listener [conn fn]
  (.addPacketListener conn
                      (proxy [PacketListener] []
                        (processPacket [packet]
                          ((with-message-map (wrap-responder fn)) conn packet)))
                      (MessageTypeFilter. Message$Type/chat)))

(defn add-presence-listener [conn fn]
  conn)

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
    (.sendPacket available-presence)
    (add-message-listener message)
    (add-presence-listener presence)))

(defn close-connection [#^XMPPConnection conn]
  (.disconnect conn))
