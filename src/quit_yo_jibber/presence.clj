(ns quit-yo-jibber.presence
  (:import [org.jivesoftware.smack PacketListener]
           [org.jivesoftware.smack.packet Presence
                                          Presence$Type]
           [org.jivesoftware.smack.filter MessageTypeFilter]))


(defn mapify-presence [#^Presence m]
  (try
    {:mode    (.getMode m)
     :status  (.getStatus m)
     :type    (.getType m)
     :online? (.isAvailable m)
     :away?   (.isAway m)}
    (catch Exception e (println e) {})))

(defn with-presence-map [f]
  (fn [presence] (mapify-presence (f presence))))

(def presence-types {:available   (Presence. Presence$Type/available)
                     :unavailable (Presence. Presence$Type/unavailable)})

(defn set-availability! [conn type]
  (doto conn (.sendPacket (type presence-types))))

(defn add-presence-listener [conn f]
  (doto conn
    (.addPacketListener
     (proxy [PacketListener] []
       (processPacket [packet]
         ((with-presence-map f) conn packet)))
     (MessageTypeFilter. Presence$Type/subscribe))))

(defn request-presence [conn addr]
  (let [p (Presence. Presence$Type/subscribe)]
    (.setTo p addr)
    (doto conn (.sendPacket p))))
