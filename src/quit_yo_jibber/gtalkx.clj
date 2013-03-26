(ns quit-yo-jibber.gtalkx
  (:import [org.jivesoftware.smack.packet PacketExtension]))

(defn add-all-message-extensions!
  "Add all known gtalk extensions"
  [message]
  (doto message
    (extend-message! "google-mail-signature" "google:metadata")
    (extend-message! "active" "http://jabber.org/protocol/chatstates")
    (extend-message! "x" "google:nosave")
    (extend-message! "record" "http://jabber.org/protocol/archive")))

(defn extend-message! [message elem ns]
  (doto message
    (.addExtension (reify PacketExtension
                     (getElementName [this] elem)
                     (getNamespace [this] ns)
                     (toXML [this] (str "<" elem " xmlns=\"" ns "\"></" elem ">"))))))
